package com.testdroid.jenkins;

import com.google.inject.Inject;
import com.testdroid.api.model.APIDevice;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static com.testdroid.jenkins.RunInCloudDescriptorHelper.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

    private String appPath;
    private String deviceGroupId;
    private String dataPath;
    private String testPath;
    private boolean failBuildIfThisStepFailed;
    private String keyValuePairs;
    private String language;
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
    private String cloudUIUrl;
    private Long frameworkId;
    private APIDevice.OsType osType;

    private WaitForResultsBlock waitForResultsBlock;

    /**
     * Constructor; defined the mandatory parameters to be passed in Pipeline.
     *
     * @param projectId Bitbar Cloud project Id
     * @param deviceGroupId Bitbar Cloud device group Id
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
        if (isNotBlank(testRunName)) {
            this.testRunName = testRunName;
        }
    }

    @DataBoundSetter
    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    @DataBoundSetter
    public void setTestRunner(String testRunner) {
        if (isNotBlank(testRunner)) {
            this.testRunner = testRunner;
        }
    }

    @DataBoundSetter
    public void setTestPath(String testPath) {
        if (isNotBlank(testPath)) {
            this.testPath = testPath;
        }
    }

    @DataBoundSetter
    public void setScreenshotsDirectory(String screenshotsDirectory) {
        if (isNotBlank(screenshotsDirectory)) {
            this.screenshotsDirectory = screenshotsDirectory;
        }
    }

    @DataBoundSetter
    public void setKeyValuePairs(String keyValuePairs) {
        if (isNotBlank(keyValuePairs)) {
            this.keyValuePairs = keyValuePairs;
        }
    }

    @DataBoundSetter
    public void setWithAnnotation(String withAnnotation) {
        if (isNotBlank(withAnnotation)) {
            this.withAnnotation = withAnnotation;
        }
    }

    @DataBoundSetter
    public void setWithoutAnnotation(String withoutAnnotation) {
        if (isNotBlank(withoutAnnotation)) {
            this.withoutAnnotation = withoutAnnotation;
        }
    }

    @DataBoundSetter
    public void setTestCasesSelect(String testCasesSelect) {
        if (isNotBlank(testCasesSelect) && !testCasesSelect.equalsIgnoreCase(DEFAULT_TEST_CASES_SELECT)) {
            this.testCasesSelect = testCasesSelect;
        }
    }

    @DataBoundSetter
    public void setTestCasesValue(String testCasesValue) {
        if (isNotBlank(testCasesValue)) {
            this.testCasesValue = testCasesValue;
        }
    }

    @DataBoundSetter
    public void setDataPath(String dataPath) {
        if (isNotBlank(dataPath)) {
            this.dataPath = dataPath;
        }
    }

    @DataBoundSetter
    public void setLanguage(String language) {
        if (isNotBlank(language) && !language.equals(DEFAULT_LANGUAGE)) {
            this.language = language;
        }

    }

    @DataBoundSetter
    public void setScheduler(String scheduler) {
        if (isNotBlank(scheduler) && !scheduler.equalsIgnoreCase(DEFAULT_SCHEDULER)) {
            this.scheduler = scheduler.toUpperCase();
        }
    }

    @DataBoundSetter
    public void setTestTimeout(String testTimeout) {
        if (isNotBlank(testTimeout)) {
            this.testTimeout = testTimeout;
        }
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
    public void setCloudUIUrl(String cloudUIUrl) {
        this.cloudUIUrl = cloudUIUrl;
    }

    @DataBoundSetter
    public void setFailBuildIfThisStepFailed(boolean failBuildIfThisStepFailed) {
        this.failBuildIfThisStepFailed = failBuildIfThisStepFailed;
    }

    @DataBoundSetter
    public void setFrameworkId(Long frameworkId) {
        this.frameworkId = frameworkId;
    }

    @DataBoundSetter
    public void setOsType(APIDevice.OsType osType) {
        this.osType = osType;
    }

    @DataBoundSetter
    public void setWaitForResultsBlock(WaitForResultsBlock waitForResultsBlock) {
        this.waitForResultsBlock = waitForResultsBlock;
    }

    public WaitForResultsBlock getWaitForResultsBlock() {
        return waitForResultsBlock;
    }

    public String getTestRunName() {
        return testRunName;
    }

    public String getAppPath() {
        return appPath;
    }

    public String getTestPath() {
        return testPath;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDeviceGroupId() {
        return deviceGroupId;
    }

    public String getTestRunner() {
        return testRunner;
    }

    public String getScreenshotsDirectory() {
        return screenshotsDirectory;
    }

    public String getKeyValuePairs() {
        return keyValuePairs;
    }

    public String getWithAnnotation() {
        return withAnnotation;
    }

    public String getWithoutAnnotation() {
        return withoutAnnotation;
    }

    public String getTestCasesSelect() {
        return isNotBlank(testCasesSelect) ? testCasesSelect.toUpperCase() : DEFAULT_TEST_CASES_SELECT;
    }

    public String getTestCasesValue() {
        return testCasesValue;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getLanguage() {
        return isNotBlank(language) ? language : DEFAULT_LANGUAGE;
    }

    public String getScheduler() {
        return isNotBlank(scheduler) ? scheduler.toUpperCase() : DEFAULT_SCHEDULER;
    }

    public boolean isFailBuildIfThisStepFailed() {
        return failBuildIfThisStepFailed;
    }

    public String getTestTimeout() {
        return testTimeout;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

    public String getCloudUIUrl() {
        return cloudUIUrl;
    }

    public Long getFrameworkId() {
        return this.frameworkId;
    }

    public APIDevice.OsType getOsType() {
        return osType;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl implements RunInCloudDescriptorHelper {

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
        private transient Run<?, ?> run;

        @StepContextParameter
        private transient FilePath workspace;

        @Override
        protected Boolean run() {
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
                    step.getScreenshotsDirectory(),
                    step.getKeyValuePairs(),
                    step.getWithAnnotation(),
                    step.getWithoutAnnotation(),
                    step.getTestCasesSelect(),
                    step.getTestCasesValue(),
                    step.isFailBuildIfThisStepFailed(),
                    step.getWaitForResultsBlock(),
                    step.getTestTimeout(),
                    step.getCredentialsId(),
                    step.getCloudUrl(),
                    step.getCloudUIUrl(),
                    step.getFrameworkId(),
                    step.getOsType()
            );

            return builder.completeRun(run, workspace, launcher, listener);
        }
    }
}
