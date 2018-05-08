package com.testdroid.jenkins.utils;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.DefaultAPIClient;
import com.testdroid.api.model.APIUser;
import com.testdroid.jenkins.TestdroidCloudSettings;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestdroidApiUtil {

    private static final Logger LOGGER = Logger.getLogger(TestdroidApiUtil.class.getName());

    private TestdroidCloudSettings.DescriptorImpl settings;

    private APIClient client;

    private TestdroidApiUtil(TestdroidCloudSettings.DescriptorImpl settings) {
        this.settings = settings;
    }

    public APIClient getTestdroidAPIClient() {
        if (client == null) {
            if (settings.getIsProxy()) {
                HttpHost proxy = settings.getProxyPort() != null ?
                        new HttpHost(settings.getProxyHost(), settings.getProxyPort(), "http") :
                        new HttpHost(settings.getProxyHost());

                client = StringUtils.isBlank(settings.getProxyUser()) ?
                        new DefaultAPIClient(settings.getActiveCloudUrl(), settings.getEmail(), settings.getPassword(), proxy, settings.getNoCheckCertificate()) :
                        new DefaultAPIClient(settings.getActiveCloudUrl(), settings.getEmail(), settings.getPassword(), proxy, settings.getProxyUser(), settings.getProxyPassword(), settings.getNoCheckCertificate());
            } else {
                client = new DefaultAPIClient(settings.getActiveCloudUrl(), settings.getEmail(), settings.getPassword(), settings.getNoCheckCertificate());
            }
        }

        return client;
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

    /**
     * Static methods for managing instances of this class.
     */

    private static TestdroidApiUtil instance;

    public static TestdroidApiUtil getInstance(TestdroidCloudSettings.DescriptorImpl settings) {
        if (instance == null || !instance.equals(settings)) {
            instance = new TestdroidApiUtil(settings);
        }
        return instance;
    }

    public static void clean() {
        instance = null;
    }

    public static void tryValidateConfig(TestdroidCloudSettings.DescriptorImpl settings) throws APIException {
        if (new TestdroidApiUtil(settings).getUser() == null) {
            throw new APIException("Couldn't retrieve Cloud user with the current settings!");
        }
    }
}
