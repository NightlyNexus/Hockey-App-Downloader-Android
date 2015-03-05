package com.nightlynexus.hockey.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferencesUtils {

    // names of preference files
    private static final String NAME_APP = SharedPreferencesUtils.class.getName()
        + "." + "NAME_APP";
    private static final String NAME_USER = SharedPreferencesUtils.class.getName()
            + "." + "NAME_USER";

    // keys for preferences
    private static final String KEY_API_KEY = SharedPreferencesUtils.class.getName()
            + "." + "KEY_API_KEY";

    public static void clearPreferencesApp(Context context) {
        final SharedPreferences prefs = context
                .getSharedPreferences(NAME_APP,
                        Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static void clearPreferencesUser(Context context) {
        final SharedPreferences prefs = context
                .getSharedPreferences(NAME_USER,
                        Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static void clearApiKey(Context context) {
        final SharedPreferences prefs = context
                .getSharedPreferences(NAME_USER,
                        Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_API_KEY).apply();
    }

    public static void setApiKey(Context context, String apiKey) {
        final SharedPreferences prefs = context
                .getSharedPreferences(NAME_USER,
                        Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_API_KEY, apiKey).apply();
    }

    public static String getApiKey(Context context) {
        final SharedPreferences prefs = context
                .getSharedPreferences(NAME_USER,
                        Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_KEY, null);
    }

    private SharedPreferencesUtils() {
    }
}
