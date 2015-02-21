package com.github.stkent.ohnosnow.weatherapi;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.stkent.ohnosnow.R;
import com.github.stkent.ohnosnow.alarm.AlarmManagerHelper;
import com.github.stkent.ohnosnow.utils.NotificationsUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit.RestAdapter;

import static android.app.AlarmManager.RTC;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class WeatherUpdateService extends IntentService {

    private static final int PENDING_INTENT_REQUEST_CODE = 0x123;
    private static final String WUNDERGROUND_ROOT_URL = "http://api.wunderground.com/api/";

    /**
     * @return PendingIntent wrapping an Intent that can be used to start this service.
     */
    public static PendingIntent getPendingIntent(final Context context) {
        final Intent intent = new Intent(context, WeatherUpdateService.class);
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, intent, 0);
    }

    public WeatherUpdateService() {
        super("WeatherUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d(LOG_TAG, "Received intent from alarm, checking weather data.");

        setNextAlarm(this);

        final String endpoint = WUNDERGROUND_ROOT_URL + getString(R.string.wunderground_api_key);
        final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build();
        final WundergroundApi weatherApi = restAdapter.create(WundergroundApi.class);

        try {
            // note: blocking call
            final JsonElement weatherJsonElement = weatherApi.getWeatherData();
            final double predictedAccumulationInches = computeSnowAccumulation(weatherJsonElement);
            final String dateString = computeDateString(weatherJsonElement);

            if (predictedAccumulationInches > 0) {
                NotificationsUtil.showSnowNotification(this, dateString, predictedAccumulationInches);
            } else {
                NotificationsUtil.showNoShowNotification(this, dateString);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Caught exception while parsing weather data", e);
            NotificationsUtil.showFailureNotification(this);
        }
    }

    private void setNextAlarm(final Context context) {
        if (SDK_INT >= KITKAT) {
            final Calendar calendar = AlarmManagerHelper.getAlarmCalendar();
            final PendingIntent pendingIntent = WeatherUpdateService.getPendingIntent(context);
            AlarmManagerHelper.getAlarmManager(context).setExact(RTC, calendar.getTimeInMillis(), pendingIntent);

            Log.d(LOG_TAG, "On KitKat or newer, scheduling next alarm for " + calendar.getTime().toString());
        }
    }

    private double computeSnowAccumulation(final JsonElement weatherJsonElement) {
        final JsonObject forecastDay = getForecastDayJsonObject(weatherJsonElement);
        final JsonObject snowNight = forecastDay.getAsJsonObject("snow_night");
        return snowNight.get("in").getAsDouble();
    }

    private String computeDateString(final JsonElement weatherJsonElement) {
        final JsonObject forecastDay = getForecastDayJsonObject(weatherJsonElement);
        final JsonObject date = forecastDay.getAsJsonObject("date");
        final String epoch = date.getAsJsonPrimitive("epoch").getAsString();

        final Date forecastDate = new Date(Long.parseLong(epoch) * 1000);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return simpleDateFormat.format(forecastDate);
    }

    private JsonObject getForecastDayJsonObject(final JsonElement weatherJsonElement) {
        final JsonObject rootObject = weatherJsonElement.getAsJsonObject();
        final JsonObject forecast = rootObject.getAsJsonObject("forecast");
        final JsonObject simpleForecast = forecast.getAsJsonObject("simpleforecast");
        return simpleForecast.getAsJsonArray("forecastday").get(0).getAsJsonObject();
    }

}
