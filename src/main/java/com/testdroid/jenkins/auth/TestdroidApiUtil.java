package com.testdroid.jenkins.auth;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIKeyClient;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.remotesupport.MachineIndependentTask;
import com.testdroid.jenkins.utils.ApiClientAdapter;
import hudson.Extension;
import hudson.util.Secret;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.testdroid.jenkins.utils.BitbarCredentialsUtils.getBitbarCredentials;

@Extension
public class TestdroidApiUtil {

    private static ApiClientAdapter apiClientAdapter;

    public static ApiClientAdapter getGlobalApiClient() {
        if (apiClientAdapter == null) {
            refreshApiClient(new TestdroidCloudSettings.DescriptorImpl());
        }
        return apiClientAdapter;
    }

    public static void refreshApiClient(TestdroidCloudSettings.DescriptorImpl settings) {
        apiClientAdapter = createApiClientAdapter(settings);
    }

    public static ApiClientAdapter createApiClientAdapter(TestdroidCloudSettings.DescriptorImpl settings) {
        return createApiClientAdapter(settings.getCloudUrl(), settings.getCredentialsId(),
                settings.getNoCheckCertificate(),
                settings.getIsProxy(), settings.getProxyHost(), settings.getProxyPort(), settings.getProxyUser(),
                settings.getProxyPassword(), null);
    }

    public static ApiClientAdapter createApiClientAdapter(MachineIndependentTask mit) {
        return createApiClientAdapter(mit.cloudUrl, mit.credentialsId, mit.noCheckCertificate, mit.isProxy,
                mit.proxyHost,
                mit.proxyPort, mit.proxyUser, mit.proxyPassword, mit.credentials);
    }

    private static ApiClientAdapter createApiClientAdapter(
            String cloudURL, String credentialsId, Boolean skipCertCheck, Boolean isProxy, String proxyHost,
            Integer proxyPort, String proxyUser, String proxyPassword, IBitbarCredentials credentialsWrapper) {
        BitbarCredentials credentials;
        if (credentialsWrapper == null) {
            credentials = getBitbarCredentials(credentialsId);
        } else {
            credentials = credentialsWrapper.getCredentials();
        }

        APIClient apiClient;
        if (Boolean.TRUE.equals(isProxy)) {
            int port = proxyPort != null ? proxyPort : 80;
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
            if (StringUtils.isNotBlank(proxyUser)) {
                apiClient = new APIKeyClient(cloudURL, Secret.toString(credentials.getApiKey()), proxy, proxyUser,
                        proxyPassword, skipCertCheck);
            } else {
                apiClient = new APIKeyClient(cloudURL, Secret.toString(credentials.getApiKey()), proxy, skipCertCheck);
            }
        } else {
            apiClient = new APIKeyClient(cloudURL, Secret.toString(credentials.getApiKey()), skipCertCheck);
        }

        return new ApiClientAdapter(apiClient);
    }
}
