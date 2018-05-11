package com.testdroid.jenkins;

import com.testdroid.api.model.*;
import com.testdroid.api.model.APITestRunConfig.Scheduler;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import com.google.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Pipeline build step for Bitbar Cloud's Jenkins plugin.
 *
 * Can be invoked from pipeline like eg:
 * ...
 * steps {
 *     runInCloud(
 *         projectId: "144314736",
 *         deviceGroupId: "36085",
 *         appPath: "application.ipa",
 *         testPath: "tests.zip"
 *     )
 * }
 * ...
 */

public class PipelineCloudStep extends AbstractStepImpl {

    private static final Logger LOGGER = Logger.getLogger(PipelineCloudStep.class.getSimpleName());

    private String appPath;
    private String deviceGroupId;
    private String dataPath;
    private String testPath;
    private boolean failBuildIfThisStepFailed;
    private String keyValuePairs;
    private String language;
    private String notificationEmail = "";
    private String notificationEmailType = String.valueOf(APINotificationEmail.Type.ALWAYS);
    private String projectId;
    private String scheduler;
    private String screenshotsDirectory;
    private String testCasesSelect;
    private String testCasesValue;
    private String testRunName;
    private String testRunner;
    private String withAnnotation;
    private String withoutAnnotation;
    private String testTimeout;
    private String credentialsId;
    private String cloudUrl;

    // these variables are used to create a WaitForResultsBlock
    private boolean waitForResults;
    private String testRunStateCheckMethod; // API_CALL, HOOK_URL,
    private String hookURL;
    private String waitForResultsTimeout;
    private String resultsPath;
    private boolean downloadScreenshots;
    private boolean forceFinishAfterBreak;

    /**
     * Constructor; defined the mandatory parameters to be passed in Pipeline.
     *
     * @param projectId: Bitbar Cloud project Id
     * @param deviceGroupId: Bitbar Cloud device group Id
     */
    @DataBoundConstructor
    public PipelineCloudStep(String projectId, String deviceGroupId) {
        this.projectId = projectId;
        this.deviceGroupId = deviceGroupId;
    }

    // optional params for the plugin
    // defined in DataBoundSetters
    
    @DataBoundSetter
    public void setTestRunName(String testRunName) {
        this.testRunName = testRunName;
    }

    @DataBoundSetter
    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    @DataBoundSetter
    public void setTestRunner(String testRunner) {
        this.testRunner = testRunner;
    }

    @DataBoundSetter
    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    @DataBoundSetter
    public void setScreenshotsDirectory(String screenshotsDirectory) {
        this.screenshotsDirectory = screenshotsDirectory;
    }

