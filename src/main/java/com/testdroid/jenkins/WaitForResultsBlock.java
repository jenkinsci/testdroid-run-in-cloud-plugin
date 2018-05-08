package com.testdroid.jenkins;

import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import hudson.Extension;
import hudson.model.*;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import jenkins.model.Jenkins;

public class WaitForResultsBlock implements Describable<WaitForResultsBlock> {

    /**
     * @see hudson.model.Describable#getDescriptor()
     */
    @SuppressWarnings("unchecked")
    public Descriptor<WaitForResultsBlock> getDescriptor() {
        return Jenkins.getActiveInstance().getDescriptorOrDie(getClass());
    }

    boolean downloadScreenshots;

    boolean forceFinishAfterBreak;

    String hookURL = "";

    String resultsPath = "";

    TestRunStateCheckMethod testRunStateCheckMethod;

    Integer waitForResultsTimeout;

    @DataBoundConstructor
    public WaitForResultsBlock(String testRunStateCheckMethod, String hookURL, String waitForResultsTimeout, String resultsPath, boolean downloadScreenshots, boolean forceFinishAfterBreak) {
        this.testRunStateCheckMethod = TestRunStateCheckMethod.valueOf(testRunStateCheckMethod);
        this.hookURL = hookURL;
        this.resultsPath = resultsPath;
        this.downloadScreenshots = downloadScreenshots;
        this.forceFinishAfterBreak = forceFinishAfterBreak;
        this.waitForResultsTimeout = NumberUtils.toInt(waitForResultsTimeout);
    }

    public String getHookURL() {
        return hookURL;
    }

    public void setHookURL(String hookURL) {
        this.hookURL = hookURL;
    }

    public Integer getWaitForResultsTimeout() {
        if (waitForResultsTimeout == null) {
            waitForResultsTimeout = 0;
        }
        return waitForResultsTimeout;
    }

    public void setWaitForResultsTimeout(Integer waitForResultsTimeout) {
        this.waitForResultsTimeout = waitForResultsTimeout;
    }

    public String getResultsPath() {
        return resultsPath;
    }

    public void setResultsPath(String resultsPath) {
        this.resultsPath = resultsPath;
    }

    public boolean isDownloadScreenshots() {
        return downloadScreenshots;
    }

    public void setDownloadScreenshots(boolean downloadScreenshots) {
        this.downloadScreenshots = downloadScreenshots;
    }

    public TestRunStateCheckMethod getTestRunStateCheckMethod() {
        if (testRunStateCheckMethod == null) {
            testRunStateCheckMethod = TestRunStateCheckMethod.HOOK_URL;
        }
        return testRunStateCheckMethod;
    }

    public void setTestRunStateCheckMethod(TestRunStateCheckMethod testRunStateCheckMethod) {
        this.testRunStateCheckMethod = testRunStateCheckMethod;
    }

    public boolean isForceFinishAfterBreak() {
        return forceFinishAfterBreak;
    }

    public void setForceFinishAfterBreak(boolean forceFinishAfterBreak) {
        this.forceFinishAfterBreak = forceFinishAfterBreak;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<WaitForResultsBlock> {
        public DescriptorImpl() {
            super(WaitForResultsBlock.class);
        }

        @Override
        public String getDisplayName() {
            return "Wait for Bitbar Cloud results";
        }
    }
}
