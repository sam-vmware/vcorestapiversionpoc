package com.vmware.o11n.sdk.rest.client.examples;

import java.net.URI;

import org.kohsuke.args4j.Option;

public abstract class AbstractParams {

    @Option(name = "-u", required = true)
    private String username;

    @Option(name = "-p", required = true)
    private String password;

    @Option(name = "-vco", required = true)
    private URI vco;

    @Option(name = "-sso")
    private URI sso;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVco(URI vco) {
        this.vco = vco;
    }

    public void setSso(URI sso) {
        this.sso = sso;
    }

    public String getPassword() {
        return password;
    }

    public URI getVco() {
        return vco;
    }

    public URI getSso() {
        return sso;
    }
}
