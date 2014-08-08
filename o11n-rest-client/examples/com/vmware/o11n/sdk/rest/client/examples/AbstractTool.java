package com.vmware.o11n.sdk.rest.client.examples;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.vmware.o11n.sdk.rest.client.DefaultVcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.SsoAuthenticator;
import com.vmware.o11n.sdk.rest.client.VcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.authentication.Authentication;
import com.vmware.o11n.sdk.rest.client.authentication.UsernamePasswordAuthentication;

public abstract class AbstractTool {

    private AbstractParams params;

    public AbstractTool(AbstractParams params) {
        this.params = params;
    }
    public AbstractTool(Class<? extends AbstractParams> type, String args[]) {
        try {
            params = type.newInstance();
            CmdLineParser parser = new CmdLineParser(params);

            try {
                parser.parseArgument(args);
            } catch (CmdLineException e) {
                System.err.println(e.getMessage());
                parser.printUsage(System.err);
                System.err.println();
                System.exit(1);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractParams> T getParams() {
        return (T) params;
    }

    protected VcoSessionFactory createSessionFactory() {
        return new DefaultVcoSessionFactory(getParams().getVco()) {
            @Override
            protected HostnameVerifier newHostnameVerifier() {
                return newUnsecureHostnameVerifier();
            }

            @Override
            protected SSLContext newSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
                return newUnsecureSSLContext();
            }
        };
    }

    public Authentication createAuthentication(VcoSessionFactory sessionFactory) throws IOException {
        Authentication auth;
        AbstractParams params = getParams();
        if (params.getSso() != null) {
            SsoAuthenticator authenticator = new SsoAuthenticator(params.getSso(), sessionFactory, 24 * 60 * 60);
            auth = authenticator.createSsoAuthentication(params.getUsername(), params.getPassword());
        } else {
            auth = new UsernamePasswordAuthentication(params.getUsername(), params.getPassword());
        }
        return auth;
    }

    public abstract void run() throws Exception;
}
