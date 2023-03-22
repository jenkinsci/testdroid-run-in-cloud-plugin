package com.testdroid.jenkins.auth;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

public interface IBitbarCredentials extends StandardCredentials {

    BitbarCredentials getCredentials();

}
