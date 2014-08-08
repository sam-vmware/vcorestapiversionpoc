package com.vmware.vcopoc.operations

import com.vmware.vcopoc.utils.ServiceMethodInvoker
import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical
import org.codehaus.groovy.runtime.StackTraceUtils

/**
 * Acts as the base for all others to inherit
 * author: samueldoyle
 */
@Canonical
@Slf4j
abstract class BaseOperation {
    String wsdlURL
    String userName
    String passWord
    def client

    BaseOperation(String wsdlURL, String userName, String passWord) {
        this.wsdlURL = wsdlURL
        this.userName = userName
        this.passWord = passWord
        this.client = new ServiceMethodInvoker(this.wsdlURL, this.userName, this.passWord)
    }

    /**
     * Simply make the call and return response
     * @param serviceMethod
     * @param paramMap
     * @return
     */
    protected def makeCall(String serviceMethod, Map paramMap = [:]) {
        try {
        log.info "Making call with method: $serviceMethod, params: $paramMap"
        def result = client.callWebService(serviceMethod, paramMap)
        log.info "Returning: $result"

        result
        } catch(all) {
	    all.printStackTrace()
            //def santized = StackTraceUtils.deepSanitize(all)
            //StackTraceUtils.printSanitizedStackTrace(santized)
        }
    }

    abstract def performServiceMethod(Map params)
}

