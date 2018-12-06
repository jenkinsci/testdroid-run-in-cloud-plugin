package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import hudson.model.TaskListener;

public class TestRunFinishCheckSchedulerFactory {

    public static TestRunFinishCheckScheduler createTestRunFinishScheduler(TestRunStateCheckMethod method, TaskListener listener) {
        TestRunFinishCheckScheduler result;
        switch (method) {
            case API_CALL:
                result = new APIDrivenTestFinishCheckScheduler(listener);
                break;
            case HOOK_URL:
            default:
                result = new HookUrlDrivenTestFinishCheckScheduler(listener);
                break;
        }
        return result;
    }

}
