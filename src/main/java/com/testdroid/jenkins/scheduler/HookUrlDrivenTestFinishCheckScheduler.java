package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.utils.ResultWaiter;

/**
 * @author Damian Sniezek <damian.sniezek@bitbar.com>
 */
public class HookUrlDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    public void schedule(final Object object, final Long projectId, final Long testRunId) {
        ResultWaiter.getInstance().putToWaitList(testRunId, object);
    }

    public void cancel(final Long projectId, final Long testRunId) {
        ResultWaiter.getInstance().removeFromWaitList(testRunId);
    }

}
