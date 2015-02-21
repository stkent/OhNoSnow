package com.github.stkent.ohnosnow.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.stkent.ohnosnow.alarm.AlarmManagerHelper;
import com.github.stkent.ohnosnow.sharedprefs.SharedPreferencesHelper;

import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            if (SharedPreferencesHelper.areNotificationsEnabled(context)) {
                Log.d(LOG_TAG, "Phone rebooted, restarting alarm.");
                AlarmManagerHelper.setAlarm(context);
            } else {
                Log.d(LOG_TAG, "Phone rebooted, no alarm to restart.");
            }
        }
    }
}
