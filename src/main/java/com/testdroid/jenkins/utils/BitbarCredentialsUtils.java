package com.testdroid.jenkins.utils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.testdroid.jenkins.auth.BitbarCredentials;
import com.testdroid.jenkins.auth.IBitbarCredentials;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Artur Ćwikliński <artur.cwiklinski@smartbear.com>
 */
public final class BitbarCredentialsUtils {

    private BitbarCredentialsUtils() {
        throw new IllegalStateException("Utility class");
    }

    @Nonnull
    public static BitbarCredentials getBitbarCredentials(String credentialsId) {
        if (credentialsId == null) {
            return BitbarCredentials.EMPTY;
        }
        List<IBitbarCredentials> credentialsList = CredentialsProvider.lookupCredentials(
                IBitbarCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList());
        return Optional.ofNullable(
                        CredentialsMatchers.firstOrNull(credentialsList, CredentialsMatchers.withId(credentialsId)))
                .map(IBitbarCredentials::getCredentials).orElse(BitbarCredentials.EMPTY);
    }

    @Nonnull
    public static String getSecretText(Secret secret) {
        return Objects.nonNull(secret) ? secret.getPlainText() : StringUtils.EMPTY;
    }
}
