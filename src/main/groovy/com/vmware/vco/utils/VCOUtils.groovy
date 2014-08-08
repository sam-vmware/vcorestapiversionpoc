package com.vmware.vco.utils

import com.vmware.utils.GroovyHelper
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.RESTClient
/**
 * Created by samueldoyle on 6/28/13.
 */
@Slf4j
@Singleton
class VCOUtils {
    // This is callback method name to use if you want to invoke a script with vco response objectj
    public final String RESPONSE_CB_PATH_PROPERTY = "restRequest"
    public final String RESPONSE_CB_DATA_PROPERTY = "jsonResponseData"
    public final String RESPONSE_CB_CLIENT_PROPERTY = "restClient"
    public final String RESPONSE_CB_RESPONSE_PROPERTY = "returnedResponse"
    public final String RESPONSE_CB_METHOD_PROPERTY = "requestMethod"
    public final List POST_PARAM_REQUIRED = ["name", "type", "value"].asImmutable()
    public final String DATA_FILE_TUPLE_DEL = "\\|"
    public final String DATA_FILE_PARAM_DEL = ":"
    public final String IN_DATABINDING_NAME = "inBinding"
    public final String OUT_DATABINDING_NAME = "outBinding"
    // This needs to be a map which is used to construct the next binding

    public final boolean followRedirectsDefault = true
    public final boolean sslTrustAllCertsDefault = true

    private static InheritableThreadLocal<HashMap<String, String>> VCO_CONFIG = new InheritableThreadLocal<HashMap<String, String>>()

    // TODO Ugly to clean up
    final String hostNameKey = 'vco.host.name'
    final String hostPortKey = 'vco.host.port'
    final String userNameKey = 'vco.user.name'
    final String userPasswordKey = 'vco.user.password'
    final String httpSchemeKey = 'vco.http.scheme'
    final String charSetKey = 'vco.http.charSet'
    final String headerAcceptTypeKey = 'vco.http.headers.acceptType'
    final String headerContentTypeKey = 'vco.http.headers.contentType'


    public RESTClient createDefaultClient() {
        def config = VCO_CONFIG.get()
        if (config.isEmpty()) {
            throw new Exception("VCOUtils has not yet been initialized")
        }
        def (charSet, userName, userPassword) = [config[charSetKey], config[userNameKey], config[userPasswordKey]]

        def basicAuth = new HTTPBasicAuthorization(username: userName, password: userPassword)
        def client = new RESTClient(url: constructDefaultBaseURL())
        client.authorization = basicAuth
        client.defaultCharset = charSet

        client
    }

    def constructDefaultBaseURL()  {
        def config = VCO_CONFIG.get()
        def (httpScheme, hostName, hostPort)  = [config[httpSchemeKey], config[hostNameKey], config[hostPortKey] ]
        assert httpScheme && hostName && hostPort

        hostPort = hostPort.isNumber() ? ":$hostPort" : ""
        httpScheme + '://' + hostName + hostPort
    }

    def initConfig(File configFile) {
        def config = new ConfigSlurper().parse(configFile.toURL())
        initConfig(config)
    }

    void initConfig(ConfigObject configObject) {
        HashMap<String, String> configMap = configObject.flatten() as HashMap<String, String>
        VCO_CONFIG.set(configMap)
    }

    // Delegate property config lookup to retrieve from configobject
    // assert com.vmware.vco.utils.VCOUtils.instance."vco.http.charSet" == "UTF-8"
    def getProperty(String property) {
        if (!VCO_CONFIG.get()) {
            throw new Exception("Configuration not set")
        }
        VCO_CONFIG.get()[property] ?: this.@"$property"
    }

    void setProperty(String property, Object newValue) {
        assert newValue instanceof String
        VCO_CONFIG.get()[property] = newValue;
    }

    /**
     * Purpose is to take a dataFile which contains a parameter one per line defined by the vCO Parameter
     * and return a list of Maps defining each, the tuple itself is pipe (|) delimited which the key/value being
     * delimited by (:)
     * e.g.
     * name:myParamName|value:testValue|type:string
     * name:myParamName2|value:aNewValue|type:int
     * ...
     * @param dataFile
     * @return
     */
    List<Map> parsePostParamsDataFile(File dataFile) {
        def returnList = []
        if (!dataFile.canRead()) {
            throw new Exception("Can't read from file: $dataFile.name")
        }
        dataFile.eachLine { line ->
            def result = line.split(DATA_FILE_TUPLE_DEL).inject([:]) { map, token ->
                token.split(DATA_FILE_PARAM_DEL).with { map[it[0]] = it[1] }
                map
            }

            // Validate that we havet the required fields
            POST_PARAM_REQUIRED.each {
                assert result[it]: "Missing required parameter field: $it"
            }

            returnList << result
        }

        returnList
    }

