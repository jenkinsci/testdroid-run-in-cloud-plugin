package com.testdroid.jenkins.utils;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.DefaultAPIClient;
import com.testdroid.api.model.APIUser;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;

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
public class TestdroidApiUtil {

    private static final String CLOUD_ENDPOINT = "https://cloud.testdroid.com";

    private static final Logger LOGGER = Logger.getLogger(TestdroidApiUtil.class.getName());

    private static TestdroidApiUtil instance;

    private APIClient client;

    private String cloudUrl;

    private String email;

    private boolean isProxy;

    private boolean noCheckCertificate;

    private String password;

    private String proxyHost;

    private String proxyPassword;

    private Integer proxyPort;

    private String proxyUser;

    private TestdroidApiUtil(
            String email, String password, String cloudUrl,
            boolean privateInstanceState, boolean noCheckCertificate,
            boolean isProxy, String proxyHost, Integer proxyPort,
            String proxyUser, String proxyPassword) {
        this.email = email;
        this.password = password;
        this.cloudUrl = (!privateInstanceState || StringUtils.isEmpty(cloudUrl)) ? CLOUD_ENDPOINT : cloudUrl;
        this.noCheckCertificate = noCheckCertificate;
        this.isProxy = isProxy;

        if (isProxy) {
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPassword = proxyPassword;
        }
    }

    public static TestdroidApiUtil init(
            String email, String password, String cloudUrl,
            boolean privateInstanceState, boolean noCheckCertificate,
            boolean isProxy, String proxyHost, Integer proxyPort,
            String proxyUser, String proxyPassword) {
        if (instance == null) {
            instance = new TestdroidApiUtil(
                    email, password, cloudUrl,
                    privateInstanceState, noCheckCertificate,
                    isProxy, proxyHost, proxyPort,
                    proxyUser, proxyPassword);
        }
        return instance;
    }

    public static void clean() {
        instance = null;
    }

    public static TestdroidApiUtil getInstance() {
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public APIClient getTestdroidAPIClient() {
        if (client == null) {
            if (isProxy) {
                HttpHost proxy = proxyPort != null ? new HttpHost(proxyHost, proxyPort, "http")
                        : new HttpHost(proxyHost);
                client = StringUtils
                        .isBlank(proxyUser) ? new DefaultAPIClient(cloudUrl, email, password, proxy, noCheckCertificate)
                        : new DefaultAPIClient(cloudUrl, email, password, proxy, proxyUser, proxyPassword,
                        noCheckCertificate);
            } else {
                client = new DefaultAPIClient(cloudUrl, email, password, noCheckCertificate);
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
}
