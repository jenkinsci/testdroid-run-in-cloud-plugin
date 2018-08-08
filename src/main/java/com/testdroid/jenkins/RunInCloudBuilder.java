package com.testdroid.jenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.testdroid.api.APIException;
import com.testdroid.api.APIListResource;
import com.testdroid.api.dto.Context;
import com.testdroid.api.filter.BooleanFilterEntry;
import com.testdroid.api.filter.StringFilterEntry;
import com.testdroid.api.model.*;
import com.testdroid.api.model.APITestRunConfig.Scheduler;
import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import com.testdroid.jenkins.remotesupport.MachineIndependentFileUploader;
import com.testdroid.jenkins.remotesupport.MachineIndependentResultsDownloader;
import com.testdroid.jenkins.scheduler.TestRunFinishCheckScheduler;
import com.testdroid.jenkins.scheduler.TestRunFinishCheckSchedulerFactory;
import com.testdroid.jenkins.utils.AndroidLocale;
import com.testdroid.jenkins.utils.ApiClientAdapter;
import com.testdroid.jenkins.utils.LocaleUtil;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.testdroid.api.dto.Operand.EQ;
import static com.testdroid.api.model.APIDevice.OsType;
import static com.testdroid.api.model.APIDevice.OsType.UNDEFINED;
import static com.testdroid.dao.repository.dto.MappingKey.*;
import static com.testdroid.jenkins.Messages.*;
import static hudson.util.ListBoxModel.Option;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class RunInCloudBuilder extends AbstractBuilder {

    private static final Logger LOGGER = Logger.getLogger(RunInCloudBuilder.class.getSimpleName());

    private static final String POST_HOOK_URL = "/plugin/testdroid-run-in-cloud/api/json/cloud-webhook";

    private static final String DEFAULT_TEST_TIMEOUT = "600"; // 10 minutes

    private static final Semaphore semaphore = new Semaphore(1);

    private static final List<String> PAID_ROLES = Arrays
            .asList("PRIORITY_SILVER", "PRIORITY_GOLD", "PRIORITY_PLATINUM");

    private String appPath;

    private String clusterId;

    private String dataPath;

    private boolean failBuildIfThisStepFailed;

    private String keyValuePairs;

    private String language;

    private String projectId;

    private String scheduler;

    private String screenshotsDirectory;

    private String testCasesSelect;

    private String testCasesValue;

    private String testPath;

    private String testRunName;

    private String testRunner;

    private WaitForResultsBlock waitForResultsBlock;

    private String withAnnotation;

    private String withoutAnnotation;

    private String testTimeout;

    private String credentialsId;

    private String cloudUrl;

    private Long frameworkId;

    private APIDevice.OsType osType;

    private String cloudUIUrl;

    public String getCloudUIUrl() {
        return cloudUIUrl;
    }

    public void setCloudUIUrl(String cloudUIUrl) {
        this.cloudUIUrl = cloudUIUrl;
    }

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public RunInCloudBuilder(
            String projectId, String appPath, String testPath, String dataPath, String testRunName, String scheduler,
            String testRunner, String clusterId, String language, String screenshotsDirectory,
            String keyValuePairs, String withAnnotation, String withoutAnnotation, String testCasesSelect,
            String testCasesValue, Boolean failBuildIfThisStepFailed,
            WaitForResultsBlock waitForResultsBlock, String testTimeout, String credentialsId, String cloudUrl,
            String cloudUIUrl, Long frameworkId, APIDevice.OsType osType) {
        this.projectId = projectId;
        this.appPath = appPath;
        this.dataPath = dataPath;
        this.testPath = testPath;
        this.testRunName = testRunName;
        this.scheduler = scheduler;
        this.testRunner = testRunner;
        this.screenshotsDirectory = screenshotsDirectory;
        this.keyValuePairs = keyValuePairs;
        this.withAnnotation = withAnnotation;
        this.withoutAnnotation = withoutAnnotation;
        this.testCasesSelect = testCasesSelect;
        this.testCasesValue = testCasesValue;
        this.clusterId = clusterId;
        this.language = language;
        this.failBuildIfThisStepFailed = failBuildIfThisStepFailed;
        this.testTimeout = testTimeout;
        this.credentialsId = credentialsId;
        this.cloudUrl = cloudUrl;
        this.frameworkId = frameworkId;
        this.osType = osType;
        this.waitForResultsBlock = waitForResultsBlock;
        this.cloudUIUrl = cloudUIUrl;
    }

    public String getTestRunName() {
        return testRunName;
    }

    public void setTestRunName(String testRunName) {
        this.testRunName = testRunName;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getTestPath() {
        return testPath;
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTestRunner() {
        return testRunner;
    }

    public void setTestRunner(String testRunner) {
        this.testRunner = testRunner;
    }

    public String getScreenshotsDirectory() {
        return screenshotsDirectory;
    }

    public void setScreenshotsDirectory(String screenshotsDirectory) {
        this.screenshotsDirectory = screenshotsDirectory;
    }

    public String getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(String keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    public String getWithAnnotation() {
        return withAnnotation;
    }

    public void setWithAnnotation(String withAnnotation) {
        this.withAnnotation = withAnnotation;
    }

    public String getWithoutAnnotation() {
        return withoutAnnotation;
    }

    public void setWithoutAnnotation(String withoutAnnotation) {
        this.withoutAnnotation = withoutAnnotation;
    }

    public String getTestCasesSelect() {
        if (StringUtils.isBlank(testCasesSelect)) {
            return APITestRunConfig.LimitationType.PACKAGE.name();
        }

        return testCasesSelect;
    }

    public void setTestCasesSelect(String testCasesSelect) {
        this.testCasesSelect = testCasesSelect;
    }

    public String getTestCasesValue() {
        return testCasesValue;
    }

    public void setTestCasesValue(String testCasesValue) {
        this.testCasesValue = testCasesValue;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getLanguage() {
        if (StringUtils.isBlank(language)) {
            language = LocaleUtil.formatLangCode(Locale.US);
        }
        // handle old versions' configs with wrongly formatted language codes
        if (language.contains("-")) {
            language = language.replace('-', '_');
        }
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScheduler() {
        if (StringUtils.isBlank(scheduler)) {
            scheduler = Scheduler.PARALLEL.name();
        }
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler.toLowerCase();
    }

    public String getTestTimeout() {
        if (StringUtils.isBlank(testTimeout)) {
            return DEFAULT_TEST_TIMEOUT;
        }
        return testTimeout;
    }

    public void setTestTimeout(String testTimeout) {
        this.testTimeout = testTimeout;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

    public void setCloudUrl(String cloudUrl) {
        this.cloudUrl = cloudUrl;
    }

    public WaitForResultsBlock getWaitForResultsBlock() {
        return waitForResultsBlock;
    }

    public void setWaitForResultsBlock(WaitForResultsBlock waitForResultsBlock) {
        this.waitForResultsBlock = waitForResultsBlock;
    }

    public boolean isFailBuildIfThisStepFailed() {
        return failBuildIfThisStepFailed;
    }

    public void setFailBuildIfThisStepFailed(boolean failBuildIfThisStepFailed) {
        this.failBuildIfThisStepFailed = failBuildIfThisStepFailed;
    }

    public boolean isFullTest() {
        return StringUtils.isNotBlank(testPath);
    }

    public boolean isDataFile() {
        return StringUtils.isNotBlank(dataPath);
    }

    private boolean verifyParameters(TaskListener listener) {
        boolean result = true;
        if (StringUtils.isBlank(projectId)) {
            listener.getLogger().println(EMPTY_PROJECT() + "\n");
            result = false;
        }
        return result;
    }

    public boolean isWaitForResults() {
        return waitForResultsBlock != null;
    }

    public Long getFrameworkId() {
        return frameworkId;
    }

    public void setFrameworkId(Long frameworkId) {
        this.frameworkId = frameworkId;
    }

    public OsType getOsType() {
        if (osType == null) {
            osType = UNDEFINED;
        }
        return osType;
    }

    public void setOsType(OsType osType) {
        this.osType = osType;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private String evaluateHookUrl() {
        return isWaitForResults() ?
                StringUtils.isNotBlank(waitForResultsBlock.getHookURL()) ? waitForResultsBlock.getHookURL()
                        : String.format("%s%s", Jenkins.getInstance().getRootUrl(), POST_HOOK_URL) :
                null;
    }

    private String evaluateResultsPath(FilePath workspace) {
        if (isWaitForResults()) {
            String resultsPath = waitForResultsBlock.getResultsPath();
            if (StringUtils.isNotBlank(resultsPath)) {
                try {
                    return getAbsolutePath(workspace, resultsPath);
                } catch (Exception exception) {
                    LOGGER.log(Level.WARNING, "Couldn't get absolute path for results. Using workspace...");
                }
            }

            return workspace.getRemote();
        }

        return null;
    }

    /**
     * Perform build step, as required by AbstractBuilder
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return completeRun(build, build.getWorkspace(), launcher, listener);
    }

    /**
     * Wrapper around runTest to be used elsewhere, and sensibly log steps of the build procedure
     */
    public boolean completeRun(Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener) {
        listener.getLogger().println(Messages.RUN_TEST_IN_CLOUD_STARTED());

        boolean result = runTest(build, workspace, launcher, listener);
        if (result) {
            listener.getLogger().println(Messages.RUN_TEST_IN_CLOUD_SUCCEEDED());
        } else {
            listener.getLogger().println(Messages.RUN_TEST_IN_CLOUD_FAILED());
        }

        return result || !failBuildIfThisStepFailed;
    }

    /**
     * Actually run tests against Bitbar Cloud, and perhaps wait for results
     */
    private boolean runTest(Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener) {
        // rewrite paths to take variables into consideration
        String appPathFinal = applyMacro(build, listener, appPath);
        String testPathFinal = applyMacro(build, listener, testPath);
        String dataPathFinal = applyMacro(build, listener, dataPath);
        String withAnnotationFinal = applyMacro(build, listener, withAnnotation);
        String testRunnerFinal = applyMacro(build, listener, testRunner);
        String withoutAnnotationFinal = applyMacro(build, listener, withoutAnnotation);

        // cloudSettings will load the global settings in constructor..!
        TestdroidCloudSettings.DescriptorImpl cloudSettings = new TestdroidCloudSettings.DescriptorImpl();

        // override default cloud settings if credentials/cloud URL specified on build level
        if (StringUtils.isNotBlank(getCredentialsId())) {
            StandardUsernamePasswordCredentials credentials = CredentialsProvider.findCredentialById(
                    getCredentialsId(), StandardUsernamePasswordCredentials.class,
                    build,
                    Collections.emptyList()
            );

            if (credentials != null) {
                listener.getLogger().println(Messages.BUILD_STEP_USING_CREDENTIALS());
                cloudSettings = new TestdroidCloudSettings.DescriptorImpl(
                        credentials.getUsername(),
                        credentials.getPassword().getPlainText()
                );
                if (StringUtils.isNotBlank(getCloudUrl())) {
                    cloudSettings.setCloudUrl(getCloudUrl());
                    if( StringUtils.isNotBlank(getCloudUIUrl())) {
                        cloudSettings.setNewCloudUrl(getCloudUIUrl());
                    }
                }
            } else {
                listener.getLogger().println(String.format(Messages.COULDNT_FIND_CREDENTIALS(), getCredentialsId()));
            }
        } else if (StringUtils.isNotBlank(this.cloudUrl)) {
            // cloud URL always goes 1-to-1 with credentials, so it can't be used if credentials aren't specified..!
            listener.getLogger().println(String.format(Messages.CLOUD_URL_SET_BUT_NO_CREDENTIALS(), cloudUrl,
                    cloudSettings.getCloudUrl()));
        }

        boolean releaseDone = false;

        try {
            // make part update and run project "transactional"
            // so that a different job can't overwrite project settings just after the first one set it
            RunInCloudBuilder.semaphore.acquire();

            ApiClientAdapter api = TestdroidApiUtil.createApiClient(cloudSettings);
            if (!api.isAuthenticated()) {
                listener.getLogger().println("Couldn't connect to the cloud!");
                return false;
            }

            if (!verifyParameters(listener)) {
                return false;
            }

            APIUser user = api.getUser();
            String cloudVersion = api.getCloudVersion();

            final APIProject project = user.getProject(Long.parseLong(getProjectId().trim()));
            if (project == null) {
                listener.getLogger().println(Messages.CHECK_PROJECT_NAME());
                return false;
            }

            APITestRunConfig config = project.getTestRunConfig();
            config.setDeviceLanguageCode(getLanguage());
            config.setScheduler(Scheduler.valueOf(getScheduler().toUpperCase()));
            config.setUsedDeviceGroupId(Long.parseLong(getClusterId()));
            //Reset as in RiC we use only deviceGroups
            config.setDeviceIds(null);
            config.setHookURL(evaluateHookUrl());
            config.setScreenshotDir(getScreenshotsDirectory());
            config.setInstrumentationRunner(testRunnerFinal);
            config.setWithoutAnnotation(withoutAnnotationFinal);
            config.setWithAnnotation(withAnnotationFinal);
            config.setFrameworkId(Optional.ofNullable(frameworkId).orElse(config.getFrameworkId()));
            config.setOsType(Optional.ofNullable(osType).orElse(config.getOsType()));

            // default test timeout is 10 minutes
            config.setTimeout(Long.parseLong(DEFAULT_TEST_TIMEOUT));
            if (ApiClientAdapter.isPaidUser(user)) {
                try {
                    long runTimeout = Long.parseLong(getTestTimeout());
                    config.setTimeout(runTimeout);
                } catch (NumberFormatException e) {
                    listener.getLogger().println(TEST_TIMEOUT_NOT_NUMERIC_VALUE(getTestTimeout()));
                    LOGGER.log(Level.WARNING, "NumberFormatException when parsing timeout.", e);
                }
            } else {
                listener.getLogger().println(String.format(Messages.FREE_USERS_MAX_10_MINS(), user.getEmail()));
            }

            setLimitations(build, listener, config);
            createProvidedParameters(config);

            config = user.validateTestRunConfig(config);

            printTestJob(project, config, cloudSettings, cloudVersion, listener);
            getDescriptor().save();

            Long testFileId;
            Long dataFileId;
            Long appFileId;

            List<APIFileConfig> files = new ArrayList<>();

            if (StringUtils.isNotBlank(getAppPath())) {
                final FilePath appFile = new FilePath(launcher.getChannel(), getAbsolutePath(workspace, appPathFinal));
                listener.getLogger().println(String.format(Messages.UPLOADING_NEW_APPLICATION_S(), appPathFinal));
                appFileId = appFile.act(new MachineIndependentFileUploader(cloudSettings, listener));
                if (appFileId == null) {
                    return false;
                } else {
                    files.add(new APIFileConfig(appFileId, APIFileConfig.Action.INSTALL));
                }
            } else {
                listener.getLogger().println("App path was blank. Using latest app in project.");
            }

            if (isFullTest()) {
                FilePath testFile = new FilePath(launcher.getChannel(), getAbsolutePath(workspace, testPathFinal));
                listener.getLogger().println(String.format(Messages.UPLOADING_NEW_INSTRUMENTATION_S(), testPathFinal));
                testFileId = testFile.act(new MachineIndependentFileUploader(cloudSettings, listener));
                if (testFileId == null) {
                    return false;
                } else {
                    files.add(new APIFileConfig(testFileId, APIFileConfig.Action.RUN_TEST));
                }
            }

            if (isDataFile()) {
                FilePath dataFile = new FilePath(launcher.getChannel(), getAbsolutePath(workspace, dataPathFinal));
                listener.getLogger().println(String.format(Messages.UPLOADING_DATA_FILE_S(), dataPathFinal));
                dataFileId = dataFile.act(new MachineIndependentFileUploader(cloudSettings, listener));
                if (dataFileId == null) {
                    return false;
                } else {
                    files.add(new APIFileConfig(dataFileId, APIFileConfig.Action.COPY_TO_DEVICE));
                }
            }

            listener.getLogger().println(Messages.RUNNING_TESTS());

            // run project with proper name set in jenkins if it's set
            String finalTestRunName = applyMacro(build, listener, getTestRunName());
            if (StringUtils.isBlank(finalTestRunName) || finalTestRunName.trim().startsWith("$")) {
                finalTestRunName = null;
            }

            config.setFiles(files);
            config.setTestRunName(finalTestRunName);
            config = user.validateTestRunConfig(config);
            // start the test run itself
            APITestRun testRun = user.startTestRun(config);

            // add the Bitbar Cloud link to the left-hand-side menu in Jenkins
            BuildBadgeAction cloudLinkAction = new CloudLink(cloudSettings.resolveCloudUiUrl(), project.getId(),
                    testRun.getId(), cloudVersion);
            build.addAction(cloudLinkAction);
            RunInCloudEnvInject variable = new RunInCloudEnvInject("CLOUD_LINK", cloudLinkAction.getUrlName());
            build.addAction(variable);

            listener.getLogger().println(String.format("Started new Bitbar Cloud run at: %s (id: %s)",
                    cloudLinkAction.getUrlName(),
                    testRun.getId()));

            RunInCloudBuilder.semaphore.release();
            releaseDone = true;

            return waitForResults(user, project, testRun, workspace, launcher, listener, cloudSettings);

        } catch (APIException e) {
            listener.getLogger().println(String.format("%s: %s", Messages.ERROR_API(), e.getMessage()));
            LOGGER.log(Level.WARNING, Messages.ERROR_API(), e);
        } catch (IOException e) {
            listener.getLogger().println(String.format("%s: %s", Messages.ERROR_CONNECTION(), e.getLocalizedMessage()));
            LOGGER.log(Level.WARNING, Messages.ERROR_CONNECTION(), e);
        } catch (InterruptedException e) {
            listener.getLogger().println(String.format("%s: %s", Messages.ERROR_TESTDROID(), e.getLocalizedMessage()));
            LOGGER.log(Level.WARNING, Messages.ERROR_TESTDROID(), e);
        } catch (NumberFormatException e) {
            listener.getLogger().println(Messages.NO_DEVICE_GROUP_CHOSEN());
            LOGGER.log(Level.WARNING, Messages.NO_DEVICE_GROUP_CHOSEN());
        } finally {
            if (!releaseDone) {
                RunInCloudBuilder.semaphore.release();
            }
        }

        return false;
    }

    @SuppressFBWarnings(value="NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private boolean waitForResults(
            final APIUser user, final APIProject project, final APITestRun testRun,
            FilePath workspace, Launcher launcher, TaskListener listener,
            TestdroidCloudSettings.DescriptorImpl cloudSettings) {

        if (isWaitForResults()) {
            boolean isDownloadOk = false;
            TestRunFinishCheckScheduler scheduler = TestRunFinishCheckSchedulerFactory.createTestRunFinishScheduler(
                    waitForResultsBlock.getTestRunStateCheckMethod()
            );

            try {
                boolean testRunToAbort = false;
                listener.getLogger().println("Waiting for results...");
                scheduler.schedule(this, user, project.getId(), testRun.getId());
                try {
                    synchronized (this) {
                        wait(waitForResultsBlock.getWaitForResultsTimeout() * 1000);
                    }
                    scheduler.cancel(project.getId(), testRun.getId());
                    testRun.refresh();
                    if (testRun.getState() == APITestRun.State.FINISHED) {
                        isDownloadOk = launcher.getChannel().call(
                                new MachineIndependentResultsDownloader(
                                        cloudSettings, listener, project.getId(), testRun.getId(),
                                        evaluateResultsPath(workspace), waitForResultsBlock.isDownloadScreenshots()));

                        if (!isDownloadOk) {
                            listener.getLogger().println(Messages.DOWNLOAD_RESULTS_FAILED());
                            LOGGER.log(Level.WARNING, Messages.DOWNLOAD_RESULTS_FAILED());
                        }
                    } else {
                        testRunToAbort = true;
                        String msg = String.format(Messages.DOWNLOAD_RESULTS_FAILED_WITH_REASON_S(),
                                "Test run is not finished yet!");
                        listener.getLogger().println(msg);
                        LOGGER.log(Level.WARNING, msg);
                    }
                } catch (InterruptedException e) {
                    testRunToAbort = true;
                    listener.getLogger().println(e.getMessage());
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
                if (testRunToAbort && waitForResultsBlock.isForceFinishAfterBreak()) {
                    String msg = "Force finish test in Cloud";
                    listener.getLogger().println(msg);
                    LOGGER.log(Level.WARNING, msg);
                    testRun.abort();
                }
            } catch (APIException e) {
                listener.getLogger().println(
                        String.format("%s: %s", Messages.ERROR_API(), e.getMessage()));
                LOGGER.log(Level.WARNING, Messages.ERROR_API(), e);
            } catch (IOException e) {
                listener.getLogger().println(
                        String.format("%s: %s", Messages.ERROR_CONNECTION(), e.getLocalizedMessage()));
                LOGGER.log(Level.WARNING, Messages.ERROR_CONNECTION(), e);
            } finally {
                scheduler.cancel(project.getId(), testRun.getId());
            }
            return isDownloadOk;
        } else {
            return true;
        }
    }

    private void setLimitations(Run<?, ?> build, final TaskListener listener, APITestRunConfig config) {
        if (StringUtils.isNotBlank(getTestCasesValue())) {
            config.setLimitationType(APITestRunConfig.LimitationType.valueOf(getTestCasesSelect().toUpperCase()));
            config.setLimitationValue(applyMacro(build, listener, getTestCasesValue()));
        } else {
            config.setLimitationType(null);
            config.setLimitationValue("");
        }
    }

    private void createProvidedParameters(APITestRunConfig config) {
        List<APITestRunParameter> apiTestRunParameters = new ArrayList<>();
        if (keyValuePairs != null) {
            String[] splitKeyValuePairs = keyValuePairs.split(";");
            apiTestRunParameters.addAll(Arrays.stream(splitKeyValuePairs).filter(StringUtils::isNotEmpty).map(s -> {
                String[] pair = s.split(":");
                return pair.length == 2 ? new APITestRunParameter(pair[0], pair[1]) : null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        config.setTestRunParameters(apiTestRunParameters);
    }

    private void printTestJob(APIProject project, APITestRunConfig config,
            TestdroidCloudSettings.DescriptorImpl cloudSettings, String cloudVersion, TaskListener listener) {
        listener.getLogger().println(Messages.TEST_RUN_CONFIGURATION());
        listener.getLogger().println(String.format("%s: %s (version %s)", Messages.CLOUD_URL(),
                cloudSettings.getCloudUrl(), cloudVersion));
        listener.getLogger().println(String.format("%s: %s", Messages.USER_EMAIL(), cloudSettings.getEmail()));
        listener.getLogger().println(String.format("%s: %s", Messages.PROJECT(), project.getName()));
        listener.getLogger().println(OS_TYPE_VALUE(config.getOsType()));
        listener.getLogger().println(FRAMEWORK_ID_VALUE(config.getFrameworkId()));
        listener.getLogger().println(String.format("%s: %s", Messages.LOCALE(), config.getDeviceLanguageCode()));
        listener.getLogger().println(String.format("%s: %s", Messages.SCHEDULER(), config.getScheduler()));
        listener.getLogger().println(String.format("%s: %s", Messages.TIMEOUT(), config.getTimeout()));
    }

    private String getAbsolutePath(FilePath workspace, String path) throws IOException, InterruptedException {
        if (StringUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        }
        String trimmed = StringUtils.trim(path);
        if (trimmed.startsWith(File.separator)) { // absolute
            return trimmed;
        } else {
            URI workspaceURI = workspace.toURI();
            return workspaceURI.getPath() + trimmed;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> implements Serializable {

        public static final ListBoxModel.Option EMPTY_OPTION = new Option(EMPTY, EMPTY);

        private static final long serialVersionUID = 1L;

        public DescriptorImpl() {
            super(RunInCloudBuilder.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.TESTDROID_RUN_TESTS_IN_CLOUD();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> arg0) {
            return true;
        }

        public boolean isAuthenticated() {
            return TestdroidApiUtil.getGlobalApiClient().isAuthenticated();
        }

        //Do not remove, is used in config.jelly
        public boolean isPaidUser() {
            boolean result = false;
            if (isAuthenticated()) {
                try {
                    Date now = new Date();
                    result = Arrays.stream(TestdroidApiUtil.getGlobalApiClient().getUser().getRoles()).
                            anyMatch(r -> PAID_ROLES.contains(r.getName())
                                    && (r.getExpireTime() == null || r.getExpireTime().after(now)));
                } catch (APIException e) {
                    LOGGER.log(Level.WARNING, Messages.ERROR_API());
                }
            }
            return result;
        }

        public ListBoxModel doFillProjectIdItems() {
            ListBoxModel projects = new ListBoxModel();
            try {
                APIUser user = TestdroidApiUtil.getGlobalApiClient().getUser();
                final Context<APIProject> context = new Context(APIProject.class, 0, MAX_VALUE, EMPTY, EMPTY);
                final APIListResource<APIProject> projectResource = user.getProjectsResource(context);
                for (APIProject project : projectResource.getEntity().getData()) {
                    projects.add(project.getName(), project.getId().toString());
                }
            } catch (APIException e) {
                LOGGER.log(Level.WARNING, Messages.ERROR_API());
            }
            return projects;
        }

        public ListBoxModel doFillOsTypeItems() {
            ListBoxModel osTypes = new ListBoxModel();
            osTypes.addAll(Arrays.stream(OsType.values())
                    .map(t -> new Option(t.getDisplayName(), t.name()))
                    .collect(Collectors.toList()));
            return osTypes;
        }

        public FormValidation doCheckOsType(@QueryParameter OsType value) {
            return value == UNDEFINED ? FormValidation.error(DEFINE_OS_TYPE()) : FormValidation.ok();
        }

        public ListBoxModel doFillSchedulerItems() {
            ListBoxModel schedulers = new ListBoxModel();
            schedulers.add(Messages.SCHEDULER_PARALLEL(), Scheduler.PARALLEL.name());
            schedulers.add(Messages.SCHEDULER_SERIAL(), Scheduler.SERIAL.name());
            schedulers.add(Messages.SCHEDULER_SINGLE(), Scheduler.SINGLE.name());
            return schedulers;
        }

        public ListBoxModel doFillClusterIdItems() {
            ListBoxModel deviceGroups = new ListBoxModel();
            try {
                APIUser user = TestdroidApiUtil.getGlobalApiClient().getUser();
                final Context<APIDeviceGroup> context = new Context(APIDeviceGroup.class, 0, MAX_VALUE, EMPTY, EMPTY);
                context.setExtraParams(Collections.singletonMap(WITH_PUBLIC, TRUE));
                final APIListResource<APIDeviceGroup> deviceGroupResource = user.getDeviceGroupsResource(context);
                for (APIDeviceGroup deviceGroup : deviceGroupResource.getEntity().getData()) {
                    deviceGroups.add(String.format("%s (%d device(s))", deviceGroup.getDisplayName(),
                            deviceGroup.getDeviceCount()), deviceGroup.getId().toString());
                }
            } catch (APIException e) {
                LOGGER.log(Level.WARNING, Messages.ERROR_API());
            }
            return deviceGroups;
        }

        public ListBoxModel doFillLanguageItems() {
            ListBoxModel language = new ListBoxModel();
            for (Locale locale : AndroidLocale.LOCALES) {
                String langDisplay = String.format("%s (%s)", locale.getDisplayLanguage(),
                        locale.getDisplayCountry());
                String langCode = LocaleUtil.formatLangCode(locale);
                language.add(langDisplay, langCode);
            }
            return language;
        }

        public ListBoxModel doFillTestCasesSelectItems() {
            ListBoxModel testCases = new ListBoxModel();
            String value;
            for (APITestRunConfig.LimitationType limitationType : APITestRunConfig.LimitationType.values()) {
                value = limitationType.name();
                testCases.add(value.toLowerCase(), value);
            }
            return testCases;
        }

        public ListBoxModel doFillTestRunStateCheckMethodItems() {
            ListBoxModel items = new ListBoxModel();
            for (TestRunStateCheckMethod method : TestRunStateCheckMethod.values()) {
                items.add(method.name(), method.name());
            }
            return items;
        }

        public ListBoxModel doFillFrameworkIdItems(@QueryParameter OsType osType) {
            ListBoxModel frameworks = new ListBoxModel();
            frameworks.add(EMPTY_OPTION);
            if (osType != UNDEFINED) {
                try {
                    APIUser user = TestdroidApiUtil.getGlobalApiClient().getUser();
                    final Context<APIFramework> context = new Context(APIFramework.class, 0, MAX_VALUE, EMPTY, EMPTY);
                    context.addFilter(new StringFilterEntry(OS_TYPE, EQ, osType.name()));
                    context.addFilter(new BooleanFilterEntry(FOR_PROJECTS, EQ, TRUE));
                    context.addFilter(new BooleanFilterEntry(CAN_RUN_FROM_UI, EQ, TRUE));
                    final APIListResource<APIFramework> availableFrameworksResource = user
                            .getAvailableFrameworksResource(context);
                    frameworks.addAll(availableFrameworksResource.getEntity().getData().stream().map(f ->
                            new Option(f.getName(), f.getId().toString())).collect(Collectors.toList()));
                } catch (APIException e) {
                    LOGGER.log(Level.WARNING, Messages.ERROR_API());
                }
            }
            return frameworks;
        }

        public FormValidation doCheckFrameworkId(@QueryParameter String value) {
            return parseLong(value).isPresent() ? FormValidation.ok() : FormValidation.error(DEFINE_FRAMEWORK());
        }

        private static Optional<Long> parseLong(String value){
            try {
                return Optional.of(Long.parseLong(value));
            } catch (NumberFormatException nfe) {
                return Optional.empty();
            }
        }

    }
}
