package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import hudson.model.TaskListener;

public class TestRunFinishCheckSchedulerFactory {

    private TestRunFinishCheckSchedulerFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static TestRunFinishCheckScheduler createTestRunFinishScheduler(
            TestRunStateCheckMethod method, TaskListener listener) {
        if (method == TestRunStateCheckMethod.API_CALL) {
            return new APIDrivenTestFinishCheckScheduler(listener);
        } else {
            return new HookUrlDrivenTestFinishCheckScheduler();
        }
    }

}
