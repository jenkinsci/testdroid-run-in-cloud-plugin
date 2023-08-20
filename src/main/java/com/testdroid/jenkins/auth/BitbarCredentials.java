package com.testdroid.jenkins.auth;

import hudson.util.Secret;
import org.apache.commons.lang3.ObjectUtils;

public class BitbarCredentials {

    public static final BitbarCredentials EMPTY = new BitbarCredentials();

    private Secret apiKey;

    private String email;

    private Secret password;

    private BitbarCredentials() {

    }

    BitbarCredentials(Secret apiKey) {
        this.apiKey = apiKey;
    }

    BitbarCredentials(String email, Secret password) {
        this.email = email;
        this.password = password;
    }

    public boolean usesApiKey() {
        return apiKey != null;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public String getEmail() {
        return email;
    }

    public Secret getPassword() {
        return password;
    }

    public boolean isEmpty() {
        return ObjectUtils.allNull(apiKey, email, password);
    }
}
