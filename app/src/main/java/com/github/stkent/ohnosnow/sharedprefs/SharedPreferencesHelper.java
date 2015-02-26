package com.github.stkent.ohnosnow.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class SharedPreferencesHelper {

    public enum NotificationMode {
        NONE("Off"),
        SNOW_AND_FAILURE_ONLY("Snow only"),
        ALL("Always");

        private String displayString;

        NotificationMode(final String displayString) {
            this.displayString = displayString;
        }

        @Override
        public String toString() {
            return displayString;
        }
    }

    private static final String NOTIFICATION_MODE_KEY = "com.github.stkent.ohnosnow.notification_mode_key";
    private static final NotificationMode DEFAULT_NOTIFICATION_MODE = NotificationMode.NONE;

    public static void setNotificationMode(final Context context, final NotificationMode newNotificationMode) {
        Log.d(LOG_TAG, "Setting notification mode: " + newNotificationMode);
        getSharedPreferences(context).edit().putInt(NOTIFICATION_MODE_KEY, newNotificationMode.ordinal()).apply();
    }

    public static NotificationMode getNotificationMode(final Context context) {
        return NotificationMode.values()[getSharedPreferences(context).getInt(NOTIFICATION_MODE_KEY, DEFAULT_NOTIFICATION_MODE.ordinal())];
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getApplicationContext().getSharedPreferences("", MODE_PRIVATE);
    }

}
