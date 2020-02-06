package com.testdroid.jenkins.utils;

import com.testdroid.api.APIClient;
import com.testdroid.api.APIException;
import com.testdroid.api.model.APICloudInfo;
import com.testdroid.api.model.APIUser;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiClientAdapter {

    private static final Logger LOGGER = Logger.getLogger(ApiClientAdapter.class.getName());

    private APIClient apiClient;

    public ApiClientAdapter(APIClient apiClient){
        this.apiClient = apiClient;
    }

    public String getCloudVersion() {
        try {
            APICloudInfo cloudInfo = apiClient.get("/info", APICloudInfo.class);
            return cloudInfo.getVersion();
        } catch (APIException e) {
            LOGGER.log(Level.WARNING, "APIException occurred when trying to get the cloud version.");
        }

        return null;
    }

    public APIUser getUser() {
        APIUser user = null;
        try {
            user = apiClient.me();
        } catch (APIException e) {
            LOGGER.log(Level.INFO, "ApiException occurred during get user from client.");
        }
        return user;
    }

    public void tryValidateConfig() throws APIException {
        if (getUser() == null) {
            throw new APIException("Couldn't retrieve Cloud user with the current settings!");
        }
    }

    public boolean isAuthenticated() {
        return getUser() != null;
    }

    /**
     * Static methods for managing instances of this class.
     */
    public static boolean isPaidUser(APIUser user) {
        if (user == null) return false;
        return user.getRoles().stream().anyMatch(r -> r.getName().startsWith("PRIORITY"));
    }
}
