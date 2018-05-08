package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.Messages;
import hudson.Plugin;
import hudson.model.Api;
import hudson.model.ModelObject;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST listener, that responds to hooks from the backend. We exploit the Jenkins instance to produce a singleton
 * listener class.
 *
 * Rationale:
 *  Extending "Plugin" gives us access to the my-jenkins-url:8080/plugin/my-special-plugin/api/json endpoint, which is
 *  why we use it here.
 *  We also implement "ModelObject" purely to listen for requests, even though the Jenkins API describes this as a
 *  means to "expose your plugin data".
 */

@ExportedBean()
public class HookUrlResultWaiter extends Plugin implements ModelObject {

    private static final Logger LOGGER = Logger.getLogger(HookUrlResultWaiter.class.getName());

    private static final Map<Long, Object> runIdWaitingObjectsMap = new HashMap<>();

    static void removeFromWaitList(Long testRunId) {
        runIdWaitingObjectsMap.remove(testRunId);
    }

    static void addToWaitList(Long testRunId, Object waitingObject) {
        runIdWaitingObjectsMap.put(testRunId, waitingObject);
    }

    private void notifyWaitingObject(Long testRunId) {
        Object waitingObject = runIdWaitingObjectsMap.get(testRunId);
        if (waitingObject != null) {
            synchronized (waitingObject) {
                waitingObject.notify();
            }
        }
    }

    @Override
    public String getDisplayName() {
        return Messages.PLUGIN_NAME();
    }

    public Api getApi() {
        return new TestdroidRunInCloudApi(this);
    }

    private static class TestdroidRunInCloudApi extends Api {

        private TestdroidRunInCloudApi(HookUrlResultWaiter thisPlugin) {
            super(thisPlugin);
        }

        /**
         * Process requests when Jenkins passes REST calls to this plugin's json endpoint.
         * Note: we only listen here. We don't produce a response with useful data.
         */
        @Override
        public void doJson(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
            HookUrlResultWaiter plugin = (HookUrlResultWaiter) bean;
            LOGGER.log(Level.INFO, String.format("%s received a REST request to the JSON endpoint.", plugin.getDisplayName()));
            if (req.getMethod().toLowerCase().equals("post") && req.hasParameter("testRunId")) {
                plugin.notifyWaitingObject(Long.parseLong(req.getParameter("testRunId")));
            } else {
                LOGGER.log(Level.INFO, String.format("%s: the request did not contain the parameter 'testRunId'. Ignoring.", plugin.getDisplayName()));
            }
        }
    }
}
