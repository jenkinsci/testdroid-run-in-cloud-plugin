package com.testdroid.jenkins.utils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.testdroid.api.APIClient;
import com.testdroid.api.APIKeyClient;
import com.testdroid.jenkins.BitbarApiKey;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.remotesupport.MachineIndependentTask;
import hudson.Extension;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;

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

    private static ApiClientAdapter createAdapter(String cloudURL, String credentialsId, Boolean skipCertCheck,
            Boolean isProxy, String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) {
        String apiKey = getApiKey(credentialsId);
        APIClient apiClient;
        if (isProxy) {
            HttpHost proxy = proxyPort != null
                    ? new HttpHost(proxyHost, proxyPort, "http")
                    : new HttpHost(proxyHost);

            apiClient = StringUtils.isBlank(proxyUser)
                    ? new APIKeyClient(cloudURL, apiKey, skipCertCheck)
                    : new APIKeyClient(cloudURL, apiKey, proxy, proxyUser, proxyPassword, skipCertCheck);
        } else {
            apiClient = new APIKeyClient(cloudURL, apiKey, skipCertCheck);
        }
        return new ApiClientAdapter(apiClient);
    }

    private static String getApiKey(String credentialsId) {
        if (credentialsId == null) {
            return EMPTY;
        }
        List<BitbarApiKey> credentialsList = CredentialsProvider.lookupCredentials(
                BitbarApiKey.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList());
        return Optional.ofNullable(
                CredentialsMatchers.firstOrNull(credentialsList, CredentialsMatchers.withId(credentialsId)))
                .map(c -> c.getApiKey().getPlainText()).orElse(EMPTY);
    }
}
