package com.vmware.utils

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
/**
 * Created by samueldoyle on 6/29/13.
 * Some helper methods
 */
@Slf4j
@Singleton
class GroovyHelper {

    // Simple method for invoking a groovy script
    Script evalExternalScript(File groovyScript, Map bindings, CompilerConfiguration compilerConfiguration = null) {
        def theScript
        try {
            GroovyShell shell = compilerConfiguration ? new GroovyShell(compilerConfiguration) : new GroovyShell()
            theScript = shell.parse(groovyScript)
            theScript.setBinding(new Binding(bindings))
            theScript.run()
        } catch (all) {
            all.printStackTrace()
        }

        theScript
    }

    void runScript(File groovyScript, List args = []) {
        GroovyShell shell = new GroovyShell();
        shell.run(groovyScript, args)
    }

    def toJSON(String jsonString) {
        def slurper = new JsonSlurper()
        slurper.parseText(jsonString)
    }
}
