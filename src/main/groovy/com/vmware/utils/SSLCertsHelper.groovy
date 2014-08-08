package com.vmware.utils

import groovy.util.logging.Slf4j

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Slf4j
@Singleton
class SSLCertsHelper {

    public String getServerCert(String host, int port, String contentType = "TLS") {
        def cert
        def trustManager = [
                checkClientTrusted: { chain, authType -> },
                checkServerTrusted: { chain, authType -> cert = chain[0] },
                getAcceptedIssuers: { null }
        ] as X509TrustManager

        def hostnameVerifier = [
                verify: { a, b -> true }
        ] as HostnameVerifier

        // Install the all-trusting trust manager
        SSLContext context = SSLContext.getInstance(contentType)
        context.init(null, [trustManager] as TrustManager[], null)

        context.socketFactory.createSocket(host, port).with {
            startHandshake()
            close()
        }

        cert.encoded.encodeBase64()
    }

    public String getServerCert(String wsdlUrl, contentType = "TLS") {
        def serverUrl = new URL(wsdlUrl)
        def certStr = getServerCert(serverUrl.host, serverUrl.port)

        assert certStr
        def fullCertStr = new StringWriter()
        fullCertStr << "-----BEGIN CERTIFICATE-----" << newLine
        fullCertStr << certStr << newLine
        fullCertStr << "-----END CERTIFICATE-----"

        fullCertStr
    }

    String getNewLine() {
        System.getProperty('line.separator')
    }
}

