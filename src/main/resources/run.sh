#!/bin/bash
set -e

##### Leave These #####
baseDir=$(dirname "$0")
export GROOVY_VER="2.1.5"
export GROOVY_HOME="$baseDir/groovy-$GROOVY_VER"
export PATH="$GROOVY_HOME/bin:$PATH"
export CLASSPATH="$baseDir:$baseDir/lib/*"
#export JAVA_OPTS="-Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Slf4jLogger -Dbase.dir=$baseDir -Dgrape.root=$baseDir/lib -Dgroovy.grapes.report.downloads=true $JAVA_OPTS"
#export JAVA_OPTS="-Djavax.net.debug=ssl:record:plaintext $JAVA_OPTS"

# Remove previous files
rm -f $certfilename $keystorename

#### Run #####
# First get server cert
#groovy -cp $CLASSPATH genericExecute.groovy "$vcourl" "$certfilename"
# Now create a keystore with it
#keytool -import -alias server -file $certfilename -keystore $keystorename -storepass $defaultpass -keypass $defaultpass -noprompt -trustcacerts -validity 3650
# groovy -cp "$CLASSPATH" -configscript config.groovy vcoSimpleController.groovy "$@"
groovy -cp "$CLASSPATH" vcoSimpleController.groovy "$@"
