package com.vmware.vcopoc.utils

import javax.net.ssl.X509TrustManager
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/* Bogus TM to get around the self-signed cert hoops */
class TestTrustManager implements X509TrustManager {

    private final def certList = []

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
        println "checkClientTrusted: $authType"
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
        certs.each {
            System.out.println("Loading certificate " + it.getSubjectDN() + " issued by: " + it.getIssuerDN());
            certList << it
        }
    }

    public List getCerts() {
        return certList;
    }
}
