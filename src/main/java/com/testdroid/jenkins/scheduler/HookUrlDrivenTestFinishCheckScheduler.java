package com.testdroid.jenkins.scheduler;

import com.testdroid.api.model.APITestRun;

public class HookUrlDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    public void schedule(final Object object, final APITestRun apiTestRun) {
        HookUrlResultWaiter.addToWaitList(apiTestRun.getId(), object);
    }

    public void cancel(final APITestRun apiTestRun) {
        HookUrlResultWaiter.removeFromWaitList(apiTestRun.getId());
    }

}
