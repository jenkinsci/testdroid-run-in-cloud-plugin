package com.testdroid.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.BuildBadgeAction;

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
public class CloudLink implements BuildBadgeAction {

    public final Run<?, ?> owner;

    private String cloudLink;

    public CloudLink(Run<?, ?> owner, String cloudLink) {
        this.owner = owner;
        this.cloudLink = cloudLink;
    }

    public boolean hasLink() {
        return cloudLink != null;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/testdroid-run-in-cloud/images/cloud.gif";
    }

    @Override
    public String getDisplayName() {
        return "See detailed results in Testdroid Cloud";
    }

    @Override
    public String getUrlName() {
        return cloudLink;
    }
}
