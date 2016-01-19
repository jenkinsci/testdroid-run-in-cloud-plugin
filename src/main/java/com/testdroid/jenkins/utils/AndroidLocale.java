package com.testdroid.jenkins.utils;

import java.util.*;

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
public class AndroidLocale {

    public static final Locale[] LOCALES;

    static {
        SortedSet<Locale> languages = new TreeSet<Locale>(new Comparator<Locale>() {
            @Override
            public int compare(Locale o1, Locale o2) {
                if (o1.getDisplayLanguage().trim().compareTo(o2.getDisplayLanguage().trim()) == 0) {
                    return o1.getDisplayCountry().trim().compareTo(o2.getDisplayCountry().trim());
                } else {
                    return o1.getDisplayLanguage().trim().compareTo(o2.getDisplayLanguage().trim());
                }
            }
        });
        Collections.addAll(languages,
                new Locale("en", "US"), new Locale("de", "DE"),
                new Locale("zh", "CN"), new Locale("zh", "TW"),
                new Locale("cs", "CZ"), new Locale("nl", "BE"),
                new Locale("nl", "NL"), new Locale("en", "AU"),
                new Locale("en", "GB"), new Locale("en", "CA"),
                new Locale("en", "NZ"), new Locale("en", "SG"),
                new Locale("fr", "BE"), new Locale("fr", "CA"),
                new Locale("fr", "FR"), new Locale("fr", "CH"),
                new Locale("de", "AT"), new Locale("de", "LI"),
                new Locale("de", "CH"), new Locale("it", "IT"),
                new Locale("it", "CH"), new Locale("ja", "JP"),
                new Locale("ko", "KR"), new Locale("pl", "PL"),
                new Locale("ru", "RU"), new Locale("es", "ES"),
                new Locale("ar", "EG"), new Locale("ar", "IL"),
                new Locale("bg", "BG"), new Locale("ca", "ES"),
                new Locale("hr", "HR"), new Locale("da", "DK"),
                new Locale("en", "IN"), new Locale("en", "IE"),
                new Locale("en", "ZA"), new Locale("fi", "FI"),
                new Locale("el", "GR"), new Locale("iw", "IL"),
                new Locale("hi", "IN"), new Locale("hu", "HU"),
                new Locale("in", "ID"), new Locale("lv", "LV"),
                new Locale("lt", "LT"), new Locale("nb", "NO"),
                new Locale("pt", "BR"), new Locale("pt", "PT"),
                new Locale("ro", "RO"), new Locale("sr", "RS"),
                new Locale("sk", "SK"), new Locale("sl", "SI"),
                new Locale("es", "US"), new Locale("sv", "SE"),
                new Locale("tl", "PH"), new Locale("th", "TH"),
                new Locale("tr", "TR"), new Locale("uk", "UA"),
                new Locale("vi", "VN"));
        LOCALES = new Locale[languages.size()];
        languages.toArray(LOCALES);
    }
}
