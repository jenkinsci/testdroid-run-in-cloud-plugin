package com.testdroid.jenkins.auth;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.testdroid.jenkins.Messages;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.util.Secret;

@NameWith(BitbarApiKey.NameProvider.class)
public interface BitbarApiKey extends IBitbarCredentials {

    Secret getApiKey();

    class NameProvider extends CredentialsNameProvider<BitbarApiKey> {

        @NonNull
        @Override
        public String getName(BitbarApiKey c) {
            String description = Util.fixEmptyAndTrim(c.getDescription());
            return Messages.BITBAR_API_KEY() + (description != null ? " (" + description + ")" : " id: " + c.getId());
        }
    }
}
