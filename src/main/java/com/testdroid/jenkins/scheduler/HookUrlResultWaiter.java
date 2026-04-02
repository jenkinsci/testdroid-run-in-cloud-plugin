package com.testdroid.jenkins.scheduler;

import com.testdroid.jenkins.Messages;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Plugin;
import hudson.model.Api;
import hudson.model.ModelObject;
import jakarta.servlet.http.HttpServletResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST listener, that responds to hooks from the backend. We exploit the Jenkins instance to produce a singleton
 * listener class.
 * Rationale:
 *  Extending "Plugin" gives us access to the my-jenkins-url:8080/plugin/my-special-plugin/api/json endpoint, which is
 *  why we use it here.
 *  We also implement "ModelObject" purely to listen for requests, even though the Jenkins API describes this as a
 *  means to "expose your plugin data".
 */

@ExportedBean
public class HookUrlResultWaiter extends Plugin implements ModelObject {

    private static final Logger LOGGER = Logger.getLogger(HookUrlResultWaiter.class.getName());

    private static final Map<Long, Object> runIdWaitingObjectsMap = new HashMap<>();

    static void removeFromWaitList(Long testRunId) {
        runIdWaitingObjectsMap.remove(testRunId);
    }

    static void addToWaitList(Long testRunId, Object waitingObject) {
        runIdWaitingObjectsMap.put(testRunId, waitingObject);
    }

    @SuppressFBWarnings("NN_NAKED_NOTIFY")
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

    public static class TestdroidRunInCloudApi extends Api {

        private TestdroidRunInCloudApi(HookUrlResultWaiter thisPlugin) {
            super(thisPlugin);
        }

        /**
         * Process requests when Jenkins passes REST calls to this plugin's json endpoint.
         * Note: we only listen here. We don't produce a response with useful data, always http 200 status code.
         */
        @POST
        @RequirePOST
        @Override
        @SuppressWarnings("lgtm[jenkins/no-permission-check]")
        public void doJson(StaplerRequest2 req, StaplerResponse2 resp) {
            HookUrlResultWaiter plugin = (HookUrlResultWaiter) bean;
            String pluginDisplayName = plugin.getDisplayName();
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, Messages.RECEIVED_REST_JSON(pluginDisplayName));
            }
            if (req.getMethod().equalsIgnoreCase("post") && req.hasParameter("testRunId")) {
                try {
                    plugin.notifyWaitingObject(Long.parseLong(req.getParameter("testRunId")));
                }  catch (NumberFormatException ignored) {
                    // Ignore invalid testRunId parameter
                }
            } else if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, Messages.REQUEST_NEEDS_TESTRUNID(pluginDisplayName));
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
