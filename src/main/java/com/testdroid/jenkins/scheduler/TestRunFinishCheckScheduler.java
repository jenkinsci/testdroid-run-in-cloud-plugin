package com.testdroid.jenkins.scheduler;

/**
 * Testdroid Run in Cloud plugin
 *
 * https://git@github.com/bitbar/testdroid-run-in-cloud
 *
 * Usage:
 * @TODO
 *
 * @author info@bitbar.com
 */
public interface TestRunFinishCheckScheduler {

    public void schedule(final Object object, final Long projectId, final Long testRunId);

    public void cancel(final Long projectId, final Long testRunId);

}
