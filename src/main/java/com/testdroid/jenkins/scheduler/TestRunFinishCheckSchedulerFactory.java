package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.model.TestRunStateCheckMethod;

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
public class TestRunFinishCheckSchedulerFactory {

    public static TestRunFinishCheckScheduler createTestRunFinishScheduler(TestRunStateCheckMethod method) {
        TestRunFinishCheckScheduler result;
        switch (method) {
            case API_CALL:
                result = new APIDrivenTestFinishCheckScheduler();
                break;
            case HOOK_URL:
            default:
                result = new HookUrlDrivenTestFinishCheckScheduler();
                break;
        }
        return result;
    }

}
