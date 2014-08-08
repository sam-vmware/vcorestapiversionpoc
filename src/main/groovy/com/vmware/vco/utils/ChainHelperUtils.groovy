package com.vmware.vco.utils

import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

/**
 * Created by samueldoyle on 7/2/13.
 * Helper class, can inject this into flow scripts
 */
@Slf4j
@Singleton
class ChainHelperUtils {

    /**
     * Given a map of 1..N of [name:value, type:value, value:value]
     * return VCO correct generated markup
     * @param postParams
     * @return
     */
    public String genPostParamXML(List postParams) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml {
            'execution-context'(xmlns: "http://www.vmware.com/vco") {
                parameters {
                    postParams.each { param ->
                        parameter(name: "${param.name}", type: "${param.type}") {
                            "${param.type}"("${param.value}")
                        }
                    }
                }
            }
        }
        println "***** XML *****"
        writer.toString()
    }

    def logChain(msg) {
        log.info msg
    }
}
