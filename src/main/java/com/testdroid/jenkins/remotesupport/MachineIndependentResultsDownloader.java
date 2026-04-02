package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.APIException;
import com.testdroid.api.APIListResource;
import com.testdroid.api.dto.Context;
import com.testdroid.api.model.APIDeviceSession;
import com.testdroid.api.model.APIScreenshot;
import com.testdroid.api.model.APITestRun;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.auth.IBitbarCredentials;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.Roles;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.remoting.RoleChecker;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.testdroid.jenkins.auth.TestdroidApiUtil.createApiClientAdapter;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Utility for downloading results from the cloud after a run is completed
 */
public class MachineIndependentResultsDownloader extends MachineIndependentTask
        implements Callable<Boolean, APIException> {

    private static final Logger LOGGER = Logger.getLogger(MachineIndependentResultsDownloader.class.getName());

    private final boolean downloadScreenshots;

    private final TaskListener listener;

    private final long projectId;

    private final String resultsPath;

    private final long testRunId;

    public MachineIndependentResultsDownloader(
            TestdroidCloudSettings.DescriptorImpl settings, TaskListener listener, long projectId, long testRunId,
            String resultsPath, boolean downloadScreenshots, IBitbarCredentials credentials) {
        super(settings, credentials);
        this.projectId = projectId;
        this.testRunId = testRunId;
        this.resultsPath = resultsPath;
        this.downloadScreenshots = downloadScreenshots;
        this.listener = listener;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        checker.check(this, Roles.SLAVE);
    }

    @Override
    public Boolean call() throws APIException {
        APITestRun testRun = createApiClientAdapter(this).getUser().getProject(projectId).getTestRun(testRunId);

        boolean success = false; //if we are able to download results from at least one device then whole method
        // should return true, false only when results was not available at all, other case just warn in logs

        String deviceDisplayName;

        final APIListResource<APIDeviceSession> deviceSessionsResource = testRun.getDeviceSessionsResource(
                new Context<>(APIDeviceSession.class, 0, MAX_VALUE, EMPTY, EMPTY));
        for (APIDeviceSession deviceSession : deviceSessionsResource.getEntity().getData()) {
            deviceDisplayName = deviceSession.getDevice().getDisplayName();

            //create directory with results for this device
            String fileName = String.format("testdroid_result-%s-%d", deviceDisplayName.replace(" ", "_"),
                    deviceSession.getId());
            File resultDir = new File(resultsPath, fileName);
            if (deviceSession.getState() != APIDeviceSession.State.EXCLUDED) {
                success = download(deviceSession.getOutputFiles(), resultDir, "results.zip", deviceDisplayName);
                //optionally download screenshots
                if (downloadScreenshots) {
                    resultDir = new File(resultDir, "screenshots");
                    final APIListResource<APIScreenshot> screenshotsResource = deviceSession.getScreenshotsResource(
                            new Context<>(APIScreenshot.class, 0, MAX_VALUE, EMPTY, EMPTY));
                    for (APIScreenshot screenshot : screenshotsResource.getEntity().getData()) {
                        download(screenshot.getContent(), resultDir, screenshot.getOriginalName(), deviceDisplayName);
                    }
                }
            } else {
                listener.getLogger().println(Messages.NO_RESULT_FROM_DEVICE_TEST_WAS_NOT_LAUNCHED_S(deviceDisplayName));
            }
        }

        return success;
    }

    private boolean download(InputStream inputStream, File resultDir, String fileName, String deviceName) {
        try {
            FileUtils.forceMkdir(resultDir);
        } catch (IOException e) {
            String msg = Messages.COULDNT_CREATE_DIRECTORY(resultDir);
            listener.getLogger().println(msg);
            LOGGER.log(Level.WARNING, msg, e);
            return false;
        }
        try (InputStream input = inputStream;
             OutputStream outputStream = new FileOutputStream(new File(resultDir, fileName))) {
            IOUtils.copy(input, outputStream);
            return true;
        } catch (IOException e) {
            String msg = Messages.ERROR_DURING_DOWNLOAD_S_FROM_S(fileName, deviceName);
            listener.getLogger().println(msg);
            LOGGER.log(Level.WARNING, msg, e);
        }
        return false;
    }
}
