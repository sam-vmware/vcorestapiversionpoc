package com.vmware.vcopoc.flows

import com.vmware.vcopoc.exceptions.ValidationException
import com.vmware.vcopoc.operations.ExecuteWorkflowOperation
import com.vmware.vcopoc.operations.GetWorkFlowNameOperation
import com.vmware.vcopoc.operations.GetWorkflowResultOperation
import com.vmware.vcopoc.operations.GetWorkflowStatusOperation
import com.vmware.vcopoc.utils.PropertyMappingEnum
import com.vmware.vcopoc.utils.WorkFlowUtils
import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical

/**
 * author: samueldoyle
 * More usage here
 * All flows should inherit from this.
 */
@Canonical
@Slf4j
abstract class BaseFlow {
    String vcoUrl
    String username
    String password
    String machineName
    int numberOfPollAttempts
    long pollPeriod
    def status

    BaseFlow(String vcoUrl, String username, String password, String machineName,
             String numberOfPollAttempts, String pollPeriod) {
        assert vcoUrl && username && password && machineName && numberOfPollAttempts?.isNumber() && pollPeriod?.isNumber()
        this.vcoUrl = vcoUrl
        this.username = username
        this.password = password
        this.machineName = machineName
        this.numberOfPollAttempts = numberOfPollAttempts as int
        this.pollPeriod = pollPeriod as long
    }

    BaseFlow(String vcoUrl, String username, String password, String numberOfPollAttempts, String pollPeriod) {
        assert vcoUrl && username && password && numberOfPollAttempts?.isNumber() && pollPeriod?.isNumber()
        this.vcoUrl = vcoUrl
        this.username = username
        this.password = password
        this.numberOfPollAttempts = numberOfPollAttempts as int
        this.pollPeriod = pollPeriod as long
    }

    protected def getWorkFlowIdByName(String workFlowName) {
        log.info "Getting workflow id for workflowname: $workFlowName"
        def workFlowOperation = new GetWorkFlowNameOperation(vcoUrl, username, password)
        def workflowid = workFlowOperation.performServiceMethod([workFlowName: workFlowName])
        log.info "Returning workflowid: $workflowid"

        workflowid
    }

    /**
     * Gets whatever result is returned for the workflow
     * @param workFlowId
     * @return
     */
    protected def getWorkFlowResult(String workFlowId) {
        log.info "Getting workflow result for workflowid: $workFlowId"
        def workflowResultOperation = new GetWorkflowResultOperation(vcoUrl, username, password)
        def result = workflowResultOperation.performServiceMethod([workFlowId: workFlowId])
        log.info "Returning result: $result"

        result
    }

    protected def getMachineId() {
        assert machineName : "Machine name has not been set. Unable to get machine id"
        log.info "Getting machine id for $machineName"
        def findMachineIdByNameConfig =
            WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.WF_FIND_MACHINE_ID_BY_NAME)
        def findMachineByIdWF = getWorkFlowIdByName(findMachineIdByNameConfig.wfName)

        def getMachineIdOperation = new ExecuteWorkflowOperation(vcoUrl, username, password)
        def name, type, value

        findMachineIdByNameConfig.inAttributes.each { inAttr ->
            (name, type, value) = [inAttr.name, inAttr.type, inAttr.value != null ? inAttr.value : inAttr.defaultValue]
            log.info "Adding a new attribute to getMachineIdOperation. name: $name, type: $type, value: $value"
            getMachineIdOperation.addAttribute(name, type, value)
        }

        def theMachineIdWF = getMachineIdOperation.performServiceMethod([workFlowId: findMachineByIdWF])
        if (!waitForWorkflowStatusSuccessOrFailure(theMachineIdWF)) {
            throw new Exception("Failed to obtin the machineid for machine $machineName, workflow: $theMachineIdWF")
        }

        def result = getWorkFlowResult(theMachineIdWF)
        log.info "Displaying results for getMachineId for machineName $machineName"
        GetWorkflowResultOperation.printResult(result)
        assert result.size() == 1: "Get machine id should only have one list entry."
        def theMachineId = result[0].value

        log.info "Returning machineId: $theMachineId"

        theMachineId
    }

    /**
     * Returns if the status of the workflow has completed for failed
     */
    protected boolean waitForWorkflowStatusSuccessOrFailure(String workFlowId) {
        assert workFlowId: "Missing required workFlowId"
        def getStatusOperation = new GetWorkflowStatusOperation(vcoUrl, username, password)
        status = getStatusOperation.performServiceMethod([workFlowId: workFlowId])

        def statusList =
            WorkFlowUtils.instance.getConfigProperty(PropertyMappingEnum.VCO_WF_STATUS_COMPLETE, PropertyMappingEnum.VCO_WF_STATUS_COMPLETED)
        status in statusList || getStatusOperation.doesCompleteSuccessful(numberOfPollAttempts, pollPeriod)
    }

    abstract def performFlow()

    // Make sure whatever attributes that are required for this workflow has been defined correctly
    protected abstract def validate() throws ValidationException
}
