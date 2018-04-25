package com.testdroid.jenkins.scheduler;

public interface TestRunFinishCheckScheduler {

    void schedule(final Object object, final Long projectId, final Long testRunId);

    void cancel(final Long projectId, final Long testRunId);

}
