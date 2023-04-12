package com.testdroid.jenkins.auth;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.testdroid.jenkins.Messages;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;

@NameWith(BitbarPassword.NameProvider.class)
public interface BitbarPassword extends IBitbarCredentials {

    String getEmail();

    class NameProvider extends CredentialsNameProvider<BitbarPassword> {

        @NonNull
        @Override
        public String getName(BitbarPassword c) {
            String description = Util.fixEmptyAndTrim(c.getDescription());
            return Messages.BITBAR_PASSWORD() + (description != null ? " (" + description + ")"
                    : " user: " + c.getEmail());
        }
    }

}
