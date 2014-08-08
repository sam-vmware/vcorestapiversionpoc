#!/usr/bin/env groovy -cp "`pwd`:$CLASSPATH" -Dhttp.proxyHost=proxy.vmware.com -Dhttp.proxyPort=3128 -Dgroovy.grape.report.downloads=true

/*
* Purpose is to provide a generic way to execute any workflow not assuming any vcm dependencies.
* param vcoWSURL - the wsdl url of vco ws
* param username - vco username
* param password - vco password
* param workflowname - the name of the workflow exactly as it appears on vco, this is needed to get the id
* param numberofpollattempts - when the workflow is executed this is the number of times we will check for the completion
* param pollperiod - number of milliseconds waited in between poll attempts
* param responsecodekey - in all the out fields this field will be the one looked at to determine if the result was success or not
*                         only happens if status was complete
* param resultcode - this is the value the responsecodekey must have to assume success
* param workflowAttributes - this is a string of pipe delimited tuples i.e. name:type:value - this corresponds to the vco WorkflowTokenAttribute
*                            and each of the tuples is an entry in an array of WorkflowTokenAttribute
* Example usage:
*
* Standalone with GRAB
* export JAVA_OPTS="-Dhttp.proxyHost=proxy.vmware.com -Dhttp.proxyPort=3128  -Dgrape.root=$VCO_INSTALL_BASE/lib -Dgroovy.grapes.report.downloads=true $JAVA_OPTS"
* #!/usr/bin/env groovy
* $run_dir/genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay"
*                                "Manage Machine Basic" "10" "60000" "resultCode" "0.0" "connectionString:string:myTestMachineName|resetMutualAuthentication:boolean:true|..."
*
* Run with Groovy
* groovy -cp $CLASSPATH $run_dir/genericExecute.groovy "http://10.25.49.138:8280/vmware-vmo-webcontrol/webservice?WSDL" "wp\\kkannan" "3812SunnyDay"
*                                "Manage Machine Basic" "10" "60000" "resultCode" "0.0" "connectionString:string:myTestMachineName|resetMutualAuthentication:boolean:true|..."
*
* author: samueldoyle
*/

@GrabExclude(group="org.codehaus.groovy", module="groovy")
@Grapes([
    @Grab(group="org.codehaus.groovy.modules", module="groovyws", version="0.5.2"),
    @Grab(group="org.slf4j", module="slf4j-api", version="1.7.1"),
    @Grab(group='org.slf4j', module='log4j-over-slf4j', version='1.7.1'),
    @Grab(group="ch.qos.logback", module="logback-classic", version="1.0.7"),
])
import com.vmware.vcopoc.flows.VcoGenericExecuteFlow
import com.vmware.vcopoc.utils.WorkFlowUtilsHelper

def entry(args) {
    println "\n---- Using args: $args ----\n"
    def genericExecuteFlow = new VcoGenericExecuteFlow(*args)
    try {
        genericExecuteFlow.performFlow()
    } catch(all) {
        println "\n**** ERROR ****\n"
        all.printStackTrace()
    }
}

def getServerCert(url, certFileName) {
    assert url

    def wfHelper = new WorkFlowUtilsHelper()
    def certString = wfHelper.getServerCert(url)

    assert certString
    new File(certFileName).text = certString
}

if (args && args.length == 2) {
    // Assume get cert for now
    getServerCert(args[0], args[1])
    System.exit(0)
}

if (!args || !args.length == 9) {
    println "Usage genericExecute.groovy vCOUrl username password workflowname numberofpollattempts pollperiod responsekey expectedresponsevalue 'name1:type1:value1, name2:type:value2, name3:type:value3, ...'"
    System.exit(1)
}

entry(args)

