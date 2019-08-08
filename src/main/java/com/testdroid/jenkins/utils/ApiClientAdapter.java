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

    public APIUser getUser() throws APIException {
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
        try {
            return getUser() != null;
        } catch (APIException e) {
            LOGGER.log(Level.WARNING, "isAuthenticated: Error when getting user.", e);
        }
        return false;
    }

    /**
     * Static methods for managing instances of this class.
     */
    public static boolean isPaidUser(APIUser user) {
        if (user == null) return false;
        return Arrays.stream(user.getRoles()).anyMatch(r -> r.getName().startsWith("PRIORITY"));
    }
}
