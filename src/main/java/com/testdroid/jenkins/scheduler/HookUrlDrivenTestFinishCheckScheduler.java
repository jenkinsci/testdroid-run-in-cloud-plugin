package com.testdroid.jenkins.scheduler;

import com.testdroid.api.model.APIUser;

public class HookUrlDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    public void schedule(final Object object, APIUser user, final Long projectId, final Long testRunId) {
        HookUrlResultWaiter.addToWaitList(testRunId, object);
    }

    public void cancel(final Long projectId, final Long testRunId) {
        HookUrlResultWaiter.removeFromWaitList(testRunId);
    }

}
