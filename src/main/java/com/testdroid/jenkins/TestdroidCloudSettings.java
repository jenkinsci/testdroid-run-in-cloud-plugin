package com.testdroid.jenkins;

import com.testdroid.api.APIException;
import com.testdroid.api.model.APINotificationEmail;
import com.testdroid.jenkins.remotesupport.MachineIndependentTask;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestdroidCloudSettings implements Describable<TestdroidCloudSettings> {

    private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

    @Override
    public Descriptor<TestdroidCloudSettings> getDescriptor() {
        return Jenkins.getActiveInstance().getDescriptorByType(DescriptorImpl.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestdroidCloudSettings> {

        private static final String DEFAULT_CLOUD_URL = "https://cloud.bitbar.com";

        String cloudUrl;

        String newCloudUrl;

        String email;

        String password;

        boolean isProxy;

        boolean noCheckCertificate;

        String notificationEmail = "";

        String notificationEmailType = String.valueOf(APINotificationEmail.Type.ALWAYS);

        boolean privateInstanceState;

        String proxyHost;

        String proxyPassword;

        Integer proxyPort;

        String proxyUser;

        public DescriptorImpl() {
            super();
            load();
        }

        DescriptorImpl(String email, String password) {
            this();
            this.email = email;
            this.password = password;
        }

        DescriptorImpl(String cloudUrl, String email, String password) {
            this(email, password);
            this.cloudUrl = cloudUrl;
        }

        /**
         * Recreate a CloudSettings object from a serialized task.
         * Note: this is not a full set of params, which is ok... mostly
         */
        public DescriptorImpl(MachineIndependentTask task) {
            this.cloudUrl = task.cloudUrl;
            this.email = task.user;
            this.password = task.password;
            this.isProxy = task.isProxy;
            this.noCheckCertificate = task.noCheckCertificate;
            this.privateInstanceState = task.privateInstance;
            this.proxyHost = task.proxyHost;
            this.proxyPassword = task.proxyPassword;
            this.proxyPort = task.proxyPort;
            this.proxyUser = task.proxyUser;
        }

        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }

        @Override
        public void save() {
            this.password = Secret.fromString(this.password).getEncryptedValue();
            this.proxyPassword = Secret.fromString(this.proxyPassword).getEncryptedValue();
            TestdroidApiUtil.createGlobalApiClient(this);
            super.save();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            req.bindParameters(this);
            super.configure(req, json);
            save();
            return true;
        }

        public FormValidation doAuthorize(
                @QueryParameter String email, @QueryParameter String password, @QueryParameter String cloudUrl,
                @QueryParameter boolean privateInstanceState, @QueryParameter boolean noCheckCertificate,
                @QueryParameter boolean isProxy, @QueryParameter String proxyHost, @QueryParameter Integer proxyPort,
                @QueryParameter String proxyUser, @QueryParameter String proxyPassword) {

            this.email = email;
            this.password = password;
            this.cloudUrl = cloudUrl;
            this.privateInstanceState = privateInstanceState;
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
                this.password = null;
                load();
                LOGGER.log(Level.WARNING, Messages.ERROR_API());
                return FormValidation.error(e.getLocalizedMessage());
            }
        }

        public ListBoxModel doFillNotificationEmailTypeItems() {
            ListBoxModel emailNotificationTypes = new ListBoxModel();

            emailNotificationTypes.add(Messages.ALWAYS(), APINotificationEmail.Type.ALWAYS.name());
            emailNotificationTypes.add(Messages.ON_FAILURE_ONLY(), APINotificationEmail.Type.ON_FAILURE.name());

            return emailNotificationTypes;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof DescriptorImpl)) {
                return false;
            }

            DescriptorImpl other = (DescriptorImpl) obj;
            boolean stringParamsMatch = StringUtils.equals(cloudUrl, other.cloudUrl) &&
                    StringUtils.equals(newCloudUrl, other.newCloudUrl) &&
                    StringUtils.equals(email, other.email) &&
                    StringUtils.equals(password, other.password) &&
                    StringUtils.equals(notificationEmail, other.notificationEmail) &&
                    StringUtils.equals(notificationEmailType, other.notificationEmailType) &&
                    StringUtils.equals(proxyHost, other.proxyHost) &&
                    StringUtils.equals(proxyPassword, other.proxyPassword) &&
                    StringUtils.equals(proxyUser, other.proxyUser);

            return stringParamsMatch && isProxy == other.isProxy &&
                    proxyPort == other.proxyPort &&
                    noCheckCertificate == other.noCheckCertificate &&
                    privateInstanceState == other.privateInstanceState;
        }

        public String getEmail() {
            return email;
        }


        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * Returns password in decrypted form
         */

        public String getPassword() {
            return Secret.fromString(this.password).getPlainText();
        }


        public void setPassword(String password) {
            this.password = password;
        }


        public boolean getPrivateInstanceState() {
            return privateInstanceState;
        }


        public void setPrivateInstanceState(boolean privateInstanceState) {
            this.privateInstanceState = privateInstanceState;
        }

        /**
         * Get the cloud URL that should be used by this config.
         */
        public String getActiveCloudUrl() {
            if (privateInstanceState && StringUtils.isNotBlank(newCloudUrl)) {
                return newCloudUrl;
            }
            if (privateInstanceState && StringUtils.isNotBlank(cloudUrl)) {
                return cloudUrl;
            }
            return DEFAULT_CLOUD_URL;
        }

        public String getCloudUrl() {
            return cloudUrl;
        }

        public void setCloudUrl(String cloudUrl) {
            this.cloudUrl = cloudUrl;
        }

        public String getNewCloudUrl() {
            return newCloudUrl;
        }

        public void setNewCloudUrl(String newCloudUrl) {
            this.newCloudUrl = newCloudUrl;
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


        public String getNotificationEmail() {
            return notificationEmail;
        }


        public void setNotificationEmail(String notificationEmail) {
            this.notificationEmail = notificationEmail;
        }


        public String getNotificationEmailType() {
            return notificationEmailType;
        }


        public void setNotificationEmailType(String notificationEmailType) {
            this.notificationEmailType = notificationEmailType;
        }
    }
}
