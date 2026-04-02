package com.testdroid.jenkins.remotesupport;

import com.testdroid.api.model.APIUser;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;
import com.testdroid.jenkins.auth.IBitbarCredentials;
import com.testdroid.jenkins.auth.TestdroidApiUtil;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.security.Roles;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;

/**
 * Utility for uploading files to the cloud before a run starts
 */
public class MachineIndependentFileUploader extends MachineIndependentTask implements FilePath.FileCallable<Long> {

    private final TaskListener listener;

    public MachineIndependentFileUploader(
            TestdroidCloudSettings.DescriptorImpl settings, TaskListener listener, IBitbarCredentials credentials) {
        super(settings, credentials);
        this.listener = listener;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        checker.check(this, Roles.SLAVE);
    }

    @Override
    public Long invoke(File file, VirtualChannel vc) {
        Long result = null;
        try {
            APIUser user = TestdroidApiUtil.createApiClientAdapter(this).getUser();
            if (file.exists()) {
                result = user.uploadFile(file).getId();
            } else {
                listener.getLogger().println(Messages.ERROR_FILE_NOT_FOUND(file.getAbsolutePath()));
            }
        } catch (Exception ex) {
            listener.getLogger().println(Messages.UPLOADING_FILE_ERROR(file.getAbsolutePath(), ex));
            ex.printStackTrace(listener.getLogger());
        }
        return result;
    }
}