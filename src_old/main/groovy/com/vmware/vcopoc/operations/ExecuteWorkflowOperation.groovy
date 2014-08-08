package com.vmware.vcopoc.operations

import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical
import com.vmware.vcopoc.utils.WorkFlowUtils
import com.vmware.vcopoc.utils.PropertyMappingEnum

/**
 * Performs the machine addition
 * author: samueldoyle
 */
@Canonical
@Slf4j
class ExecuteWorkflowOperation extends BaseOperation {
    public static final String MY_TARGET_METHOD = WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WSM_EXECUTE_WORKFLOW)
    List workflowTokenAttributes = []

    ExecuteWorkflowOperation(String wsdlURL, String userName, String passWord) {
        super(wsdlURL, userName, passWord)
    }

    /**
     * Appends a workflowattibute to our list for the machine creation call
     * @param name - the name of the attribute
     * @param type - the type
     * @param value - the value
     */
    void addAttribute(def name, def type, def value) {
        def workflowTokenAttribute =
            client.createType(WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WST_WORKFLOW_TOKEN_ATTRIBUTE))
        workflowTokenAttribute.name = name
        workflowTokenAttribute.type = type
        workflowTokenAttribute.value = value
        workflowTokenAttributes << workflowTokenAttribute
    }

    /**
     * Performs add operation
     * @param params - whatever params
     * @return - The new workflow id of the new add machine request
     */
    def performServiceMethod(Map params = [:]) {
        assert params.workFlowId : "Missing required workFlowId to execute against"
        def paramMap = [workflowId: params.workFlowId, username: userName, password: passWord,
                        workflowInputs: workflowTokenAttributes]
        def result = makeCall(MY_TARGET_METHOD, paramMap)
        assert result

        result.id
    }
}
