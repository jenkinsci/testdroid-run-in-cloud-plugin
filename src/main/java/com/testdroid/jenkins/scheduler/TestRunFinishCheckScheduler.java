package com.testdroid.jenkins.scheduler;

import com.testdroid.api.model.APITestRun;

public interface TestRunFinishCheckScheduler {

    void schedule(final Object object, final APITestRun apiTestRun);

    void cancel(final APITestRun apiTestRun);

}
