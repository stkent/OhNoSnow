package com.github.stkent.ohnosnow.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.stkent.ohnosnow.R;
import com.github.stkent.ohnosnow.alarm.AlarmManagerHelper;
import com.github.stkent.ohnosnow.sharedprefs.SharedPreferencesHelper;

import static com.github.stkent.ohnosnow.sharedprefs.SharedPreferencesHelper.NotificationMode;
import static com.github.stkent.ohnosnow.sharedprefs.SharedPreferencesHelper.NotificationMode.NONE;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Spinner notificationModeSpinner = (Spinner) findViewById(R.id.notificationModeSpinner);

        final NotificationMode currentNotificationMode = SharedPreferencesHelper.getNotificationMode(this);
        Log.d(LOG_TAG, "Current notification mode: " + currentNotificationMode);

        notificationModeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, NotificationMode.values()));
        notificationModeSpinner.setSelection(currentNotificationMode.ordinal());
        notificationModeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final NotificationMode selectedNotificationMode = (NotificationMode) parent.getSelectedItem();
        SharedPreferencesHelper.setNotificationMode(this, selectedNotificationMode);

        AlarmManagerHelper.cancelAlarm(this);
        if (selectedNotificationMode != NONE) {
            AlarmManagerHelper.setAlarm(this);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
