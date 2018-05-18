package com.testdroid.jenkins.model;

/**
 * How the plugin should check for the state of a test run.
 *
 * HOOK_URL: the cloud should poll the plugin when it's done.
 * API_CALL: plugin will continuously poll the cloud.
 */
public enum TestRunStateCheckMethod {
    HOOK_URL,
    API_CALL
}
