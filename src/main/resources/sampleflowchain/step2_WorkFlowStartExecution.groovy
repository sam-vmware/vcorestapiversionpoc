package sampleflowchain

/**
 * Created by samueldoyle on 7/01/13.
 * This part of the workflow is responsible for starting the workflow execution given the uuid of the workflow
 * we retrieved in the previous step
 * inBinding
 *  itemHref: This is the uuid of the workflow we retrieved in the previous step based on name
 * outBinding:
 *  The url of the successful triggered execution
 */
import com.vmware.vco.utils.ChainHelperUtils
import com.vmware.vco.utils.VCOUtils

DEBUG = true
assert inBinding.itemHref
// Following is a workaround for a side effect of the singleton annotation
VCOUtils.instance.initConfig(new File('vcoConfig.groovy'))

def logBindings() {
    ChainHelperUtils.instance.logChain "***** inBindings"
    inBinding.each {
        ChainHelperUtils.instance.logChain "***** $it"
    }
}

ChainHelperUtils.instance.logChain "***** START step2_WorkFlowStartExecution.groovy *****"
if (DEBUG) logBindings()

// Here we are constructing the workflow parameter(s) for our workflow.
// In this case it has only one parameter in the list. For any additional parameters
// we would have another entry
//def simpleDemoParams = [[name: "sam", type: "string", value: "Sams Test"]]
def simpleDemoParams = VCOUtils.instance.parsePostParamsDataFile(new File("simpleDemoWF.params"))

def vcoPostMarkup = ChainHelperUtils.instance.genPostParamXML(simpleDemoParams)
def requestHref = "${inBinding.itemHref.value}executions"
def thePath = requestHref - VCOUtils.instance.constructDefaultBaseURL()
def logMsg = $/
    NAME:       $inBinding.name.value
    PATH:       $thePath
    POST DATA:  $vcoPostMarkup
/$
ChainHelperUtils.instance.logChain logMsg

// Send the request
def (theResponse, client) = VCOUtils.instance.doPost([path: thePath], simpleDemoParams)
if (theResponse.statusCode != 202) {
    throw new Exception("Execution request was no successful")
}
def responseHeaders = theResponse.getHeaders()
ChainHelperUtils.instance.logChain "RESPONSE DATA:  $responseHeaders"

// A successful post will have statusCode: 202 and Location: (url of execution)
logMsg = $/
    RESPONSE RECEIVED
    RESPONSE STATUS:    $theResponse.statusCode
    RESPONSE LOCATION:  $responseHeaders.Location
/$
ChainHelperUtils.instance.logChain logMsg

// Required outBinding in chain
outBinding = [location: responseHeaders.Location]

ChainHelperUtils.instance.logChain "***** END step2_WorkFlowStartExecution.groovy *****"
