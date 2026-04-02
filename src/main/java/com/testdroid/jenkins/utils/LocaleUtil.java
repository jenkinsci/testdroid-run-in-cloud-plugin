package com.testdroid.jenkins.utils;

import java.util.Locale;

public class LocaleUtil {

    private LocaleUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String formatLangCode(Locale locale) {
        return String.format("%s_%s", locale.getLanguage(), locale.getCountry());
    }
}
