package com.github.stkent.ohnosnow.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.github.stkent.ohnosnow.R;
import com.github.stkent.ohnosnow.alarm.AlarmManagerHelper;
import com.github.stkent.ohnosnow.sharedprefs.SharedPreferencesHelper;

import static android.widget.CompoundButton.OnCheckedChangeListener;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class MainActivity extends ActionBarActivity implements OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final boolean notificationsEnabled = SharedPreferencesHelper.areNotificationsEnabled(this);
        Log.d(LOG_TAG, "Notifications currently enabled: " + notificationsEnabled);

        final Switch notificationsSwitch = (Switch) findViewById(R.id.notifications_switch);
        notificationsSwitch.setChecked(notificationsEnabled);
        notificationsSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferencesHelper.setNotificationsEnabled(this, isChecked);

        if (isChecked) {
            AlarmManagerHelper.setAlarm(this);
        } else {
            AlarmManagerHelper.cancelAlarm(this);
        }
    }

}
