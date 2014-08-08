package com.vmware.vcopoc.operations

import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical
import com.vmware.vcopoc.utils.WorkFlowUtils
import com.vmware.vcopoc.utils.PropertyMappingEnum

/**
 * Just obtains a work flow
 * author: samueldoyle
 */
@Canonical
@Slf4j
class GetWorkFlowNameOperation extends BaseOperation {
    public static final String MY_TARGET_METHOD =
        WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WSM_GET_WORKFLOWS_WITH_NAME)

    GetWorkFlowNameOperation(String wsdlURL, String userName, String passWord) {
        super(wsdlURL, userName, passWord)
    }

        /**
     * Gets the vCO identifier for a given workflowname
     * @param workFlowName
     * @return - the workflow identifier
     */
    def performServiceMethod(Map params = [:]) {
        assert params.workFlowName : "Missing required workFlowName"
        def paramMap = [workflowName: params.workFlowName, username: userName, password: passWord]
        def result = makeCall(MY_TARGET_METHOD, paramMap)
        assert result

        result[0].id
    }
}
