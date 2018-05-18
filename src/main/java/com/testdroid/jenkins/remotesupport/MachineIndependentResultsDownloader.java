package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.APIQueryBuilder;
import com.testdroid.api.model.APIDeviceSession;
import com.testdroid.api.model.APIScreenshot;
import com.testdroid.api.model.APITestRun;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for downloading results from the cloud after a run is completed
 */
public class MachineIndependentResultsDownloader extends MachineIndependentTask
        implements Callable<Boolean, APIException> {

    private static final Logger LOGGER = Logger.getLogger(MachineIndependentResultsDownloader.class.getName());

    private boolean downloadScreenshots;

    private TaskListener listener;

    private long projectId;

    private String resultsPath;

    private long testRunId;

    public MachineIndependentResultsDownloader(TestdroidCloudSettings.DescriptorImpl settings, TaskListener listener,
            long projectId, long testRunId,
            String resultsPath, boolean downloadScreenshots) {
        super(settings);
        this.projectId = projectId;
        this.testRunId = testRunId;
        this.resultsPath = resultsPath;
        this.downloadScreenshots = downloadScreenshots;
        this.listener = listener;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        // no specific role needed, which is somewhat dubious, but I can't think of any attack vector that involves this.
        // it would have been simpler if the setMaximumBytecodeLevel only controlled the local setting,
        // not the remote setting
    }

    @Override
    public Boolean call() throws APIException {
        TestdroidCloudSettings.DescriptorImpl settings = new TestdroidCloudSettings.DescriptorImpl(this);
        APIClient client = new TestdroidApiUtil(settings).getTestdroidAPIClient();
        APITestRun testRun = client.me().getProject(projectId).getTestRun(testRunId);

        boolean success = false; //if we are able to download results from at least one device then whole method
        // should return true, false only when results was not available at all, other case just warn in logs

        String deviceDisplayName;
        for (APIDeviceSession deviceSession : testRun.getDeviceSessionsResource(new APIQueryBuilder().limit(Integer
                .MAX_VALUE)).getEntity().getData()) {
            deviceDisplayName = deviceSession.getDevice().getDisplayName();

            //create directory with results for this device
            File resultDir = new File(String
                    .format("%s/testdroid_result-%s-%d", resultsPath.endsWith(File.pathSeparator) ? resultsPath
                                    .substring(0, resultsPath.length() - File.pathSeparator.length()) : resultsPath,
                            deviceDisplayName.replaceAll(" ", "_"), deviceSession.getId()));
            if (deviceSession.getState() != APIDeviceSession.State.EXCLUDED) {
                success = download(deviceSession.getOutputFiles(), resultDir, "results.zip", deviceDisplayName);
                //optionally download screenshots
                if (downloadScreenshots) {
                    resultDir = new File(resultDir, "screenshots");
                    for (APIScreenshot screenshot : deviceSession.getScreenshotsResource(new APIQueryBuilder()
                            .limit(Integer.MAX_VALUE)).getEntity().getData()) {
                        download(screenshot.getContent(), resultDir, screenshot.getOriginalName(), deviceDisplayName);
                    }
                }
            } else {
                listener.getLogger().println(String
                        .format(Messages.NO_RESULT_FROM_DEVICE_TEST_WAS_NOT_LAUNCHED_S(), deviceDisplayName));
            }
        }

        return success;
    }

    private boolean download(InputStream inputStream, File resultDir, String fileName, String deviceName) {
        OutputStream outputStream = null;
        try {
            FileUtils.forceMkdir(resultDir);
            outputStream = new FileOutputStream(new File(resultDir, fileName));
            IOUtils.copy(inputStream, outputStream);
            return true;
        } catch (Exception e) {
            String msg = String.format(Messages.ERROR_DURING_DOWNLOAD_S_FROM_S(), fileName, deviceName);
            listener.getLogger().println(msg);
            LOGGER.log(Level.WARNING, msg, e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return false;
    }
}
