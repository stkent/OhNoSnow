package com.github.stkent.ohnosnow.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import com.github.stkent.ohnosnow.weatherapi.WeatherUpdateService;

import java.util.Calendar;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.RTC;
import static android.content.Context.ALARM_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;
import static java.util.concurrent.TimeUnit.SECONDS;

public class AlarmManagerHelper {

    private static final int NOTIFICATION_HOUR = 19;

    public static AlarmManager getAlarmManager(final Context context) {
        return (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    public static Calendar getAlarmCalendar() {
        final Calendar now = Calendar.getInstance();
        final Calendar alarmCalendar = Calendar.getInstance();

//        alarmCalendar.set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR);
//        alarmCalendar.set(Calendar.MINUTE, 0);
//        alarmCalendar.set(Calendar.SECOND, 0);
//        alarmCalendar.set(Calendar.MILLISECOND, 0);

        alarmCalendar.add(Calendar.MINUTE, 1);

        // fudge factor to avoid potential issues when re-setting the alarm on KitKat+
        if (alarmCalendar.getTimeInMillis() < now.getTimeInMillis() + SECONDS.toMillis(1)) {
            alarmCalendar.add(Calendar.DATE, 1);
        }

        return alarmCalendar;
    }

    public static void setAlarm(final Context context) {
        final Calendar calendar = getAlarmCalendar();
        final PendingIntent pendingIntent = WeatherUpdateService.getPendingIntent(context);
        Log.d(LOG_TAG, "Initializing alarm for " + calendar.getTime().toString());

        if (SDK_INT >= KITKAT) {
            getAlarmManager(context).setExact(RTC, calendar.getTimeInMillis(), pendingIntent);
        } else {
            getAlarmManager(context).setRepeating(RTC, calendar.getTimeInMillis(), INTERVAL_DAY, pendingIntent);
        }
    }

    public static void cancelAlarm(final Context context) {
        Log.d(LOG_TAG, "Canceling alarm.");
        getAlarmManager(context).cancel(WeatherUpdateService.getPendingIntent(context));
    }

}
