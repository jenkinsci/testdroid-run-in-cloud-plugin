package com.testdroid.jenkins.scheduler;

import com.testdroid.api.model.APITestRun;
import hudson.model.TaskListener;

public class HookUrlDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    private final TaskListener listener;

    public HookUrlDrivenTestFinishCheckScheduler(final TaskListener listener) {
        this.listener = listener;
    }

    public void schedule(final Object object, final APITestRun apiTestRun) {
        HookUrlResultWaiter.addToWaitList(apiTestRun.getId(), object);
    }

    public void cancel(final APITestRun apiTestRun) {
        HookUrlResultWaiter.removeFromWaitList(apiTestRun.getId());
    }

}
