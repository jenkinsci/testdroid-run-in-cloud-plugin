package com.testdroid.jenkins;

import hudson.model.BuildBadgeAction;

/**
 * Cloud link badge for showing an icon with a link to Bitbar Cloud after a run completes.
 */
public class CloudLink implements BuildBadgeAction {

    private final String uiLink;

    public CloudLink(String uiLink) {
        this.uiLink = uiLink;
    }

    public boolean hasLink() {
        return uiLink != null;
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
        return uiLink;
    }
}
