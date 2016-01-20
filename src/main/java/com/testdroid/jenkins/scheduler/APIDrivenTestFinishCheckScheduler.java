package com.testdroid.jenkins.scheduler;

import com.testdroid.api.APIException;
import com.testdroid.api.model.APITestRun;
import com.testdroid.api.model.APIUser;
import com.testdroid.jenkins.Messages;
import com.testdroid.jenkins.TestdroidCloudSettings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class APIDrivenTestFinishCheckScheduler implements TestRunFinishCheckScheduler {

    public static final Logger LOGGER = Logger.getLogger(APIDrivenTestFinishCheckScheduler.class.getName());

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> taskHandle;

    public void schedule(final Object object, final Long projectId, final Long testRunId) {
        final Runnable beeper = new Runnable() {
            public void run() {
                LOGGER.info(Messages.CHECK_FOR_TESTRUN_STATE(testRunId));
                if (checkResult(projectId, testRunId)) {
                    synchronized (object) {
                        object.notify();
                    }
                }
            }
        };
        taskHandle = scheduler.scheduleAtFixedRate(beeper, 60, 60, TimeUnit.SECONDS);
    }

    public void cancel(final Long projectId, final Long testRunId) {
        if (taskHandle != null) {
            taskHandle.cancel(true);
        }
    }

    private boolean checkResult(final Long projectId, final Long testRunId) {
        boolean result = false;
        try {
            APIUser user = TestdroidCloudSettings.descriptor().getUser();
            APITestRun testRun = user.getProject(projectId).getTestRun(testRunId);
            if (testRun.getState() == APITestRun.State.FINISHED) {
                result = true;
            }
        } catch (APIException exc) {
            LOGGER.log(Level.SEVERE, Messages.API_GET_TESTRUN_ERROR(testRunId), exc);
        }
        return result;
    }

}
