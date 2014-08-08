import com.vmware.utils.GroovyHelper
import com.vmware.utils.SSLCertsHelper
import com.vmware.vco.utils.VCOUtils
/**
 * Created by samueldoyle on 6/28/13.
 * simple controller demonstrating driving vCO REST based WF
 */

def validateOptions(options) {
    def method = options.m
    def cbScript = null

    if ((!options.s || !(cbScript = new File(options.s)).canRead()) && method != "CHAIN") {
        println "Option -s is not readable"
        System.exit(1)
    }
    if (!method in ["GET", "POST"]) {
        println "Method should be either GET or POST but received $method"
        System.exit(1)
    }
    def postDataFile = null
    if (method == "POST") {
        if (!options.d || !(postDataFile = new File(options.d)).canRead()) {
            println "Option -d is not readable"
            System.exit(1)
        }
    }
    if (method == "CHAIN") {
        if (!options.c) {
            println "Option -c required for chain"
            System.exit(1)
        }
    }

    [options.p, method, cbScript, postDataFile, options.c]
}

def doPost(path, paramsDataFile) {

    def postParams = VCOUtils.instance.parsePostParamsDataFile(paramsDataFile)
    if (postParams.isEmpty()) {
        println "No valid parameters found in $paramsDataFile"
        System.exit(1)
    }

    VCOUtils.instance.doPost(['path': path], postParams)
}

def printResponse(response) {
    def NL = SSLCertsHelper.instance.newLine
    println "$NL**** RESPONSE START ****"
    println "$NL=== REQUEST ==="
    println response.request as String
    println "$NL=== RESPONSE ==="
    println response.response as String
    println "$NL=== JSON ==="
    println response.json as String
    println "$NL**** RESPONSE END ****$NL"
}

def main(options) {
    def returnedResponse, client
    def (path, method, cbScript, dataFile, scriptChain) = validateOptions(options)
    println "path, $path, method: $method, cbScript: $cbScript, dataFile: $dataFile, scriptChain: $scriptChain"
    switch (method) {
        case 'GET':
            (returnedResponse, client) = VCOUtils.instance.doGet(['path': path])
            break;
        case 'POST':
            (returnedResponse, client) = doPost(path, dataFile)
            break;
        case 'CHAIN':
            List<File> chainScriptFiles = scriptChain.split(VCOUtils.instance.DATA_FILE_PARAM_DEL).collect { new File(it) }
            chainScriptFiles.each { assert it.canRead() }
            VCOUtils.instance.doChain(['p':options.p, 'm':options.m], chainScriptFiles)
            break;
        default:
            println "Unknown HTTP method: $method. Must be [GET|POST|CHAIN]"
            System.exit(1)
    }

    // Chain handles callbacks
    if (method != "CHAIN") {
        def jsonObject = GroovyHelper.instance.toJSON(returnedResponse.response.contentAsString)
        def bindingMap = [
                (VCOUtils.instance.RESPONSE_CB_METHOD_PROPERTY): options.m,
                (VCOUtils.instance.RESPONSE_CB_PATH_PROPERTY): options.p,
                (VCOUtils.instance.RESPONSE_CB_CLIENT_PROPERTY): client,
                (VCOUtils.instance.RESPONSE_CB_RESPONSE_PROPERTY): returnedResponse,
                (VCOUtils.instance.RESPONSE_CB_DATA_PROPERTY): jsonObject
        ]
        // Invoke callback with the responseDataJSON set to the JSON content returned in the response
        GroovyHelper.instance.evalExternalScript(cbScript, bindingMap)
    }
}

/**** Start ****/
def cli = new CliBuilder()
cli.with {
    h longOpt: 'help', 'Help - This Message'
    o longOpt: 'optionsConfigFile', 'A groovy script containing necessary configuration options information', type: String, args: 1, required: true
    p longOpt: 'path', 'REST API path e.g. /info', type: String, args: 1, required: true
    s longOpt: 'callbackScript', 'A groovy script with a *run* method that will be called with the responseData as a parameter', type: String, args: 1, required: false
    m longOpt: 'method', 'The http method GET,POST,CHAIN. Chain allows you to you link multiple callbacks which feed into the next of a series.', type: String, args: 1, required: true
    d longOpt: 'postData', 'If POST operation then required file containing data', type: String, args: 1, required: false
    c longOpt: 'scriptChainString', 'If CHAIN operation then required : separated string of scripts defining the chain', type: String, args: 1, required: false
}
def options = cli.parse(args)
if (!options) return
if (options.h) cli.usage()

// Initialize defaults, must be done before running anything
VCOUtils.instance.initConfig(new File(options.o))

main(options)
