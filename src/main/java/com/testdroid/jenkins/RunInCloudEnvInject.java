package com.testdroid.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.Run;

/**
 * Created by rogerio.c.peixoto on 4/28/16.
 */
public class RunInCloudEnvInject implements EnvironmentContributingAction {

    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    private final String key;

    private final String value;

    public RunInCloudEnvInject(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void buildEnvironment(@NonNull Run<?, ?> run, @NonNull EnvVars envVars) {
        if (key != null && value != null) {
            envVars.put(key, value);
        }
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "RunInCloudBuilderEnvInjectionAction";
    }

    @Override
    public String getUrlName() {
        return null;
    }
}

