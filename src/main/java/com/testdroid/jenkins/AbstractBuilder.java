
package com.testdroid.jenkins;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.tasks.Builder;

import java.io.IOException;
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

public class AbstractBuilder extends Builder {

    private static final Logger LOGGER = Logger.getLogger(AbstractBuilder.class.getName());

    public static String applyMacro(AbstractBuild build, BuildListener listener, String macro) {
        try {
            EnvVars envVars = new EnvVars(Computer.currentComputer().getEnvironment());
            envVars.putAll(build.getEnvironment(listener));
            envVars.putAll(build.getBuildVariables());
            return Util.replaceMacro(macro, envVars);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to apply macro " + macro, e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to apply macro " + macro, e);
        }
        return macro;
    }
}
