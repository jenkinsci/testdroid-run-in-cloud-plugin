package com.testdroid.jenkins.remotesupport;

import com.testdroid.jenkins.TestdroidCloudSettings;

import java.io.Serializable;

/**
 * Testdroid Run in Cloud plugin
 *
 * https://git@github.com/jenkinsci/testdroid-run-in-cloud
 *
 * Usage:
 * TODO
 *
 * @author info@bitbar.com
 */
public class MachineIndependentTask implements Serializable {

    public String cloudUrl;

    public String credentialsId;

    public boolean isProxy;

    public boolean noCheckCertificate;

    public String proxyHost;

    public String proxyPassword;

    public Integer proxyPort;

    public String proxyUser;

    public MachineIndependentTask(TestdroidCloudSettings.DescriptorImpl settings) {
        this.credentialsId = settings.getCredentialsId();
        this.noCheckCertificate = settings.getNoCheckCertificate();
        this.cloudUrl = settings.getCloudUrl();
        this.isProxy = settings.getIsProxy();
        this.proxyHost = settings.getProxyHost();
        this.proxyPort = settings.getProxyPort();
        this.proxyUser = settings.getProxyUser();
        this.proxyPassword = settings.getProxyPassword();
    }
}
