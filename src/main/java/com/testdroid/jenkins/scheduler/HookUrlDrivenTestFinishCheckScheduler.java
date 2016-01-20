package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.utils.ResultWaiter;

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
public class HookUrlDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    public void schedule(final Object object, final Long projectId, final Long testRunId) {
        ResultWaiter.getInstance().putToWaitList(testRunId, object);
    }

    public void cancel(final Long projectId, final Long testRunId) {
        ResultWaiter.getInstance().removeFromWaitList(testRunId);
    }

}
