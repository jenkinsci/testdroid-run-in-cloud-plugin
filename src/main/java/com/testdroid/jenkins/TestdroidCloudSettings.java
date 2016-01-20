package com.testdroid.jenkins;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.model.APINotificationEmail;
import com.testdroid.api.model.APIUser;
import com.testdroid.jenkins.utils.ResultWaiter;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Testdroid Run in Cloud plugin
 *
 * https://git@github.com/bitbar/testdroid-run-in-cloud
 *
 * Usage:
 * @TODO
 *
 * @author info@bitbar.com
 */

@ExportedBean(defaultVisibility = 1)
public class TestdroidCloudSettings extends Plugin implements Describable<TestdroidCloudSettings>, Saveable,
        ModelObject {

    public static final String CLOUD_ENDPOINT = "https://cloud.testdroid.com";

    private static final Logger LOGGER = Logger.getLogger(TestdroidCloudSettings.class.getName());

    public static DescriptorImpl DESCRIPTOR;

    transient Semaphore semaphore = new Semaphore(1);

    public TestdroidCloudSettings() {
        super();
    }

    /**
     * Returns the instance of this plugin.
     *
     * @return TestdroidCloudSettings instance
     */
    public static TestdroidCloudSettings getInstance() {
        return Hudson.getInstance().getPlugin(TestdroidCloudSettings.class);

    }

    public static DescriptorImpl descriptor() {
        return Hudson.getInstance().getDescriptorByType(TestdroidCloudSettings.DescriptorImpl.class);
    }

    public Api getApi() {
        return new TestdroidRunInCloudApi(this);
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    @Override
    public Descriptor<TestdroidCloudSettings> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String getDisplayName() {
        return Messages.PLUGIN_NAME();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestdroidCloudSettings> {

        private String cloudUrl;

        private String newCloudUrl;

        private String email;

        private boolean isProxy;

        private boolean noCheckCertificate;

        private String notificationEmail = "";

        private String notificationEmailType = String.valueOf(APINotificationEmail.Type.ALWAYS);

        private String password;

        private boolean privateInstanceState;

        private String proxyHost;

        private String proxyPassword;

        private Integer proxyPort;

        private String proxyUser;

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }

        @Override
        public void save() {
            this.password = Secret.fromString(this.password).getEncryptedValue();
            this.proxyPassword = Secret.fromString(this.proxyPassword).getEncryptedValue();
            TestdroidApiUtil.clean();
            super.save();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            req.bindParameters(this);
            super.configure(req, json);
            save();
            return true;
        }

        private APIClient getTestdroidAPIClient() {
            return TestdroidApiUtil.isInitialized()
                    ? TestdroidApiUtil.getInstance().getTestdroidAPIClient()
                    : TestdroidApiUtil.init(
                    email, getPassword(), cloudUrl, privateInstanceState,
                    noCheckCertificate, isProxy, proxyHost, proxyPort,
                    proxyUser, getProxyPassword()).getTestdroidAPIClient();
        }

        public APIUser getUser() throws APIException {
            APIUser user;

            try {
                user = getTestdroidAPIClient().me();
            } catch (APIException e) {
                LOGGER.log(Level.INFO, "ApiException occurred during get user from client. Client will be recreated");
                //if sth happen to cached client(probably problem with refreshing access token) client is recreated
                TestdroidApiUtil.clean();
                user = getTestdroidAPIClient().me();
            }

            return user;
        }

        @Exported
        public FormValidation doAuthorize(
                @QueryParameter String email, @QueryParameter String password, @QueryParameter String cloudUrl,
                @QueryParameter boolean privateInstanceState, @QueryParameter boolean noCheckCertificate,
                @QueryParameter boolean isProxy, @QueryParameter String proxyHost, @QueryParameter Integer proxyPort,
                @QueryParameter String proxyUser, @QueryParameter String proxyPassword) {

            FormValidation validation = null;
            TestdroidApiUtil.clean();

            try {
                this.email = email;
                this.password = password;
                this.cloudUrl = cloudUrl;
                this.noCheckCertificate = noCheckCertificate;
                this.isProxy = isProxy;
                this.privateInstanceState = privateInstanceState;

                if (isProxy) {
                    this.proxyHost = proxyHost;
                    this.proxyPort = proxyPort;
                    this.proxyUser = proxyUser;
                    this.proxyPassword = proxyPassword;
                }

                APIUser user = getTestdroidAPIClient().me();

                save();

                if (user != null) {
                    validation = FormValidation.ok(Messages.AUTHORIZATION_OK());
                }
            } catch (APIException e) {
                validation = FormValidation.error(e.getLocalizedMessage());
                this.password = null;
                load();
                LOGGER.log(Level.WARNING, Messages.ERROR_API());
            }

            return validation;
        }

        @Exported
        public ListBoxModel doFillNotificationEmailTypeItems() {
            ListBoxModel emailNotificationTypes = new ListBoxModel();

            emailNotificationTypes.add(Messages.ALWAYS(), APINotificationEmail.Type.ALWAYS.toString());
            emailNotificationTypes.add(Messages.ON_FAILURE_ONLY(), APINotificationEmail.Type.ON_FAILURE.toString());

            return emailNotificationTypes;
        }

        @Exported
        public String getEmail() {
            return email;
        }

        @Exported
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * Returns password in decrypted form
         */
        @Exported
        public String getPassword() {
            return Secret.fromString(this.password).getPlainText();
        }

        @Exported
        public void setPassword(String password) {
            this.password = password;
        }

        @Exported
        public boolean getPrivateInstanceState() {
            return privateInstanceState;
        }

        @Exported
        public void setPrivateInstanceState(boolean privateInstanceState) {
            this.privateInstanceState = privateInstanceState;
        }

        @Exported
        public String getCloudUrl() {
            return StringUtils.isBlank(cloudUrl) ? CLOUD_ENDPOINT : cloudUrl;
        }

        @Exported
        public void setCloudUrl(String cloudUrl) {
            this.cloudUrl = cloudUrl;
        }

        @Exported
        public String getNewCloudUrl() {
            return newCloudUrl;
        }

        @Exported
        public void setNewCloudUrl(String newCloudUrl) {
            this.newCloudUrl = newCloudUrl;
        }

        @Exported
        public Boolean getNoCheckCertificate() {
            return noCheckCertificate;
        }

        @Exported
        public void setNoCheckCertificate(Boolean noCheckCertificate) {
            this.noCheckCertificate = noCheckCertificate;
        }

        @Exported
        public Boolean getIsProxy() {
            return isProxy;
        }

        @Exported
        public void setIsProxy(Boolean isProxy) {
            this.isProxy = isProxy;
        }

        @Exported
        public String getProxyHost() {
            return proxyHost;
        }

        @Exported
        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }

        @Exported
        public Integer getProxyPort() {
            return proxyPort;
        }

        @Exported
        public void setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
        }

        @Exported
        public String getProxyUser() {
            return proxyUser;
        }

        @Exported
        public void setProxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
        }

        /**
         * Returns proxy password in decrypted form
         */
        @Exported
        public String getProxyPassword() {
            return Secret.fromString(this.proxyPassword).getPlainText();
        }

        @Exported
        public void setProxyPassword(String proxyPassword) {
            this.proxyPassword = proxyPassword;
        }

        @Exported
        public String getNotificationEmail() {
            return notificationEmail;
        }

        @Exported
        public void setNotificationEmail(String notificationEmail) {
            this.notificationEmail = notificationEmail;
        }

        @Exported
        public String getNotificationEmailType() {
            return notificationEmailType;
        }

        @Exported
        public void setNotificationEmailType(String notificationEmailType) {
            this.notificationEmailType = notificationEmailType;
        }
    }

    public class TestdroidRunInCloudApi extends Api {

        public TestdroidRunInCloudApi(TestdroidCloudSettings thisPlugin) {
            super(thisPlugin);
        }

        @Override
        public void doJson(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
            LOGGER.log(Level.INFO, "rest call");
            if (req.getMethod().toLowerCase().equals("post")) {
                ResultWaiter.getInstance().notifyWaitingObject(Long.parseLong(req.getParameter("testRunId")));
            }
        }
    }
}
