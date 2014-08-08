package com.vmware.vcopoc.operations

import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical
import java.util.logging.Level
import com.vmware.vcopoc.utils.WorkFlowUtils
import com.vmware.vcopoc.utils.PropertyMappingEnum

/**
 * For a given workflow id get the current status
 * author: samueldoyle
 */
@Canonical
@Slf4j
class GetWorkflowStatusOperation extends BaseOperation {
    public static final String MY_TARGET_METHOD = WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WSM_GET_WORKFLOW_TOKEN_STATUS)
    def paramMap

    GetWorkflowStatusOperation(String wsdlURL, String userName, String passWord) {
        super(wsdlURL, userName, passWord)
    }
    /**
     * Gets the vCO identifier for a given workflowname
     * @param workFlowId - the work flow id to get status for
     * @return - the string status
     */
    def performServiceMethod(Map params = [:]) {
        if (paramMap) {
            return makeCall(MY_TARGET_METHOD, paramMap)[0]
        }

        assert params.workFlowId : "Missing required workFlowId to execute against"
        def workFlowIds = [params.workFlowId]
        paramMap = [workflowIds: workFlowIds, username: userName, password: passWord]

        makeCall(MY_TARGET_METHOD, paramMap)[0]
    }

    /**
     * For the the previous run check wait for the completion or the timeout first
     * if completed it finished fine.
     * @param waitTimes
     * @param waitInterval
     * @return - true if success(completed) false if not or timeout
     */
    boolean doesCompleteSuccessful(int numOfCheckRetries, long waitInterval) {
        assert paramMap : "performServiceMethod has not yet been called, do that first"
        assert numOfCheckRetries && waitInterval

        def (runningStatus, completeStatus, completedStatus) =
            WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WF_STATUS_RUNNING,
                    PropertyMappingEnum.VCO_WF_STATUS_COMPLETE, PropertyMappingEnum.VCO_WF_STATUS_COMPLETED)

        for (retry in 0..<numOfCheckRetries) {
            log.info "Checking ${paramMap.workflowIds[0]}"
            def serviceCheckResult = performServiceMethod()
            switch (serviceCheckResult) {
                case runningStatus:
                    log.info "workFlow is still running on count: $retry out of ${numOfCheckRetries-1}"
                    Thread.sleep(waitInterval)
                    break
                case [completeStatus, completedStatus]:
                    log.fine "Completed!"
                    return true
                default:
                    log.log Level.WARNING, "Received an unexpected response so assuming failure, response: $serviceCheckResult"
                    return false
            }
        }

        false
    }
}
