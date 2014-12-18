package com.testdroid.jenkins.scheduler;

/**
 * @author Damian Sniezek <damian.sniezek@bitbar.com>
 */
public interface TestRunFinishCheckScheduler {

    public void schedule(final Object object, final Long projectId, final Long testRunId);

    public void cancel(final Long projectId, final Long testRunId);

}