    /**
     * Moved provide a default vCO specific GET
     * @param options : Map of options which since most are defined by config is really only
     * path: REST path (mandatory)
     * followRedirects (optional)
     * sslTrustAllCerts (optional)
     * @return theResponse object and client
     */
    def doGet(Map options = [:]) {
        assert !options.isEmpty() && options['path']
        def path = options['path']

        def acceptType = VCOUtils.instance.'vco.http.headers.acceptType'
        def contentType = VCOUtils.instance.'vco.http.headers.contentType'
        def client = VCOUtils.instance.createDefaultClient()

        log.debug "Sending GET request: $path"
        def theResponse = client.get(path: path,
                headers: ['Content-Type': contentType, 'Accept': acceptType],
                followRedirects: options['followRedirects'] ?: followRedirectsDefault,
                sslTrustAllCerts: options['sslTrustAllCerts'] ?: sslTrustAllCertsDefault)

        [theResponse, client]
    }

    /**
     * Moved provide a default vCO specific POST
     * @param options : Map of options which since most are defined by config is really only
     * path: REST path (mandatory)
     * followRedirects (optional)
     * sslTrustAllCerts (optional)
     * @param postParams : This is the list of tuples defining the parameters
     * e.g.
     * name:name|type:string|value:Sam's Test
     * name:name2|type:string|value:Sam's Second Test
     * @return
     */
    def doPost(Map requestOptions = [:], List<Map> postParams = []) {
        assert !requestOptions.isEmpty() && requestOptions['path'] && !postParams.isEmpty()

        def acceptType = VCOUtils.instance.'vco.http.headers.acceptType'
        def contentType = VCOUtils.instance.'vco.http.headers.contentType'
        def client = VCOUtils.instance.createDefaultClient()

        postParams.each { param ->
            POST_PARAM_REQUIRED.each { assert param[it]: "Missing required post attribute: $it" }
        }

        if (log.isDebugEnabled()) {
            log.debug "Sending GET request: $path"
            log.debug "XML: ${genPostParamXML(postParams)}"
        }

        def response = client.post(path: requestOptions['path'],
                headers: ['Content-Type': contentType, 'Accept': acceptType],
                followRedirects: true,
                sslTrustAllCerts: true) {
            type contentType
            xml {
                'execution-context'(xmlns: "http://www.vmware.com/vco") {
                    parameters {
                        postParams.each { param ->
                            parameter(name: "${param.name}", type: "${param.type}") {
                                "${param.type}"("${param.value}")
                            }
                        }
                    }
                }
            }
        }
        [response, client]
    }

    /**
     * Given a list of groovyScript files there is made an initial GET based on the path and the first script is
     * invoked as a normal callback. The difference here being that instead of one callback it is expected that the
     * firstcallback script outject a map of values for continuation in the chain, what's outjected becomes part
     * of what is injected next in the chain.
     * @param options : These are the options passed as the first invocation as with normal GET/POST
     * @param chainScripts : List of files of scripts to invoked in the chain
     */
    def doChain(Map options = [:], List<File> chainScripts = []) {
        assert !options.isEmpty() && options.p && !chainScripts.isEmpty()
        def chainScriptsQ = chainScripts as Queue<File>

        def theChain = new ChainScriptWrapper(chainScriptsQ)

        // Special first case follow general contract
        def (theResponse, client) = doGet(['path': options.p])
        def jsonObject = GroovyHelper.instance.toJSON(theResponse.response.contentAsString)
        def bindingMap = [(RESPONSE_CB_METHOD_PROPERTY): options.m, (RESPONSE_CB_PATH_PROPERTY): options.p,
                (RESPONSE_CB_CLIENT_PROPERTY): client, (RESPONSE_CB_RESPONSE_PROPERTY): theResponse,
                (RESPONSE_CB_DATA_PROPERTY): jsonObject]

        // Start the chain
        theChain.doNext(bindingMap)
    }

    def genPostParamXML(postParams) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.'execution-context'(xmlns: "http://www.vmware.com/vco") {
            parameters {
                postParams.each { param ->
                    parameter(name: "${param.name}", type: "${param.type}") {
                        "${param.type}"("${param.value}")
                    }
                }
            }
        }
        println "***** XML *****"
        writer.toString()
    }

    class ChainScriptWrapper {
        private File chainScript = null
        private nextInChain = null

        ChainScriptWrapper(Queue<File> theChain) {
            chainScript = theChain.poll()
            assert chainScript
            if (!theChain?.peek()) {
                nextInChain = NOOPChainEntry
                return
            }
            nextInChain = new ChainScriptWrapper(theChain)
        }

        // inBinding being the outBinding of the previous
        void doNext(Map inBinding) {
            /* def importCustomizer = new ImportCustomizer()
             importCustomizer.addImport 'com.vmware.vco.utils.ChainHelperUtils'
             def configuration = new CompilerConfiguration()
             configuration.addCompilationCustomizers(importCustomizer)*/

            log.debug "doNext inBinding: $inBinding"
            Script theScript = GroovyHelper.instance.evalExternalScript(chainScript, [(IN_DATABINDING_NAME): inBinding])
            Map outBinding = theScript.getProperty(OUT_DATABINDING_NAME)
            nextInChain.doNext(outBinding)
        }
    }

    final NOOPChainEntry = [doNext: { paramHolder ->
        println "***** NOOP DEFAULT CHAIN ENTRY *****"
        if (paramHolder) {
            log.info "***** However, I did receive the following which I will happily drop on the floor:"
            log.info paramHolder?.toString()
        }
    }]

}
