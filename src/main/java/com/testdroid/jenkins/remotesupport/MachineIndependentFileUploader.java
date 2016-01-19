package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.APIClient;
import com.testdroid.api.model.APIProject;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
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
public class MachineIndependentFileUploader extends MachineIndependentTask implements FilePath.FileCallable<Long> {

    private static final Logger LOGGER = Logger.getLogger(MachineIndependentFileUploader.class.getName());

    private FILE_TYPE fileType;

    BuildListener listener;

    long projectId;

    public enum FILE_TYPE {
        APPLICATION,
        TEST,
        DATA
    }

    public MachineIndependentFileUploader(
            TestdroidCloudSettings.DescriptorImpl descriptor, long projectId, FILE_TYPE fileType,
            BuildListener listener) {
        super(descriptor);

        this.projectId = projectId;
        this.fileType = fileType;
        this.listener = listener;
    }

    @Override
    public Long invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {
        Long result = null;
        int attempts = 3;
        do {
            try {
                if (!TestdroidApiUtil.isInitialized()) {
                    TestdroidApiUtil.init(user, password, cloudUrl, privateInstance,
                            noCheckCertificate, isProxy, proxyHost, proxyPort, proxyUser,
                            proxyPassword);
                }
                APIClient client = TestdroidApiUtil.getInstance().getTestdroidAPIClient();
                APIProject project = client.me().getProject(projectId);

                if (file.exists()) {
                    switch (fileType) {
                        case APPLICATION:
                            result = project.uploadApplication(file, "application/octet-stream").getId();
                            break;
                        case TEST:
                            result = project.uploadTest(file, "application/octet-stream").getId();
                            break;
                        case DATA:
                            result = project.uploadData(file, "application/zip").getId();
                            break;
                    }
                } else {
                    listener.getLogger().println(String.format("%s: %s", Messages.ERROR_FILE_NOT_FOUND(),
                            file.getAbsolutePath()));
                    return null;
                }
            } catch (Exception ex) {
                String message = String.format("Cannot upload file %s%s", file.getAbsolutePath(), attempts > 1 ?
                        ". Will retry automatically" : "Won't be retried anymore");
                listener.getLogger().println(message);
                LOGGER.log(Level.WARNING, message, ex);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                }
            }
        } while (result == null && --attempts > 0);

        return result;
    }
}
