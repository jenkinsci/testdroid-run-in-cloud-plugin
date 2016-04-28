package com.testdroid.jenkins;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

/**
 * Created by rogerio.c.peixoto on 4/28/16.
 */
public class RunInCloudEnvInject implements EnvironmentContributingAction {

    private String key;

    private String value;

    public RunInCloudEnvInject(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
        if (envVars != null && key != null && value != null) {
            envVars.put(key, value);
        }
    }

    @Override public String getIconFileName() {
        return null;
    }

    @Override public String getDisplayName() {
        return "RunInCloudBuilderEnvInjectionAction";
    }

    @Override public String getUrlName() {
        return null;
    }
}

