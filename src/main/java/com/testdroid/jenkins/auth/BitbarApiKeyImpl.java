package com.testdroid.jenkins.auth;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.testdroid.jenkins.Messages;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

public final class BitbarApiKeyImpl extends BaseStandardCredentials implements BitbarApiKey {

    private final Secret apiKey;

    @DataBoundConstructor
    public BitbarApiKeyImpl(CredentialsScope scope, String id, String description, Secret apiKey) {
        super(scope, id, description);
        this.apiKey = apiKey;
    }

    @Override
    public Secret getApiKey() {
        return apiKey;
    }

    @Override
    public BitbarCredentials getCredentials() {
        return new BitbarCredentials(apiKey);
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BITBAR_API_KEY();
        }
    }
}
