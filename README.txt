Purpose is to provide a generic way to execute any vco workflow.
param vcoWSURL - the wsdl url of vco ws 
param username - vco username
param password - vco password
param workflowname - the name of the workflow exactly as it appears on vco, this is needed to get the id to execute the workflow with
param numberofpollattempts - when the workflow is executed this is the number of times we will check for the completion
param pollperiod - number of milliseconds waited in between poll attempts
param responsecodekey - in all the out fields this field will be the one looked at to determine if the result was success or not
                        only happens if status was complete
param resultcode - this is the value the responsecodekey must have to assume success
param workflowAttributes - this is a string of pipe delimited tuples i.e. name:type:value - this corresponds to the vco WorkflowTokenAttribute
                           and each of the tuples is an entry in an array of VCO WorkflowTokenAttribute
Two example usages:
As script:
----------

cd $run_dir

./genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay" "Find Machine Id by Name" "10" "60000" "machineId" "99" "machineName:string:myTestMachineName"

./genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay" "Manage Machine Basic" "10" "60000" "resultCode" "0.0" "connectionString:string:myTestMachineName|resetMutualAuthentication:boolean:true"


Run with Groovy
---------------

cd $run_dir or setup the classpath to include

- Setup java options, here if you have to go through a proxy for example, the grape.root is where dependencies are downloaded to and the report.download is so that you can see something is happening. The first time you run this it takes a little bit
export JAVA_OPTS="-Dhttp.proxyHost=proxy.vmware.com -Dhttp.proxyPort=3128  -Dgrape.root=$DEPENDENCY_DOWNLOAD_DIR/lib -Dgroovy.grapes.report.downloads=true $JAVA_OPTS"

groovy -cp "`pwd`:$CLASSPATH" $run_dir/genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay" "Find Machine Id by Name" "10" "60000" "machineId" "99" "machineName:string:myTestMachineName"

groovy -cp "`pwd`:$CLASSPATH" $run_dir/genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay"
                            "Manage Machine Basic" "10" "60000" "resultCode" "0.0" "connectionString:string:myTestMachineName|resetMutualAuthentication:boolean:true"


The manage machhine basic is an example which uses a VCM Workflow

Some output
-----------

Success case, here we expect to find a machineId of 99 for machine with machineName myTestMachineName

./genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay" "Find Machine Id by Name" "10" "60000" "machineId" "99" "machineName:string:myTestMachineName"

!!!! RESULT: WorkFlowOperation Results for 1 entries !!!!
15:34:54.725 [main] INFO  c.v.v.o.GetWorkflowResultOperation - name machineId, value: 99
15:34:54.729 [main] INFO  c.v.v.flows.VcoGenericExecuteFlow - !!!! SUCCESS !!!! 

Error case, here we were expecting an id 0f 98 but it is 99 as displayed in the previous success case.

!!!! RESULT: WorkFlowOperation Results for 1 entries !!!!
15:44:07.808 [main] INFO  c.v.v.o.GetWorkflowResultOperation - name machineId, value: 99
Caught: Assertion failed: 

assert GetWorkflowResultOperation.isSuccess(result, myResponseCodeKey, myExpectedResponseCodeValue)
                                  |         |       |                  |
                                  false     |       machineId          98
                                            [ch.dunes.vso.webservice.WorkflowTokenAttribute@6a65e3ba]

Assertion failed: 

assert GetWorkflowResultOperation.isSuccess(result, myResponseCodeKey, myExpectedResponseCodeValue)
                                  |         |       |                  |
                                  false     |       machineId          98
                                            [ch.dunes.vso.webservice.WorkflowTokenAttribute@6a65e3ba]

        at com.vmware.vcopoc.flows.VcoGenericExecuteFlow.performFlow(VcoGenericExecuteFlow.groovy:83)
        at com.vmware.vcopoc.flows.VcoGenericExecuteFlow$performFlow.call(Unknown Source)
        at genericExecute.entry(genericExecute.groovy:44)
        at genericExecute$entry.callCurrent(Unknown Source)
        at genericExecute.run(genericExecute.groovy:56)

