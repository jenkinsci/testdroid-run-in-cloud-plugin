package com.testdroid.jenkins.utils;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.DefaultAPIClient;
import com.testdroid.api.model.APICloudInfo;
import com.testdroid.api.model.APIRole;
import com.testdroid.api.model.APIUser;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import hudson.model.Api;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestdroidApiUtil {

    private static final Logger LOGGER = Logger.getLogger(TestdroidApiUtil.class.getName());

    private static final List<String> PAID_ROLES = new ArrayList<String>() {
        {
            add("PRIORITY_SILVER");
            add("PRIORITY_GOLD");
            add("PRIORITY_PLATINUM");
            add("PAID_RUN");
        }
    };

    private TestdroidCloudSettings.DescriptorImpl settings;

    private APIClient client;

    public TestdroidApiUtil(TestdroidCloudSettings.DescriptorImpl settings) {
        this.settings = settings;
    }

    public APIClient getTestdroidAPIClient() {
        if (client == null) {
            String cloudURL = settings.getActiveCloudUrl();
            String email = settings.getEmail();
            String password = settings.getPassword();
            boolean dontCheckCert = settings.getNoCheckCertificate();

            if (settings.getIsProxy()) {
                HttpHost proxy = settings.getProxyPort() != null ?
                        new HttpHost(settings.getProxyHost(), settings.getProxyPort(), "http") :
                        new HttpHost(settings.getProxyHost());

                client = StringUtils.isBlank(settings.getProxyUser()) ?
                        new DefaultAPIClient(cloudURL, email, password, proxy, dontCheckCert) :
                        new DefaultAPIClient(cloudURL, email, password, proxy,
                                settings.getProxyUser(), settings.getProxyPassword(), dontCheckCert);
            } else {
                client = new DefaultAPIClient(cloudURL, email, password, dontCheckCert);
            }
        }

        return client;
    }

    public String getCloudVersion() {
        try {
            APICloudInfo cloudInfo = getTestdroidAPIClient().get("/info", APICloudInfo.class);
            return cloudInfo.getVersion();
        } catch (APIException e) {
            LOGGER.log(Level.WARNING, "APIException occurred when trying to get the cloud version.");
        }

        return null;
    }

    public APIUser getUser() throws APIException {
        APIUser user;
        try {
            user = getTestdroidAPIClient().me();
        } catch (APIException e) {
            LOGGER.log(Level.INFO, "ApiException occurred during get user from client. Client will be recreated");
            //if sth happen to cached client(probably problem with refreshing access token) then we create client again
            client = null;
            user = getTestdroidAPIClient().me();
        }

        return user;
    }

    public void tryValidateConfig() throws APIException {
        if (getUser() == null) {
            throw new APIException("Couldn't retrieve Cloud user with the current settings!");
        }
    }

    public boolean isAuthenticated() {
        try {
            return getUser() != null;
        } catch (APIException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Static methods for managing instances of this class.
     */
    public static boolean isPaidUser(APIUser user) {
        if (user == null) return false;

        Date now = new Date();
        for (APIRole role : user.getRoles()) {
            if (PAID_ROLES.contains(role.getName())
                    && (role.getExpireTime() == null || role.getExpireTime().after(now))) {
                return true;
            }
        }

        return false;
    }
}
