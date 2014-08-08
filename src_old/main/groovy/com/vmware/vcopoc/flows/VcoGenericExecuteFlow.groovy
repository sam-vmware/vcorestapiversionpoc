package com.vmware.vcopoc.flows

import com.vmware.vcopoc.exceptions.ValidationException
import com.vmware.vcopoc.operations.ExecuteWorkflowOperation
import com.vmware.vcopoc.operations.GetWorkflowResultOperation
import com.vmware.vcopoc.utils.PropertyMappingEnum
import com.vmware.vcopoc.utils.WorkFlowUtils
import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical
import com.vmware.vcopoc.exceptions.StatusFailureException

/**
 * author: samueldoyle
 * This can be used for executing any flow.
 * Params for this are
 */
@Canonical
@Slf4j
class VcoGenericExecuteFlow extends BaseFlow {
    def workflowId
    def newExecutionWFId
    def workflowName
    def wfAttributes = [:]
    def myResponseCodeKey
    def myExpectedResponseCodeValue

    public VcoGenericExecuteFlow(String vcoUrl, String username, String password, String targetWorkflowName,
                                  String numberOfPollAttempts, String pollPeriod, String responseCodeKey,
                                  String expectedResponseCodeValue, String wfAttributesStr) {
        super(vcoUrl, username, password, numberOfPollAttempts, pollPeriod)
        assert targetWorkflowName && responseCodeKey && expectedResponseCodeValue : "Expected value missing: targetWorkflowName $targetWorkflowName, responseCodeKey: $responseCodeKey, expectedResponseCodeValue: $expectedResponseCodeValue"
        this.workflowName = targetWorkflowName
        this.myResponseCodeKey = responseCodeKey
        this.myExpectedResponseCodeValue = expectedResponseCodeValue
        wfAttributes = WorkFlowUtils.instance.extractGenericWorkflowParams(wfAttributesStr)
        log.info "Extracted attributes: ${wfAttributes}"
    }

    protected def getWorkFlow() {
        workflowId = getWorkFlowIdByName(workflowName)
        assert workflowId
        log.info "Workflow id: $workflowId"
        this
    }

    protected def executeFlow() {
        def executeOperation = new ExecuteWorkflowOperation(vcoUrl, username, password)
        def name, type, value

        wfAttributes.each { key, typeValueArray ->
            (name, type, value) = [key, *typeValueArray]
            log.info "Adding a new attribute to $workflowName. name: $name, type: $type, value: $value"
            executeOperation.addAttribute(name, type, value)
        }

        newExecutionWFId = executeOperation.performServiceMethod([workFlowId: workflowId])
        assert newExecutionWFId
        log.info "Work flow id: $newExecutionWFId"
        this
    }

    protected def waitForSuccessOrFailureResult() throws StatusFailureException {
        if(!super.waitForWorkflowStatusSuccessOrFailure(newExecutionWFId)) {
            throw new StatusFailureException("$workflowName operation failed to complete with success")
        }
        this
    }

    // Unknown generic execution flow, no params we know anything about so just return this
    protected def validate() throws ValidationException {
        this
    }

    def performFlow() {
        def result = this.validate()
                .getWorkFlow()
                .executeFlow()
                .waitForSuccessOrFailureResult()
                .getWorkFlowResult(newExecutionWFId)
        log.info "!!!! Displaying results for VcoGenericExecuteFlow.performFlow !!!!"
        GetWorkflowResultOperation.printResult(result)
        assert GetWorkflowResultOperation.isSuccess(result, myResponseCodeKey, myExpectedResponseCodeValue)
        log.info "!!!! SUCCESS !!!! "
    }
}
