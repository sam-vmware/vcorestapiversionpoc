package com.vmware.vcopoc.utils
/**
 * Basic utility class which performs webservice method invocation
 * author: samueldoyle
 */

/*@Grapes([
@Grab('org.codehaus.groovy.modules:groovyws:0.5.2'),
@GrabExclude('org.codehaus.groovy:groovy')
])*/

import org.slf4j.*
import groovy.util.logging.Slf4j
import groovy.transform.Canonical
import groovyx.net.ws.WSClient
import com.vmware.vcopoc.utils.WorkFlowUtils

@Slf4j
@Canonical
class ServiceMethodInvoker {

    String wsdlUrl
    String userName
    String passWord

    def proxy = null

    /**
     * Call a webservice
     * @param serviceName - the webservice method to invoke
     * @param paramsMap - the parameters to pass to the call key value pair, key does not matter but order does
     * @return - the webservice method call resulte
     */
    def callWebService(String serviceName, Map paramsMap = [:]) {
        assert wsdlUrl && userName && passWord && serviceName
        createProxy()
        log.info "Calling service: $serviceName, params: $paramsMap"
        def paramsList = paramsMap.values().toList()

        /*if (! proxy.metaClass.getMetaMethod(serviceName)) {
            log.error "!!!!! ERROR: proxy missing method $serviceName !!!!!"
            log.error "Has the following:"
            proxy.metaClass.methods.each {
                log.error "***** method: $it.name"
            }
        }*/
        def result = proxy."$serviceName"(*paramsList)

        log.info "Returning result: $result"
        result
    }

    /**
     * Creates an instance of a type that has been generated from the wsdl associated with this instance
     * @param type - a new instance oof whatever type
     * @return - a new instance of that type
     */
    def createType(String type) {
        assert type
        createProxy()

        log.info "Creating type: $type"
        proxy.create(type)
    }

    private void createProxy() {
        if (!proxy) {
            // !! TESTING REMOVEME !!
            // def serverUrl = new URL(wsdlUrl)
            // WorkFlowUtils.instance.setVoidTM(serverUrl.host, serverUrl.port)

            Map<String, String> mapClient = [
                "https.keystore":"",
                "https.keystore.pass":"",
                "https.truststore":"server.jks",
                "https.truststore.pass":"password"
            ]
            WSClient.metaClass.getCxfClient = { ->
                delegate.client
            }

            // Use GroovyWS to create a client proxy.
            proxy = new WSClient(wsdlUrl, this.class.classLoader)
            proxy.setSSLProperties(mapClient)
            proxy.setBasicAuthentication(userName, passWord)

            // Make sure all required classes are created and available.
            proxy.initialize()

            WorkFlowUtils.instance.setClientUpForTest(proxy.cxfClient)
        }
    }

    public parsedWsdl() {

        def nsxsd = new groovy.xml.Namespace("http://www.w3.org/2001/XMLSchema", 'xsd')
        def nswsdl = new groovy.xml.Namespace("http://schemas.xmlsoap.org/wsdl/", 'wsdl')

        def wsdlDocument = new XmlParser().parse(new URL(wsdlUrl).text);
        def targetNamespace = wsdlDocument.'@targetNamespace';

        def imports = wsdlDocument[nswsdl.types][nsxsd.schema][nsxsd.import];
        imports.each { theimport ->
            System.out.println(theimport.'@schemaLocation');
            System.out.println(theimport.'@namespace');
        }
    }
}

