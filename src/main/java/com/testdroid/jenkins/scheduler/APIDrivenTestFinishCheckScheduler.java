package com.testdroid.jenkins.scheduler;

import com.testdroid.api.APIException;
import com.testdroid.api.model.APITestRun;
import com.testdroid.jenkins.Messages;
import hudson.model.TaskListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class APIDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> taskHandle;

    private final TaskListener listener;

    public APIDrivenTestFinishCheckScheduler(TaskListener listener) {
        this.listener = listener;
    }

    public void schedule(final Object object, APITestRun apiTestRun) {
        final Runnable beeper = () -> {
            listener.getLogger().println(Messages.CHECK_FOR_TESTRUN_STATE(apiTestRun.getId()));
            if (checkResult(apiTestRun)) {
                synchronized (object) {
                    object.notify();
                }
            }
        };
        taskHandle = scheduler.scheduleAtFixedRate(beeper, 60, 45, TimeUnit.SECONDS);
    }

    public void cancel(final APITestRun apiTestRun) {
        if (taskHandle != null) {
            taskHandle.cancel(true);
        }
    }

    private boolean checkResult(APITestRun apiTestRun) {
        boolean result = false;
        try {
            apiTestRun.refresh();
            if (apiTestRun.getState() == APITestRun.State.FINISHED) {
                result = true;
            }
        } catch (APIException exc) {
            listener.getLogger().println(Messages.API_GET_TESTRUN_ERROR(apiTestRun.getId(), exc.getStackTrace()));
            result = true;
        }
        return result;
    }

}
