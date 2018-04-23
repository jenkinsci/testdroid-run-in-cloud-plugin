package com.testdroid.jenkins;

import com.testdroid.api.APIDeviceGroupQueryBuilder;
import com.testdroid.api.APIException;
import com.testdroid.api.APIQueryBuilder;
import com.testdroid.api.model.*;
import com.testdroid.api.model.APITestRunConfig.Scheduler;
import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import com.testdroid.jenkins.remotesupport.MachineIndependentFileUploader;
import com.testdroid.jenkins.remotesupport.MachineIndependentResultsDownloader;
import com.testdroid.jenkins.scheduler.TestRunFinishCheckScheduler;
import com.testdroid.jenkins.scheduler.TestRunFinishCheckSchedulerFactory;
import com.testdroid.jenkins.utils.AndroidLocale;
import com.testdroid.jenkins.utils.EmailHelper;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import jenkins.tasks.SimpleBuildStep;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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
