package com.testdroid.jenkins;

import hudson.model.BuildBadgeAction;

/**
 * Cloud link badge for showing an icon with a link to Bitbar Cloud after a run completes.
 */
public class CloudLink implements BuildBadgeAction {

    private String cloudLink;

    public CloudLink(String uiLink) {
        this.cloudLink = uiLink;
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
        return "See detailed results in Bitbar Cloud";
    }

    @Override
    public String getUrlName() {
        return cloudLink;
    }
}
