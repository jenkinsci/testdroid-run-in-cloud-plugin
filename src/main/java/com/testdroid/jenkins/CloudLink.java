package com.testdroid.jenkins;

import com.testdroid.jenkins.utils.VersionString;
import hudson.model.BuildBadgeAction;
import org.apache.commons.lang.StringUtils;

/**
 * Cloud link badge for showing an icon with a link to Bitbar Cloud after a run completes.
 */
public class CloudLink implements BuildBadgeAction {

    // The cloud link version format changed in version 2.54
    private static final String PRE_254_ENDPOINT_FORMAT = "%s/#service/testrun/%s/%s";

    // Version 2.54 and later
    private static final String POST_254_ENDPOINT_FORMAT = "%s/#testing/test-run/%s/%s";

    private String cloudLink;

    public CloudLink(String cloudURL, Long projectId, Long testRunId, String cloudVersion) {
        VersionString v254 = new VersionString("2.54");
        VersionString currentVersion = new VersionString(cloudVersion);

        // if the cloud version is 2.54 or bigger, return the newer cloud endpoint
        if (StringUtils.isBlank(cloudVersion) || currentVersion.compareTo(v254) >= 0) {
            this.cloudLink = String.format(POST_254_ENDPOINT_FORMAT, cloudURL, projectId, testRunId);
        } else {
            this.cloudLink = String.format(PRE_254_ENDPOINT_FORMAT, cloudURL, projectId, testRunId);
        }
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
