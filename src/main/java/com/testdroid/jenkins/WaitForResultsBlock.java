package com.testdroid.jenkins;

import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static com.testdroid.jenkins.model.TestRunStateCheckMethod.HOOK_URL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class WaitForResultsBlock implements Describable<WaitForResultsBlock> {

    /**
     * @see hudson.model.Describable#getDescriptor()
     */
    @SuppressWarnings("unchecked")
    public Descriptor<WaitForResultsBlock> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    private boolean downloadScreenshots;

    private boolean forceFinishAfterBreak;

    private String hookURL;

    private String resultsPath;

    private TestRunStateCheckMethod testRunStateCheckMethod;

    private Integer waitForResultsTimeout;

    @DataBoundConstructor
    public WaitForResultsBlock(TestRunStateCheckMethod testRunStateCheckMethod) {
        this.testRunStateCheckMethod = testRunStateCheckMethod;
    }

    public String getHookURL() {
        return hookURL;
    }

    @DataBoundSetter
    public void setHookURL(String hookURL) {
        if (isNotBlank(hookURL)) {
            this.hookURL = hookURL;
        }
    }

    public Integer getWaitForResultsTimeout() {
        return waitForResultsTimeout != null ? waitForResultsTimeout : 0;
    }

    @DataBoundSetter
    public void setWaitForResultsTimeout(Integer waitForResultsTimeout) {
        this.waitForResultsTimeout = waitForResultsTimeout;
    }

    public String getResultsPath() {
        return resultsPath;
    }

    @DataBoundSetter
    public void setResultsPath(String resultsPath) {
        if (isNotBlank(resultsPath)) {
            this.resultsPath = resultsPath;
        }
    }

    public boolean isDownloadScreenshots() {
        return downloadScreenshots;
    }

    @DataBoundSetter
    public void setDownloadScreenshots(boolean downloadScreenshots) {
        this.downloadScreenshots = downloadScreenshots;
    }

    public TestRunStateCheckMethod getTestRunStateCheckMethod() {
        return testRunStateCheckMethod;
    }

    @DataBoundSetter
    public void setTestRunStateCheckMethod(TestRunStateCheckMethod testRunStateCheckMethod) {
        this.testRunStateCheckMethod = testRunStateCheckMethod;
    }

    public boolean isForceFinishAfterBreak() {
        return forceFinishAfterBreak;
    }

    @DataBoundSetter
    public void setForceFinishAfterBreak(boolean forceFinishAfterBreak) {
        this.forceFinishAfterBreak = forceFinishAfterBreak;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<WaitForResultsBlock> {

        public static final TestRunStateCheckMethod DEFAULT_TEST_RUN_STATE_CHECK_METHOD = HOOK_URL;

        public DescriptorImpl() {
            super(WaitForResultsBlock.class);
        }

        @Override
        public String getDisplayName() {
            return "Wait for Bitbar Cloud results";
        }
    }
}
