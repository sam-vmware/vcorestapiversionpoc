package sampleflowchain

import com.vmware.utils.GroovyHelper
import com.vmware.vco.utils.ChainHelperUtils

/**
 * Created by samueldoyle on 7/01/13.
 * This step we have triggered a successful execution start of the workflow.
 * Now monitor its state
 * inBinding:
 *  Location: The uuid of our executing workflow
 * outBinding:
 *  NONE: This is the last step, successful exit completes the chain with last being the default NOOP
 */
import com.vmware.vco.utils.VCOUtils

import java.util.concurrent.atomic.AtomicInteger

DEBUG = true
assert inBinding.location
// Following is a workaround for a side effect of the singleton annotation
VCOUtils.instance.initConfig(new File('vcoConfig.groovy'))

def logBindings() {
    ChainHelperUtils.instance.logChain "***** inBindings"
    inBinding.each {
        ChainHelperUtils.instance.logChain "***** $it"
    }
}

ChainHelperUtils.instance.logChain "***** START step3_WorkFlowMonitorExecutionStatus.groovy *****"
if (DEBUG) logBindings()

def wfExecutionHref = inBinding.location
def wfStatusRequestPath = wfExecutionHref - VCOUtils.instance.constructDefaultBaseURL()

// Now poll the server for status
def counter = new AtomicInteger()
def maximumNumberOfTries = 60
def waitBetweenTries = 10000
for (i in 0..<maximumNumberOfTries) {
    def complete = false
    counter.incrementAndGet()
    ChainHelperUtils.instance.logChain "Request # $i with path: $wfStatusRequestPath"
    def (theResponse, client) = VCOUtils.instance.doGet([path: wfStatusRequestPath])
    def jso = GroovyHelper.instance.toJSON(theResponse.contentAsString)
    switch (jso?.state?.toLowerCase()) {
        case "completed":
            ChainHelperUtils.instance.logChain "WorkFlow Execution ID: $jso.id completed for WorkFlow: $jso.name"
            ChainHelperUtils.instance.logChain jso.toString()
            complete = true
            break
        case "running":
            ChainHelperUtils.instance.logChain "WorkFlow Execution ID: $jso.id still running"
            break
        default:
            throw new Exception("WorkFlow Execution ID: $jso.id in unknown state for WorkFlow: $jso.name")
    }
    if (complete) {
        break
    }
    sleep waitBetweenTries
}

// Successfully complete so outject an empty map
outBinding = [:]

ChainHelperUtils.instance.logChain "***** END step3_WorkFlowMonitorExecutionStatus.groovy *****"
