package com.testdroid.jenkins.auth;

import hudson.util.Secret;
import org.apache.commons.lang3.StringUtils;

public class BitbarCredentials {

    public static BitbarCredentials EMPTY = new BitbarCredentials();

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
        return apiKey != null && StringUtils.isNotBlank(apiKey.getPlainText());
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
}
