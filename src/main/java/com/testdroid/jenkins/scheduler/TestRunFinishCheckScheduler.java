package com.testdroid.jenkins.scheduler;

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
public interface TestRunFinishCheckScheduler {

    void schedule(final Object object, final Long projectId, final Long testRunId);

    void cancel(final Long projectId, final Long testRunId);

}
