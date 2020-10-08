package com.testdroid.jenkins.utils;

import com.testdroid.api.APIClient;
import com.testdroid.api.DefaultAPIClient;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.remotesupport.MachineIndependentTask;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

@Extension
public class TestdroidApiUtil {

    private ApiClientAdapter apiClientAdapter;

    public static TestdroidApiUtil getInstance() {
        return Jenkins.get().getExtensionList(TestdroidApiUtil.class).stream().findFirst().get();
    }

    public static ApiClientAdapter getGlobalApiClient() {
        if (getInstance().apiClientAdapter == null) {
            createGlobalApiClient(new TestdroidCloudSettings.DescriptorImpl());
        }
        return getInstance().apiClientAdapter;
    }

    public static ApiClientAdapter createGlobalApiClient(TestdroidCloudSettings.DescriptorImpl settings) {
        getInstance().apiClientAdapter = getInstance().createApiClientHelper(settings);
        return getInstance().apiClientAdapter;
    }

    public static ApiClientAdapter createApiClient(TestdroidCloudSettings.DescriptorImpl settings) {
        return getInstance().createApiClientHelper(settings);
    }

    public static ApiClientAdapter createNewApiClient(MachineIndependentTask machineIndependentTask) {
        APIClient apiClient;

        if (machineIndependentTask.isProxy) {
            HttpHost proxy = machineIndependentTask.proxyPort != null ?
                    new HttpHost(machineIndependentTask.proxyHost,
                            machineIndependentTask.proxyPort, "http") :
                    new HttpHost(machineIndependentTask.proxyHost);

            apiClient = StringUtils.isBlank(machineIndependentTask.proxyUser) ?
                    new DefaultAPIClient(machineIndependentTask.cloudUrl,
                            machineIndependentTask.user,
                            machineIndependentTask.password, proxy,
                            machineIndependentTask.noCheckCertificate) :
                    new DefaultAPIClient(machineIndependentTask.cloudUrl,
                            machineIndependentTask.user,
                            machineIndependentTask.password, proxy,
                            machineIndependentTask.proxyUser,
                            machineIndependentTask.proxyPassword,
                            machineIndependentTask.noCheckCertificate);
        } else {
            apiClient = new DefaultAPIClient(machineIndependentTask.cloudUrl,
                    machineIndependentTask.user,
                    machineIndependentTask.password,
                    machineIndependentTask.noCheckCertificate);
        }
        return new ApiClientAdapter(apiClient);
    }

    private ApiClientAdapter createApiClientHelper(TestdroidCloudSettings.DescriptorImpl settings) {
        String cloudURL = settings.getCloudUrl();
        String email = settings.getEmail();
        String password = settings.getPassword();
        boolean dontCheckCert = settings.getNoCheckCertificate();
        APIClient apiClient;

        if (settings.getIsProxy()) {
            HttpHost proxy = settings.getProxyPort() != null ?
                    new HttpHost(settings.getProxyHost(), settings.getProxyPort(), "http") :
                    new HttpHost(settings.getProxyHost());

            apiClient = StringUtils.isBlank(settings.getProxyUser()) ?
                    new DefaultAPIClient(cloudURL, email, password, proxy, dontCheckCert) :
                    new DefaultAPIClient(cloudURL, email, password, proxy,
                            settings.getProxyUser(), settings.getProxyPassword(), dontCheckCert);
        } else {
            apiClient = new DefaultAPIClient(cloudURL, email, password, dontCheckCert);
        }
        return new ApiClientAdapter(apiClient);
    }
}
