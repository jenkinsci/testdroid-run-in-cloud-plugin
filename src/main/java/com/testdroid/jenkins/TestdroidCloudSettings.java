package com.testdroid.jenkins;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.testdroid.api.APIException;
import com.testdroid.jenkins.auth.IBitbarCredentials;
import com.testdroid.jenkins.auth.TestdroidApiUtil;
import com.testdroid.jenkins.remotesupport.MachineIndependentTask;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class TestdroidCloudSettings implements Describable<TestdroidCloudSettings> {

    private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

    @Override
    public Descriptor<TestdroidCloudSettings> getDescriptor() {
        return Jenkins.get().getDescriptorByType(DescriptorImpl.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestdroidCloudSettings> {

        private static final String DEFAULT_CLOUD_URL = "https://cloud.bitbar.com";

        String cloudUrl;

        String credentialsId;

        boolean isProxy;

        boolean noCheckCertificate;

        String proxyHost;

        String proxyPassword;

        Integer proxyPort;

        String proxyUser;

        public DescriptorImpl() {
            super();
            load();
        }

        /**
         * Recreate a CloudSettings object from a serialized task.
         * Note: this is not a full set of params, which is ok... mostly
         */
        public DescriptorImpl(MachineIndependentTask task) {
            this.cloudUrl = task.cloudUrl;
            this.credentialsId = task.credentialsId;
            this.isProxy = task.isProxy;
            this.noCheckCertificate = task.noCheckCertificate;
            this.proxyHost = task.proxyHost;
            this.proxyPassword = task.proxyPassword;
            this.proxyPort = task.proxyPort;
            this.proxyUser = task.proxyUser;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }

        @Override
        public void save() {
            this.proxyPassword = Secret.fromString(this.proxyPassword).getEncryptedValue();
            TestdroidApiUtil.refreshApiClient(this);
            super.save();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            req.bindParameters(this);
            super.configure(req, json);
            save();
            return true;
        }

        @POST
        public FormValidation doAuthorize(
                @QueryParameter String credentialsId, @QueryParameter String cloudUrl,
                @QueryParameter boolean noCheckCertificate,
                @QueryParameter boolean isProxy, @QueryParameter String proxyHost, @QueryParameter Integer proxyPort,
                @QueryParameter String proxyUser, @QueryParameter String proxyPassword) {
            Objects.requireNonNull(Jenkins.getInstanceOrNull()).checkPermission(Jenkins.ADMINISTER);
            this.credentialsId = credentialsId;
            this.cloudUrl = cloudUrl;
            this.noCheckCertificate = noCheckCertificate;
            this.isProxy = isProxy;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPassword = proxyPassword;

            try {
                TestdroidApiUtil.createApiClient(this).tryValidateConfig();
                save();
                return FormValidation.ok(Messages.AUTHORIZATION_OK());
            } catch (APIException e) {
                load();
                LOGGER.log(Level.WARNING, Messages.ERROR_API());
                return FormValidation.error(e.getLocalizedMessage());
            }
        }

        public ListBoxModel doFillCredentialsIdItems() {
            ListBoxModel credentials = new ListBoxModel();
            credentials.add(new ListBoxModel.Option(EMPTY, EMPTY));

            CredentialsProvider
                    .lookupCredentials(IBitbarCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList())
                    .forEach(c -> credentials.add(CredentialsNameProvider.name(c), c.getId()));
            return credentials;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DescriptorImpl that = (DescriptorImpl) o;
            return isProxy == that.isProxy &&
                    noCheckCertificate == that.noCheckCertificate &&
                    Objects.equals(cloudUrl, that.cloudUrl) &&
                    Objects.equals(credentialsId, that.credentialsId) &&
                    Objects.equals(proxyHost, that.proxyHost) &&
                    Objects.equals(proxyPassword, that.proxyPassword) &&
                    Objects.equals(proxyPort, that.proxyPort) &&
                    Objects.equals(proxyUser, that.proxyUser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cloudUrl, credentialsId, isProxy, noCheckCertificate, proxyHost,
                            proxyPassword, proxyPort, proxyUser);
        }

        public String getCloudUrl() {
            if (StringUtils.isBlank(cloudUrl)) {
                cloudUrl = DEFAULT_CLOUD_URL;
            }
            return cloudUrl;
        }

        public void setCloudUrl(String cloudUrl) {
            this.cloudUrl = cloudUrl;
        }


        public Boolean getNoCheckCertificate() {
            return noCheckCertificate;
        }


        public void setNoCheckCertificate(Boolean noCheckCertificate) {
            this.noCheckCertificate = noCheckCertificate;
        }


        public Boolean getIsProxy() {
            return isProxy;
        }


        public void setIsProxy(Boolean isProxy) {
            this.isProxy = isProxy;
        }


        public String getProxyHost() {
            return proxyHost;
        }


        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }


        public Integer getProxyPort() {
            return proxyPort;
        }


        public void setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
        }


        public String getProxyUser() {
            return proxyUser;
        }


        public void setProxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
        }

        /**
         * Returns proxy password in decrypted form
         */
        public String getProxyPassword() {
            return Secret.fromString(this.proxyPassword).getPlainText();
        }


        public void setProxyPassword(String proxyPassword) {
            this.proxyPassword = proxyPassword;
        }

        public String getCredentialsId() {
            return credentialsId;
        }

        public void setCredentialsId(String credentialsId) {
            this.credentialsId = credentialsId;
        }
    }
}
