package com.testdroid.jenkins.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Class to safely compare version strings like "2.40", "0.1.1", "1.1-beta", etc.
 *
 * 1. Blank versions considered to be lower.
 * 2. For non-numeric parts (eg. "beta"), a string compare is performed
 */

public class VersionString implements Comparable<VersionString> {

    private final String version;

    public VersionString(String version) {
        this.version = StringUtils.defaultIfBlank(version, StringUtils.EMPTY);
    }

    private String zeroPad(String s, int len) {
        return StringUtils.leftPad(s, len, '0');
    }

    @Override
    public int compareTo(VersionString v2) {
        // splits versions by dots and dashes
        String[] parts1 = this.version.split("\\.|-");
        String[] parts2 = v2.version.split("\\.|-");

        // find the longest individual part
        int maxLen1 = Arrays.stream(parts1).max(Comparator.comparingInt(String::length)).get().length();
        int maxLen2 = Arrays.stream(parts2).max(Comparator.comparingInt(String::length)).get().length();
        int maxLen = Math.max(maxLen1, maxLen2);

        // pad parts with zeros and perform string compare
        int i = 0;
        int comparison = 0;
        while (i < parts1.length && i < parts2.length && comparison == 0) {
            comparison = zeroPad(parts1[i], maxLen).compareTo(zeroPad(parts2[i], maxLen));
            i++;
        }

        return (comparison != 0) ? comparison : Integer.compare(parts1.length, parts2.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionString that = (VersionString) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
