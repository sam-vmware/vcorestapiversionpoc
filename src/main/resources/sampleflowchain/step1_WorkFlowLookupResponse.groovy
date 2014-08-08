/**
 * Created by samueldoyle on 7/01/13.
 * Sample CallBack
 *
 * This is the first step in a sample workflow, it is called back from the initial starting
 * get request like so as it was today for testing simple-demo workflow:
 * ./run.sh -p '/api/workflows?conditions=name=simple-demo' -m 'CHAIN' -o './vcoConfig.groovy' -c './sampleflowchain/step1_WorkFlowLookupResponse.groovy' 2>&1 | tee run.out
 *
 * inBinding:
 *  requestMethod: The method used in the request GET/POST
 *  restRequest: The path used for the request
 *  jsonResponseData: The jsonDataObject returned from the response
 *  // The following added in case just the response data isn't enough
 *  restClient: The rest client object itself
 *  returnedResponse: The response object itself
 * outBinding:
 * [['itemHref',value]['name':value]]
 */
import com.vmware.vco.utils.ChainHelperUtils
import groovy.json.*

DEBUG = true
assert inBinding.requestMethod && inBinding.restRequest && inBinding.jsonResponseData

def logBindings() {
    def jsonString = JsonOutput.toJson(inBinding.jsonResponseData)
    def msg = $/
        METHOD:           $inBinding.requestMethod
        REQUEST:          $inBinding.restRequest
        RESPONSE STATUS:  $inBinding.returnedResponse.statusCode
        RESPONSE:         ${JsonOutput.prettyPrint(jsonString)}
    /$
    ChainHelperUtils.instance.logChain msg
}

ChainHelperUtils.instance.logChain "***** START step1_WorkFlowLookupResponse *****"

if (DEBUG) logBindings()
def outNodes = inBinding.jsonResponseData.links.attributes.flatten().findAll { it.name in ["itemHref","name"] }

// Required outBinding in chain
outBinding = outNodes.inject([:]) { map, node ->
    // Addition will merge so add new maps with the name being the node, fix later
    map << [(node.name): node]
    map
}

ChainHelperUtils.instance.logChain "***** END step1_WorkFlowLookupResponse *****"
