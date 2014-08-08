package com.vmware.vcopoc.utils
import java.net.URL
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.HostnameVerifier
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

import org.slf4j.*
import groovy.util.logging.Slf4j

@Slf4j
class WorkFlowUtilsHelper {

    public void getServerCerts(URL url) {
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory()
        log.info "*** Creating SSL Socket For: $url.host, $url.port"
        SSLSocket socket = (SSLSocket) factory.createSocket(url.host, url.port)
        socket.startHandshake()
        log.info "*** Handshaking Complete"
        Certificate[] serverCerts = socket.getSession().getPeerCertificates()
        log.info "*** Getting Certificate Chain"
        serverCerts.each {
            log.info "cert: pubkey: $it.publicKey"
            log.info "cert: certtype: $it.type"
        }
    }

    public void retrieveCerts(String wsdlUrl, String contextType = "TLS") {
        def serverUrl = new URL(wsdlUrl)
        registerTestCertTM(serverUrl.host, serverUrl.port)
    }

    public void retrieveCerts(String host, int port, String contextType = "TLS") {
        SSLContext sslContext = SSLContext.getInstance(contextType)
        TestTrustManager testTrustManager = new TestTrustManager()
        sslContext.init(null, [testTrustManager] as TrustManager[], null)
        SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(host, port)

        socket.getInputStream()
        socket.getSession().getPeerCertificates()
        socket.close()

        testTrustManager.certs.each {
            X509Certificate cert = it as X509Certificate
                String outputFile = cert.getSubjectDN().getName().replaceAll("[^a-zA-Z0-9-=_\\.]", "_") + ".cer";
            System.out.println("Serializing certificate to: " + outputFile);
            FileOutputStream certfos = new FileOutputStream(outputFile);
            certfos.write(cert.getEncoded());
            certfos.close();
        }
    }


    public String setVoidTM(String host, int port, String contentType = "TLS") {
        def cert
        def trustManager = [
            checkClientTrusted: { chain, authType -> },
            checkServerTrusted: { chain, authType -> cert = chain[0] },
            getAcceptedIssuers: { null }
        ] as X509TrustManager

        def hostnameVerifier = [
           verify: {a, b -> true }
        ] as HostnameVerifier

        // Install the all-trusting trust manager
        SSLContext context = SSLContext.getInstance(contentType)
        context.init(null, [trustManager] as TrustManager[], null)

        context.socketFactory.createSocket(host, port).with {
            startHandshake()
            close()
        }
        // HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory())
        // HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)

        cert.encoded.encodeBase64()
    }

    public String getServerCert(String wsdlUrl, contentType = "TLS") {
        def serverUrl = new URL(wsdlUrl)
        def certStr = setVoidTM(serverUrl.host, serverUrl.port)

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

