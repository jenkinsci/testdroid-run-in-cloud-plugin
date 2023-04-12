package com.testdroid.jenkins.auth;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.testdroid.jenkins.Messages;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

public class BitbarPasswordImpl extends BaseStandardCredentials implements BitbarPassword {

    private final String email;

    private final Secret password;

    @DataBoundConstructor
    public BitbarPasswordImpl(CredentialsScope scope, String id, String description, String email, Secret password) {
        super(scope, id, description);
        this.email = email;
        this.password = password;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public BitbarCredentials getCredentials() {
        return new BitbarCredentials(email, password);
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentials.BaseStandardCredentialsDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BITBAR_PASSWORD();
        }
    }

}
