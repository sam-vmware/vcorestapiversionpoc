package com.vmware.o11n.sdk.rest.client.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.kohsuke.args4j.Option;

import com.vmware.o11n.sdk.rest.client.DefaultVcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.VcoSession;
import com.vmware.o11n.sdk.rest.client.VcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.authentication.Authentication;
import com.vmware.o11n.sdk.rest.client.services.UtilService;
import com.vmware.o11n.sdk.rest.client.stubs.SupportedApiVersionsList;
import com.vmware.o11n.sdk.rest.client.stubs.Version;

class GetVersionOverCustomSSLParams extends AbstractParams {
    @Option(name = "-keystore",
            required = true)
    private File keystore;

    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @Option(name = "-keystorePassword")
    private String keystorePassword;

    public File getKeystore() {
        return keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }
}

public class GetVersionOverCustomSSL extends AbstractTool {

    public GetVersionOverCustomSSL(String[] args) {
        super(GetVersionOverCustomSSLParams.class, args);
    }

    @Override
    protected VcoSessionFactory createSessionFactory() {
        return new DefaultVcoSessionFactory(getParams().getVco()) {
            @Override
            protected HostnameVerifier newHostnameVerifier() {
                return newUnsecureHostnameVerifier();//verify all host
            }

            @Override
            protected SSLContext newSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
                GetVersionOverCustomSSLParams params = getParams();
                SSLContext ctx = null;
                try {
                    ctx = SSLContext.getInstance("TLS");
                    TrustManager[] trustManagers = getTrustManagers(KeyStore.getDefaultType(), new FileInputStream(
                            params.getKeystore()), params.getKeystorePassword());
                    ctx.init(null, trustManagers, null);
                } catch (Exception e) {
                    throw new RuntimeException("Error initializing SSL context with the provided keystore: "
                            + e.getMessage(), e);
                }
                return ctx;
            }
        };
    }

    private TrustManager[] getTrustManagers(String trustStoreType, InputStream trustStoreFile, String trustStorePassword)
            throws Exception {
        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        trustStore.load(trustStoreFile, trustStorePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    @Override
    public void run() throws Exception {
        VcoSessionFactory sessionFactory = createSessionFactory();
        Authentication auth = createAuthentication(sessionFactory);
        VcoSession session = sessionFactory.newSession(auth);

        UtilService utilService = new UtilService(session);

        SupportedApiVersionsList supportedApiVersions = utilService.getSupportedApiVersions();

        for (Version version : supportedApiVersions.getVersion()) {
            System.out.println("Supported API version : " + version.getValue() + ", is latest: " + version.isLatest());
        }

    }

    public static void main(String[] args) throws Exception {
        new GetVersionOverCustomSSL(args).run();
    }

}
