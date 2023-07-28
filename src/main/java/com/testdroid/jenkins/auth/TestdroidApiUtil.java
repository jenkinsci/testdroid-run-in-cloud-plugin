package com.testdroid.jenkins.auth;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIKeyClient;
import com.testdroid.api.DefaultAPIClient;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.remotesupport.MachineIndependentTask;
import com.testdroid.jenkins.utils.ApiClientAdapter;
import hudson.Extension;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

import static com.testdroid.jenkins.utils.BitbarCredentialsUtils.getBitbarCredentials;
import static com.testdroid.jenkins.utils.BitbarCredentialsUtils.getSecretText;

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
        apiClientAdapter = createApiClientHelper(settings);
    }

    public static ApiClientAdapter createApiClient(TestdroidCloudSettings.DescriptorImpl settings) {
        return createApiClientHelper(settings);
    }

    public static ApiClientAdapter createNewApiClient(MachineIndependentTask mit) {
        return createAdapter(mit.cloudUrl, mit.credentialsId, mit.noCheckCertificate, mit.isProxy, mit.proxyHost,
                mit.proxyPort, mit.proxyUser, mit.proxyPassword);
    }

    private static ApiClientAdapter createApiClientHelper(TestdroidCloudSettings.DescriptorImpl settings) {
        return createAdapter(settings.getCloudUrl(), settings.getCredentialsId(), settings.getNoCheckCertificate(),
                settings.getIsProxy(), settings.getProxyHost(), settings.getProxyPort(), settings.getProxyUser(),
                settings.getProxyPassword());
    }

    private static ApiClientAdapter createAdapter(
            String cloudURL, String credentialsId, Boolean skipCertCheck, Boolean isProxy, String proxyHost,
            Integer proxyPort, String proxyUser, String proxyPassword) {
        BitbarCredentials credentials = getBitbarCredentials(credentialsId);

        APIClient apiClient;
        if (Boolean.TRUE.equals(isProxy)) {
            HttpHost proxy = proxyPort != null
                    ? new HttpHost(proxyHost, proxyPort, "http")
                    : new HttpHost(proxyHost);
            if (StringUtils.isNotBlank(proxyUser)) {
                apiClient = credentials.usesApiKey() ?
                        new APIKeyClient(cloudURL, getSecretText(credentials.getApiKey()),
                                proxy, proxyUser, proxyPassword, skipCertCheck) :
                        new DefaultAPIClient(cloudURL, credentials.getEmail(), getSecretText(credentials.getPassword()),
                                proxy, proxyUser, proxyPassword, skipCertCheck);
            } else {
                apiClient = credentials.usesApiKey() ?
                        new APIKeyClient(cloudURL, getSecretText(credentials.getApiKey()), proxy, skipCertCheck) :
                        new DefaultAPIClient(cloudURL, credentials.getEmail(), getSecretText(credentials.getPassword()),
                                proxy, skipCertCheck);
            }
        } else {
            apiClient = credentials.usesApiKey() ?
                    new APIKeyClient(cloudURL, getSecretText(credentials.getApiKey()), skipCertCheck) :
                    new DefaultAPIClient(cloudURL, credentials.getEmail(), getSecretText(credentials.getPassword()));
        }
        return new ApiClientAdapter(apiClient);
    }
}
