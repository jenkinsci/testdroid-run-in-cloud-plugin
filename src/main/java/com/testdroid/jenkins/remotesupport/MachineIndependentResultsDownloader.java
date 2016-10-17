package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.APIQueryBuilder;
import com.testdroid.api.model.APIDeviceRun;
import com.testdroid.api.model.APIDeviceSessionDataAvailability;
import com.testdroid.api.model.APIScreenshot;
import com.testdroid.api.model.APITestRun;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Testdroid Run in Cloud plugin
 *
 * https://git@github.com/jenkinsci/testdroid-run-in-cloud
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

        APIDeviceSessionDataAvailability dataAvailability;
        String deviceDisplayName;
        for (APIDeviceRun deviceRun : testRun.getDeviceRunsResource(new APIQueryBuilder().limit(Integer.MAX_VALUE))
                .getEntity().getData()) {
            deviceDisplayName = deviceRun.getDevice().getDisplayName();

            //create directory with results for this device
            File resultDir = new File(String
                    .format("%s/testdroid_result-%s-%d", resultsPath.endsWith(File.pathSeparator) ? resultsPath
                                    .substring(0, resultsPath.length() - File.pathSeparator.length()) : resultsPath,
                            deviceDisplayName.replaceAll(" ", "_"), deviceRun.getId()));
            if (deviceRun.getRunStatus() != APIDeviceRun.RunStatus.EXCLUDED) {
                dataAvailability = deviceRun.getDataAvailability();
                if (dataAvailability.isLogs() && download(deviceRun
                        .getLogs(), resultDir, "devicelog.log", deviceDisplayName)) {
                    success = true;
                }
                if (download(deviceRun.getJunitXml(), resultDir, "TEST-all.xml", deviceDisplayName)) {
                    success = true;
                }
                if (dataAvailability.isPerformance() && download(deviceRun
                        .getPerformanceData(), resultDir, "performance.txt", deviceDisplayName)) {
                    success = true;
                }
                if (dataAvailability.isResultsDataZip() && download(deviceRun
                        .getResultDataZip(), resultDir, "results.zip", deviceDisplayName)) {
                    success = true;
                }
                //optionally download screenshots
                if (downloadScreenshots && dataAvailability.isScreenshots()) {
                    resultDir = new File(resultDir, "screenshots");
                    for (APIScreenshot screenshot : deviceRun.getScreenshotsResource(new APIQueryBuilder()
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
