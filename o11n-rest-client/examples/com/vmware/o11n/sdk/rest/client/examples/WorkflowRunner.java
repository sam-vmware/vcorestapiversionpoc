package com.vmware.o11n.sdk.rest.client.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.kohsuke.args4j.Option;

import com.vmware.o11n.sdk.rest.client.VcoSession;
import com.vmware.o11n.sdk.rest.client.VcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.authentication.Authentication;
import com.vmware.o11n.sdk.rest.client.services.ExecutionContextBuilder;
import com.vmware.o11n.sdk.rest.client.services.ExecutionService;
import com.vmware.o11n.sdk.rest.client.services.PresentationService;
import com.vmware.o11n.sdk.rest.client.services.WorkflowService;
import com.vmware.o11n.sdk.rest.client.stubs.Parameter;
import com.vmware.o11n.sdk.rest.client.stubs.Presentation;
import com.vmware.o11n.sdk.rest.client.stubs.PresentationExecution;
import com.vmware.o11n.sdk.rest.client.stubs.Workflow;
import com.vmware.o11n.sdk.rest.client.stubs.WorkflowExecution;
import com.vmware.o11n.sdk.rest.client.stubs.WorkflowExecutionState;

class WorkflowRunnerParams extends AbstractParams {
    @Option(name = "-wf", required = true)
    private String workflowId;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}

public class WorkflowRunner extends AbstractTool {

    public WorkflowRunner(String[] args) {
        super(WorkflowRunnerParams.class, args);
    }

    public static void main(String[] args) throws Exception {
        new WorkflowRunner(args).run();
    }

    @Override
    public void run() throws IOException, InterruptedException {
        WorkflowRunnerParams params = getParams();

        VcoSessionFactory sessionFactory = createSessionFactory();

        Authentication auth = createAuthentication(sessionFactory);

        VcoSession session = sessionFactory.newSession(auth);

        WorkflowService workflowService = new WorkflowService(session);
        Workflow workflow = workflowService.getWorkflow(params.getWorkflowId());
        
        if (workflow == null) {
            System.err.print("workflow with id " + params.getWorkflowId() + " does not exist");
        } else {
            System.out.println("you are going to execute: '" + workflow.getName() + "'");
            execute(workflow, session);
        }
    }

    private void execute(Workflow workflow, VcoSession session) throws IOException, InterruptedException {
        PresentationService presentationService = new PresentationService(session);
        Presentation presentation = presentationService.getPresentation(workflow);

        System.out.println("the following parameters are expected: ");
        for (Parameter param : presentation.getInputParameters().getParameter()) {
            System.out.println("\t* " + param.getName() + " (" + param.getType() + "): " + param.getDescription());
        }

        ExecutionContextBuilder builder = new ExecutionContextBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("supply input parameters: ");

        for (Parameter param : presentation.getInputParameters().getParameter()) {
            System.out.print("\t* " + param.getName() + " (" + param.getType() + ") = ");
            addParam(builder, param, reader);
        }

        PresentationExecution presentationExecution = presentationService.createPresentationExecution(presentation,
                builder.build());
        if (!presentationExecution.isValid()) {
            System.err.println("the input was incorrect");
        } else {
            System.out.println("executing workflow....");

            ExecutionService executionService = new ExecutionService(session);
            WorkflowExecution workflowExecution = executionService.executeUsingPresentation(workflow,
                    presentationExecution);

            // wait for completion by checking every half a second, max 10 times
            workflowExecution = executionService.awaitState(workflowExecution, 500, 10,
                    WorkflowExecutionState.COMPLETED, WorkflowExecutionState.FAILED);
            
            if (workflowExecution == null) {
                System.out.println("workflow did not complete of fail withing the specified wait time");
            } else {
                System.out.println("workflow completed with status" + workflowExecution.getState().name());
                System.out.println("workflow output parameters:");
                for (Parameter param : workflowExecution.getOutputParameters().getParameter()) {
                    System.out.println("\t* " + param.getName() + " = " + extractParamValue(param));
                }
            }
        }
    }

    private String extractParamValue(Parameter param) {
        if (param.getType().equals("number")) {
            return "" + param.getNumber();
        } else if (param.getType().equals("string")) {
            return param.getString();
        } else {
            return "" + param;
        }
    }

    private void addParam(ExecutionContextBuilder builder, Parameter param, BufferedReader reader) throws IOException {
        String input = reader.readLine();
        if (param.getType().equals("number")) {
            builder.addParam(param.getName(), Double.parseDouble(input));
        } else if (param.getType().equals("string")) {
            builder.addParam(param.getName(), input);
        } else {
            System.err.println("parameters of type " + param.getType() + " are not supported in this console app");
        }
    }
}