    @DataBoundSetter
    public void setKeyValuePairs(String keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    @DataBoundSetter
    public void setWithAnnotation(String withAnnotation) {
        this.withAnnotation = withAnnotation;
    }

    @DataBoundSetter
    public void setWithoutAnnotation(String withoutAnnotation) {
        this.withoutAnnotation = withoutAnnotation;
    }

    @DataBoundSetter
    public void setTestCasesSelect(String testCasesSelect) {
        this.testCasesSelect = testCasesSelect;
    }

    @DataBoundSetter
    public void setTestCasesValue(String testCasesValue) {
        this.testCasesValue = testCasesValue;
    }

    @DataBoundSetter
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    @DataBoundSetter
    public void setLanguage(String language) {
        this.language = language;
    }

    @DataBoundSetter
    public void setScheduler(String scheduler) {
        this.scheduler = scheduler.toLowerCase();
    }

    @DataBoundSetter
    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    @DataBoundSetter
    public void setNotificationEmailType(String notificationEmailType) {
        this.notificationEmailType = notificationEmailType;
    }

    @DataBoundSetter
    public void setTestTimeout(String testTimeout) {
        this.testTimeout = testTimeout;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setCloudUrl(String cloudUrl) {
        this.cloudUrl = cloudUrl;
    }

    @DataBoundSetter
    public void setFailBuildIfThisStepFailed(boolean failBuildIfThisStepFailed) {
        this.failBuildIfThisStepFailed = failBuildIfThisStepFailed;
    }

    @DataBoundSetter
    public void setWaitForResults(boolean waitForResults) {
        this.waitForResults = waitForResults;
    }

    @DataBoundSetter
    public void setTestRunStateCheckMethod(String testRunStateCheckMethod) {
        this.testRunStateCheckMethod = testRunStateCheckMethod;
    }

    @DataBoundSetter
    public void setHookURL(String hookURL) {
        this.hookURL = hookURL;
    }

    @DataBoundSetter
    public void setWaitForResultsTimeout(String waitForResultsTimeout) {
        this.waitForResultsTimeout = waitForResultsTimeout;
    }

    @DataBoundSetter
    public void setResultsPath(String resultsPath) {
        this.resultsPath = resultsPath;
    }

    @DataBoundSetter
    public void setDownloadScreenshots(boolean downloadScreenshots) {
        this.downloadScreenshots = downloadScreenshots;
    }

    @DataBoundSetter
    public void setForceFinishAfterBreak(boolean forceFinishAfterBreak) {
        this.forceFinishAfterBreak = forceFinishAfterBreak;
    }


    private String getTestRunName() {
        return testRunName;
    }

    private String getAppPath() {
        return appPath;
    }

    private String getTestPath() {
        return testPath;
    }

    private String getProjectId() {
        return projectId;
    }

    private String getDeviceGroupId() {
        return deviceGroupId;
    }

    private String getTestRunner() {
        return testRunner;
    }

    private String getScreenshotsDirectory() {
        return screenshotsDirectory;
    }

    private String getKeyValuePairs() {
        return keyValuePairs;
    }

    private String getWithAnnotation() {
        return withAnnotation;
    }

    private String getWithoutAnnotation() {
        return withoutAnnotation;
    }

    private String getTestCasesSelect() {
        return testCasesSelect;
    }

    private String getTestCasesValue() {
        return testCasesValue;
    }

    private String getDataPath() {
        return dataPath;
    }

    private String getLanguage() {
        if (language == null) {
            language = String.format("%s-%s", Locale.ENGLISH.getLanguage(), Locale.ENGLISH.getCountry());
        }
        return language;
    }

    public String getScheduler() {
        if (scheduler == null) {
            scheduler = Scheduler.PARALLEL.toString();
        }
        return scheduler;
    }

    private String getNotificationEmail() {
        return notificationEmail;
    }

    private String getNotificationEmailType() {
        return notificationEmailType;
    }

    private boolean isFailBuildIfThisStepFailed() {
        return failBuildIfThisStepFailed;
    }

    private String getTestTimeout() {
        return testTimeout;
    }

    private String getCredentialsId() {
        return credentialsId;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

    private boolean isWaitForResults() {
        return waitForResults;
    }

    private String getTestRunStateCheckMethod() {
        return testRunStateCheckMethod;
    }

    private String getHookURL() {
        return hookURL;
    }

    private String getWaitForResultsTimeout() {
        return waitForResultsTimeout;
    }

    private String getResultsPath() {
        return resultsPath;
    }

    private boolean isDownloadScreenshots() {
        return downloadScreenshots;
    }

    private boolean isForceFinishAfterBreak() {
        return forceFinishAfterBreak;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(CloudStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "runInCloud";
        }

        @Override
        public String getDisplayName() {
            return "Start a run in Bitbar Cloud";
        }
    }


    public static final class CloudStepExecution extends AbstractSynchronousNonBlockingStepExecution<Boolean> {

        private static final long serialVersionUID = 1;

        @Inject
        private transient PipelineCloudStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient Run<?,?> run;

        @StepContextParameter
        private transient FilePath workspace;

        @Override
        protected Boolean run() throws Exception {
            WaitForResultsBlock waitForResultsBlock = null;
            if (step.isWaitForResults()) {
                waitForResultsBlock = new WaitForResultsBlock(
                        step.getTestRunStateCheckMethod(),
                        step.getHookURL(),
                        step.getWaitForResultsTimeout(),
                        step.getResultsPath(),
                        step.isDownloadScreenshots(),
                        step.isForceFinishAfterBreak()
                );
            }

            RunInCloudBuilder builder = new RunInCloudBuilder(
                    step.getProjectId(),
                    step.getAppPath(),
                    step.getTestPath(),
                    step.getDataPath(),
                    step.getTestRunName(),
                    step.getScheduler(),
                    step.getTestRunner(),
                    step.getDeviceGroupId(),
                    step.getLanguage(),
                    step.getNotificationEmail(),
                    step.getScreenshotsDirectory(),
                    step.getKeyValuePairs(),
                    step.getWithAnnotation(),
                    step.getWithoutAnnotation(),
                    step.getTestCasesSelect(),
                    step.getTestCasesValue(),
                    step.getNotificationEmailType(),
                    step.isFailBuildIfThisStepFailed(),
                    waitForResultsBlock,
                    step.getTestTimeout(),
                    step.getCredentialsId(),
                    step.getCloudUrl()
            );

            return builder.completeRun(run, workspace, launcher, listener);
        }
    }
}
