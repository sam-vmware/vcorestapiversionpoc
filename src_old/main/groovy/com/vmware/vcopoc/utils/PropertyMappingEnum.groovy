package com.vmware.vcopoc.utils

/**
 * Associationed with PropertyMappings.groovy
 * Although not needed there can be some other uses for this such as doing validation of the PropertyMappings file
 * and shortcuts for looking up
 * author: samueldoyle
 */
public enum PropertyMappingEnum {

    // WorkFlow Names
    WF_FIND_MACHINE_ID_BY_NAME("vco.workflow.names.findMachineIdByName", true),
    WF_MANAGE_MACHINE_BASIC("vco.workflow.names.manageMachineBasic", false),
    WF_APPD_PATCHING("vco.workflow.names.appDPatching", false),
    WF_ASSESS_COMPLIANCE("vco.workflow.names.assessCompliance", false),

    // This is the main workflow at the moment
    WF_GENERIC_PATCHING("vco.workflow.names.genericPatching", true),

    // WorkFlow Statuses
    VCO_WF_STATUS_COMPLETE("vco.workflow.status.complete", true),
    VCO_WF_STATUS_COMPLETED("vco.workflow.status.completed", true),
    VCO_WF_STATUS_RUNNING("vco.workflow.status.running", true),

    // WebService Methods
    VCO_WSM_EXECUTE_WORKFLOW("vco.webservice.methods.executeWorkFlow", true),
    VCO_WSM_GET_WORKFLOWS_WITH_NAME("vco.webservice.methods.getWorkflowsWithName", true),
    VCO_WSM_GET_WORKFLOW_TOKEN_RESULT("vco.webservice.methods.getWorkflowTokenResult", true),
    VCO_WSM_GET_WORKFLOW_TOKEN_STATUS("vco.webservice.methods.getWorkflowTokenStatus", true),

    // WebService Types
    VCO_WST_WORKFLOW_TOKEN_ATTRIBUTE("vco.webservice.types.tokenAttribute", true)

    def final propertyPath, required

    PropertyMappingEnum(propertyPath, required) {
        this.propertyPath = propertyPath
        this.required = required
    }
}