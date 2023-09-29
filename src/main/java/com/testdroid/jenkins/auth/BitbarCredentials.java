package com.testdroid.jenkins.auth;

import hudson.util.Secret;

import java.util.Objects;

public class BitbarCredentials {

    public static final BitbarCredentials EMPTY = new BitbarCredentials();

    private Secret apiKey;

    private BitbarCredentials() {

    }

    BitbarCredentials(Secret apiKey) {
        this.apiKey = apiKey;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public boolean isEmpty() {
        return Objects.isNull(apiKey);
    }
}
