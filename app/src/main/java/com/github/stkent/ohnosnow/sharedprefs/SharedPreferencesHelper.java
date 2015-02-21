package com.github.stkent.ohnosnow.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class SharedPreferencesHelper {

    private static final String NOTIFICATIONS_ENABLED_KEY = "com.github.stkent.ohnosnow.notifications_enabled_key";
    private static final boolean NOTIFICATIONS_ENABLED_DEFAULT_STATE = false;

    public static void setNotificationsEnabled(final Context context, final boolean enabled) {
        Log.d(LOG_TAG, "Setting notifications enabled: " + enabled);
        getSharedPreferences(context).edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply();
    }

    public static boolean areNotificationsEnabled(final Context context) {
        return getSharedPreferences(context).getBoolean(NOTIFICATIONS_ENABLED_KEY, NOTIFICATIONS_ENABLED_DEFAULT_STATE);
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getApplicationContext().getSharedPreferences("", MODE_PRIVATE);
    }

}
