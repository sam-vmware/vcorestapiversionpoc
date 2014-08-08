package com.vmware.vcopoc.operations

import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical

import com.vmware.vcopoc.utils.WorkFlowUtils

import com.vmware.vcopoc.utils.PropertyMappingEnum

/**
 * For a given workflow id get the current status
 * author: samueldoyle
 */
@Canonical
@Slf4j
class GetWorkflowResultOperation extends BaseOperation {
    public static final String MY_TARGET_METHOD = WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WSM_GET_WORKFLOW_TOKEN_RESULT)
    public static final String RESULT_SUCCESS_KEY = "responseCode" // WorkflowTokenAttribute.name
    public static final String RESULT_SUCCESS_VALUE = "0.0" // WorkflowTokenAttribute.value
    def paramMap

    GetWorkflowResultOperation(String wsdlURL, String userName, String passWord) {
        super(wsdlURL, userName, passWord)
    }
    /**
     * Gets the vCO identifier for a given workflowname
     * @param workFlowId - the work flow id to get status for
     * @return - the string status
     */
    def performServiceMethod(Map params = [:]) {
        if (paramMap) {
            return makeCall(MY_TARGET_METHOD, paramMap)
        }

        assert params.workFlowId : "Missing required workFlowId to execute against"
        def workFlowId = params.workFlowId
        paramMap = [workflowIds: workFlowId, username: userName, password: passWord]

        makeCall(MY_TARGET_METHOD, paramMap)
    }

    static void printResult(def result) {
        log.info "\n\n\n!!!! RESULT: WorkFlowOperation Results for ${result.size()} entries !!!!"
        result.each {
            log.info "name ${it.name}, value: ${it.value}"
        }
    }

    static boolean isSuccess(def result = [], def responseCodeKey = RESULT_SUCCESS_KEY, def responseCodeValue = RESULT_SUCCESS_VALUE) {
        result.find { it?.name == responseCodeKey }?.value == responseCodeValue
    }

}
