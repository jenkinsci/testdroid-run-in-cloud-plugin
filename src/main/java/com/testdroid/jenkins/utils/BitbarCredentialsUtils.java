package com.testdroid.jenkins.utils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.testdroid.jenkins.auth.BitbarCredentials;
import com.testdroid.jenkins.auth.IBitbarCredentials;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;

public final class BitbarCredentialsUtils {

    private BitbarCredentialsUtils() {
        throw new IllegalStateException("Utility class");
    }

    @Nonnull
    public static BitbarCredentials getBitbarCredentials(String credentialsId) {
        if (Objects.isNull(credentialsId)) {
            return BitbarCredentials.EMPTY;
        }

        return Optional.ofNullable(getBitbarCredentialsWrapper(credentialsId))
                .map(IBitbarCredentials::getCredentials)
                .orElse(BitbarCredentials.EMPTY);
    }

    @Nullable
    public static IBitbarCredentials createBitbarCredentialsWrapperSnapshot(String credentialsId) {
        IBitbarCredentials credentials = getBitbarCredentialsWrapper(credentialsId);
        return nonNull(credentials) ? CredentialsProvider.snapshot(IBitbarCredentials.class, credentials) : null;
    }

    @Nullable
    public static IBitbarCredentials getBitbarCredentialsWrapper(String credentialsId) {
        if (Objects.isNull(credentialsId)) {
            return null;
        }
        List<IBitbarCredentials> credentialsList = CredentialsProvider.lookupCredentials(
                IBitbarCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList());
        return CredentialsMatchers.firstOrNull(credentialsList, CredentialsMatchers.withId(credentialsId));
    }
}
