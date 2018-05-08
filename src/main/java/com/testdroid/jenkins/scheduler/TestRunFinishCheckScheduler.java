package com.testdroid.jenkins.scheduler;

import com.testdroid.api.model.APIUser;

public interface TestRunFinishCheckScheduler {

    void schedule(final Object object, APIUser user, final Long projectId, final Long testRunId);

    void cancel(final Long projectId, final Long testRunId);

}
