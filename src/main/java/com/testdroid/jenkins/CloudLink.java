package com.testdroid.jenkins;

import hudson.model.Run;
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
        if (StringUtils.isBlank(cloudVersion) || versionCompare(cloudVersion, "2.54") >= 0) {
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

    private static int versionCompare(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int i = 0;
        while (i < parts1.length && i < parts2.length && parts1[i].equals(parts2[i])) {
            i++;
        }
        if (i < parts1.length && i < parts2.length) {
            return Integer.signum(Integer.valueOf(parts1[i]).compareTo(Integer.valueOf(parts2[i])));
        }
        return Integer.signum(parts1.length - parts2.length);
    }
}
