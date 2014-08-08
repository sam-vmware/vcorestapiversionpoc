package com.vmware.o11n.sdk.rest.client.examples.tests;

import org.junit.Test;

import com.vmware.o11n.sdk.rest.client.VcoSession;
import com.vmware.o11n.sdk.rest.client.services.BuilderUtils;
import com.vmware.o11n.sdk.rest.client.services.ExecutionContextBuilder;
import com.vmware.o11n.sdk.rest.client.services.ExecutionService;
import com.vmware.o11n.sdk.rest.client.services.WorkflowService;
import com.vmware.o11n.sdk.rest.client.stubs.ExecutionContext;
import com.vmware.o11n.sdk.rest.client.stubs.InventoryItem;
import com.vmware.o11n.sdk.rest.client.stubs.Workflow;
import com.vmware.o11n.sdk.rest.client.stubs.WorkflowExecution;
import com.vmware.o11n.sdk.rest.client.stubs.WorkflowExecutionState;

public class WorkflowExecutions {

    private WorkflowService workflowService;
    private VcoSession session;
    private ExecutionService executionService;

    @Test
    public void executeAWorkflowThatAcceptSdkObjectParameter() throws Exception {
        // example how to validate a broker using the "Validate a broker workflow"
        // requires AMQP plugin installed
        String id = "A08080808080808080808080808080800B808080013049260427240aee8dc5c71";
        Workflow wf = workflowService.getWorkflow(id);
        InventoryItem item = session.getRestTemplate().getForObject("https://10.23.164.112:8281/api/catalog/AMQP/Broker/32133357-bdd7-4c4f-aebd-b5df1009a6ec/",
                InventoryItem.class);

        ExecutionContext ctx = new ExecutionContextBuilder().addParam("broker", BuilderUtils.newSdkObject(item))
                .build();
        executionService = new ExecutionService(session);
        WorkflowExecution execution = executionService.execute(wf, ctx);
        execution = executionService.awaitState(execution, 500, 120, WorkflowExecutionState.COMPLETED, WorkflowExecutionState.FAILED);
        
        System.out.println(execution.getState());
    }
}
