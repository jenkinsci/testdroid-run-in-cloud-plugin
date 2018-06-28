package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.model.APIUser;
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

    private TaskListener listener;

    public MachineIndependentFileUploader(TestdroidCloudSettings.DescriptorImpl settings, TaskListener listener) {
        super(settings);
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
                APIUser user = TestdroidApiUtil.createNewApiClient(this).getUser();
                if (file.exists()) {
                    result = user.uploadFile(file).getId();
                } else {
                    listener.getLogger().println(Messages.ERROR_FILE_NOT_FOUND(file.getAbsolutePath()));
                }
            } catch (Exception ex) {
                listener.getLogger().println(Messages.UPLOADING_FILE_ERROR(file.getAbsolutePath(), ex.getStackTrace()));
            }
        return result;
    }
}
