package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.APIQueryBuilder;
import com.testdroid.api.model.APIDeviceRun;
import com.testdroid.api.model.APIScreenshot;
import com.testdroid.api.model.APITestRun;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Testdroid Run in Cloud plugin
 *
 * https://git@github.com/bitbar/testdroid-run-in-cloud
 *
 * Usage:
 * @TODO
 *
 * @author info@bitbar.com
 */
public class MachineIndependentResultsDownloader extends MachineIndependentTask
        implements Callable<Boolean, APIException> {

    private static final Logger LOGGER = Logger.getLogger(MachineIndependentResultsDownloader.class.getName());

    private boolean downloadScreenshots;

    private BuildListener listener;

    private long projectId;

    private String resultsPath;

    private long testRunId;

    public MachineIndependentResultsDownloader(
            TestdroidCloudSettings.DescriptorImpl descriptor, BuildListener listener, long projectId, long testRunId,
            String resultsPath, boolean downloadScreenshots) {
        super(descriptor);

        this.projectId = projectId;
        this.testRunId = testRunId;
        this.resultsPath = resultsPath;
        this.downloadScreenshots = downloadScreenshots;
        this.listener = listener;
    }

    @Override
    public Boolean call() throws APIException {
        if (!TestdroidApiUtil.isInitialized()) {
            TestdroidApiUtil.init(user, password, cloudUrl, privateInstance,
                    noCheckCertificate, isProxy, proxyHost, proxyPort, proxyUser,
                    proxyPassword);
        }
        APIClient client = TestdroidApiUtil.getInstance().getTestdroidAPIClient();
        APITestRun testRun = client.me().getProject(projectId).getTestRun(testRunId);
        boolean success = false; //if we are able to download results from at least one device then whole method
        // should return true, false only when results was not available at all, other case just warn in logs

        for (APIDeviceRun deviceRun : testRun.getDeviceRunsResource(new APIQueryBuilder().limit(Integer.MAX_VALUE))
                .getEntity().getData()) {
            OutputStream junitXmlOS = null;
            OutputStream logOS = null;
            OutputStream screenshotOS = null;
            String deviceDisplayName = deviceRun.getDevice().getDisplayName();

            //create directory with results for this device
            File resultDir = new File(String
                    .format("%s/testdroid_result-%s-%d", resultsPath.endsWith(File.pathSeparator)
                                    ? resultsPath
                                    .substring(0, resultsPath.length() - File.pathSeparator.length()) : resultsPath,
                            deviceDisplayName.replaceAll(" ", "_"),
                            deviceRun.getId()));
            if (deviceRun.getRunStatus() != APIDeviceRun.RunStatus.EXCLUDED
                    && (deviceRun.getInterruptedByState() == null || deviceRun.getTestCaseAllNo() > 0)) {
                try {
                    resultDir.mkdir();

                    //download .xml result
                    junitXmlOS = new FileOutputStream(new File(resultDir, "TEST-all.xml"));
                    IOUtils.copy(deviceRun.getJunitXml(), junitXmlOS);

                    //download logcat
                    logOS = new FileOutputStream(new File(resultDir, "logcat.txt"));
                    IOUtils.copy(deviceRun.getLogs(), logOS);

                    success = true;
                } catch (Exception e) {
                    String msg = String.format(Messages.DOWNLOAD_RESULTS_FROM_DEVICE_FAILED_S(), deviceDisplayName);
                    listener.getLogger().println(msg);
                    LOGGER.log(Level.WARNING, msg, e);
                } finally {
                    IOUtils.closeQuietly(junitXmlOS);
                    IOUtils.closeQuietly(logOS);
                }

                //optionally download screenshots
                if (downloadScreenshots) {
                    resultDir = new File(resultDir, "screenshots");
                    resultDir.mkdir();

                    for (APIScreenshot screenshot : deviceRun
                            .getScreenshotsResource(new APIQueryBuilder().limit(Integer.MAX_VALUE)).getEntity()
                            .getData()) {
                        try {
                            screenshotOS = new FileOutputStream(new File(resultDir, screenshot.getOriginalName()));
                            IOUtils.copy(screenshot.getContent(), screenshotOS);
                        } catch (Exception e) {
                            String msg = String.format(Messages.DOWNLOAD_SCREENSHOT_FROM_DEVICE_FAILED_S_S(), screenshot
                                    .getOriginalName(), deviceDisplayName);
                            listener.getLogger().println(msg);
                            LOGGER.log(Level.WARNING, msg, e);
                        } finally {
                            IOUtils.closeQuietly(screenshotOS);
                        }
                    }
                }
            } else {
                listener.getLogger().println(String
                        .format(Messages.NO_RESULT_FROM_DEVICE_TEST_WAS_NOT_LAUNCHED_S(), deviceDisplayName));
            }
        }

        return success;
    }
}
