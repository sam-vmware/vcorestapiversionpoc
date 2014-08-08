#!/bin/bash
set -e

##### See README.txt For more Info #####
# Purpose is to provide a generic way to execute any vco workflow.
# param vcourl - the wsdl url of vco ws
# param vcouser - vco username
# param vcopassword - vco password
# param workflowname - the name of the workflow exactly as it appears on vco, this is needed to get the id to execute the workflow with
# param numberofpollattempts - when the workflow is executed this is the number of times we will check for the completion
# param pollperiod - number of milliseconds waited in between poll attempts
# param responsecodekey - in all the out fields this field will be the one looked at to determine if the result was success or not
#                         only happens if status was complete
# param resultcode - this is the value the responsecodekey must have to assume success
# param workflowAttributes - this is a string of pipe delimited tuples i.e. name:type:value
#                            this corresponds to the vco WorkflowTokenAttribute and each of the tuples is an entry in an array of VCO 
#                            WorkflowTokenAttribute

##### Leave These #####
baseDir=$(dirname "$0")
export GROOVY_VER="1.8.9"
export GROOVY_HOME="$baseDir/groovy-$GROOVY_VER"
export PATH="$GROOVY_HOME/bin:$PATH"
export CLASSPATH="$baseDir"
#export JAVA_OPTS="-Djavax.net.ssl.trustStore=public.jks -Djavax.net.debug=plaintext -Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Slf4jLogger -Dbase.dir=$baseDir -Dgrape.root=$baseDir/lib -Dgroovy.grapes.report.downloads=true $JAVA_OPTS"
#export JAVA_OPTS="-Djavax.net.debug=ssl -Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Slf4jLogger -Dbase.dir=$baseDir -Dgrape.root=$baseDir/lib -Dgroovy.grapes.report.downloads=true $JAVA_OPTS"
export JAVA_OPTS="-Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Slf4jLogger -Dbase.dir=$baseDir -Dgrape.root=$baseDir/lib -Dgroovy.grapes.report.downloads=true $JAVA_OPTS"


##### Can Change These #####
#export vcourl="http://vco-l-01a.corp.local:8280/vmware-vmo-webcontrol/webservice?WSDL"
export vcourl="https://vco-l-01a.corp.local:8281/vmware-vmo-webcontrol/webservice?WSDL"
export vcouser="corp\Administrator"
#export vcouser="corp\\Administrator"
#export vcouser="Administrator"
export vcopassword="VMWare1!"
export workflowname="Manage Machine Basic"
export numberofpollattempts="10"
export pollperiod="60000"
export responsecodekey="10"
export resultcode="0.0"
export workflowattributes="connectionString:string:myTestMachineName|resetMutualAuthentication:boolean:true"
export certfilename="server.cert"
export keystorename="server.jks"
export defaultpass="password"

# Remove previous files
rm -f $certfilename $keystorename

#### Run #####
# First get server cert
groovy -cp $CLASSPATH genericExecute.groovy "$vcourl" "$certfilename"
# Now create a keystore with it
#keytool -import -alias server -file $certfilename -keystore $keystorename -storepass $defaultpass -keypass $defaultpass -noprompt -trustcacerts
keytool -import -file $certfilename -keystore $keystorename -storepass $defaultpass -keypass $defaultpass -validity 3650

groovy -cp $CLASSPATH genericExecute.groovy "$vcourl" "$vcouser" "$vcopassword" "$workflowname" "$numberofpollattempts" "$pollperiod" "$responsecodekey" "$resultcode" "$workflowattributes"
