package com.vmware.vcopoc.utils

import groovy.util.logging.Log
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import groovyx.net.ws.WSClient
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

import org.slf4j.*
import java.util.Iterator
import java.net.URL
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.HttpsURLConnection
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.apache.cxf.transports.http.configuration.ConnectionType

/**
 * Some statically defined content and a place to stick utilitiy methods.
 * author: samueldoyle
 */

@Slf4j
@Singleton
@Canonical
class WorkFlowUtils {
    private ConfigObject propertyConfig
    private Map flattenedPropertyConfig
    @Delegate private WorkFlowUtilsHelper workFlowUtilsHelper = new WorkFlowUtilsHelper()

    private WorkFlowUtils() {
       Class propertyClass = defaultPropertyClass()
       propertyConfig = (new ConfigSlurper()).parse(propertyClass)
       flattenedPropertyConfig = propertyConfig.flatten()

        // Test to see those properties that are required are there.
       PropertyMappingEnum.values().each { p ->
            if (p.required) {
                assert getConfigProperty(p) : "Property ${p.propertyPath} is mandatory but was not found!"
            }
        }
    }

    @WithReadLock
    public def getConfigProperty(PropertyMappingEnum... properties) {
        assert properties : "Required propertyPath(s)"
        def returnResult = []

        returnResult = properties.collect { p ->
            p.propertyPath.split("\\.").inject(propertyConfig) { root, nextNode ->
                root."$nextNode"
            }
        }

        if (!returnResult) {
            log.warning "Couldn't locate property matching: $properties"
        }

        return returnResult.size() == 1 ? returnResult[0] : returnResult
    }

    // This merges in and overrides any existing values with those passed in
    @WithWriteLock
    public void reinitConfig(Map toBind = [:]) {
        log.info "Reinitializing config with the following new bindings, these will override present values: $toBind"
        Class propertyClass = defaultPropertyClass()
        def slurper = new ConfigSlurper()
        slurper.setBinding(toBind)
        def config = slurper.parse(propertyClass)
        propertyConfig.merge(config)
        flattenedPropertyConfig = propertyConfig.flatten()
    }

    private static Class defaultPropertyClass() {
        WorkFlowUtils.class.classLoader.loadClass("com.vmware.vcopoc.conf.PropertyMappings")
    }

    public boolean validateAttributeList(List attributes) {
        if (!attributes) {
            log.warning "No attributes provided so returning false to be on the safe side"
            return false
        }

        for (attribute in attributes) {
            if (attribute.value != null && attribute.value instanceof Map && attribute.value.isEmpty()) {
                attribute.value = null
            }

            log.info "**** Validating attribute: name: ${attribute.name}, type: ${attribute.type}, value: ${attribute.value}, defaultValue: ${attribute.defaultValue}"

            if (attribute.name == null) {
                log.severe "Attribute $attribute is missing its name"
                return false
            } else if (attribute.value == null && attribute.defaultValue == null) {
                log.severe "Attribute ${attribute.name} has a required value which was not set"
                return false
            } else if (attribute.type == null) {
                log.severe "Attribute ${attribute.name} is missing its type"
                return false
            }
        }

        true
    }

    /**
     * From the script commandline passed in from the AppD script should be a list of the attributes that are to
     * be passed to the workflow. The format should be key1:value1|key2:value2| ...
     * This may only be used for workflows which are known and have their types defined in PropertyMappings.groovy
     * @param workFlowAttributesStr
     * @return - Map containing key value pairs
     */
    public Map extractWorkflowParams(String workFlowAttributesStr) {
        assert workFlowAttributesStr : "No workflowattributes provided"

        workFlowAttributesStr.split('\\|').collectEntries { attrKeyValue ->
            def(key, value) = attrKeyValue.trim().split(':', 2)
            assert key != null && value != null : "Invalid key value attribute pair: key: $key, value: $value"
            key = key.trim()
            value = value.trim()
            [(key):value]
        }
    }

    /**
     * From the script commandline passed in from the AppD script should be a list of the attributes that are to
     * be passed to the workflow. The format should be key1:type:value1|key2:value2| ...
     * This is needed where we don't know nothing about the flow and the the type needs to be embedded
     * @param workFlowAttributesStr
     * @return - Map containing key (type:value) pairs
     */
    public Map extractGenericWorkflowParams(String workFlowAttributesStr) {
        assert workFlowAttributesStr : "No workflowattributes provided"

        workFlowAttributesStr.split('\\|').collectEntries { attrKeyValue ->
            def(key, type, value) = attrKeyValue.trim().split(':', 3)
            assert key != null && value != null : "Invalid key value type tuple: key: $key, type, $type, value: $value"
            [key, type, value].each { it = it.trim()}
            [(key):[type,value]]
        }
    }

/************************** Test Specific **************************/
    public void setClientUpForTest(def client) {
        TLSClientParameters params = new TLSClientParameters()
        params.setDisableCNCheck(true)
        params.setUseHttpsURLConnectionDefaultSslSocketFactory(true)

        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setAllowChunking(false);
        policy.setAutoRedirect(true);
        policy.setConnection(ConnectionType.KEEP_ALIVE);
        policy.setContentType("application/soap+xml; charset=UTF-8")

        HTTPConduit httpC =  client.conduit as HTTPConduit
        httpC.client = policy
        // httpC.setTlsClientParameters(params)

        // Add loggers
        log.info "***** Adding Loggers"
        client.getInInterceptors().add(new LoggingInInterceptor());
        client.getOutInterceptors().add(new LoggingOutInterceptor());
    }
}

