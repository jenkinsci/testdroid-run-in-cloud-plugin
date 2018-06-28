package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.model.APIProject;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.utils.TestdroidApiUtil;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;

/**
 * Utility for uploading files to the cloud before a run starts
 */
public class MachineIndependentFileUploader extends MachineIndependentTask implements FilePath.FileCallable<Long> {

    private FILE_TYPE fileType;

    private TaskListener listener;

    private long projectId;

    public enum FILE_TYPE {
        APPLICATION,
        TEST,
        DATA
    }

    public MachineIndependentFileUploader(TestdroidCloudSettings.DescriptorImpl settings, long projectId,
            FILE_TYPE fileType, TaskListener listener) {
        super(settings);
        this.projectId = projectId;
        this.fileType = fileType;
        this.listener = listener;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        // no specific role needed, which is somewhat dubious, but I can't think of any attack vector that involves this.
        // it would have been simpler if the setMaximumBytecodeLevel only controlled the local setting,
        // not the remote setting
    }

    @Override
    public Long invoke(File file, VirtualChannel vc) {
        Long result = null;
            try {

                APIProject project = TestdroidApiUtil.createNewApiClient(this).getUser().getProject(projectId);
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
                    listener.getLogger().println(Messages.ERROR_FILE_NOT_FOUND(file.getAbsolutePath()));
                }
            } catch (Exception ex) {
                listener.getLogger().println(Messages.UPLOADING_FILE_ERROR(file.getAbsolutePath(), ex.getStackTrace()));
            }
        return result;
    }
}
