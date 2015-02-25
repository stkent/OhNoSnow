package com.github.stkent.ohnosnow.weatherapi;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.stkent.ohnosnow.R;
import com.github.stkent.ohnosnow.alarm.AlarmManagerHelper;
import com.github.stkent.ohnosnow.utils.NotificationsUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static java.util.concurrent.TimeUnit.SECONDS;

public class WeatherUpdateService extends IntentService implements LocationListener {

    private static final int PENDING_INTENT_REQUEST_CODE = 0x123;
    private static final String WEATHER_API_ROOT_URL = "http://api.wunderground.com/api/";

    /**
     * @return PendingIntent wrapping an Intent that can be used to start this service.
     */
    public static PendingIntent getPendingIntent(final Context context) {
        final Intent intent = new Intent(context, WeatherUpdateService.class);
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, intent, 0);
    }

    private GoogleApiClient googleApiClient;

    public WeatherUpdateService() {
        super("WeatherUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d(LOG_TAG, "Received intent from alarm, checking weather data.");

        setNextAlarm(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        final ConnectionResult connectionResult = googleApiClient.blockingConnect(10, SECONDS);

        if (connectionResult.isSuccess()) {
            Log.d(LOG_TAG, "Connected to Google Play Services");
            getUserLocation();
        } else {
            Log.e(LOG_TAG, "Connection to Google Play Services failed.");
            NotificationsUtil.showFailureNotification(this);
        }
    }

    private void getUserLocation() {
        final FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
        final Location location = locationProvider.getLastLocation(googleApiClient);

        if (location != null) {
            Log.d(LOG_TAG, "Last known location: " + location);
            googleApiClient.disconnect();
            getWeatherDataForLocation(location);
        } else {
            final LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
            locationProvider.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private void getWeatherDataForLocation(@NonNull final Location location) {
        final String endpoint = WEATHER_API_ROOT_URL + getString(R.string.wunderground_api_key);
        final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build();
        //        final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).setLogLevel(FULL).setLog(new AndroidLog(LOG_TAG)).build();
        final WundergroundApi weatherApi = restAdapter.create(WundergroundApi.class);

        try {
            final JsonElement locationJsonElement = weatherApi.getCityName(location.getLatitude(), location.getLongitude());
            final String cityName = computeCityName(locationJsonElement);
            final String cityRequestUrl = computeCityRequestUrl(locationJsonElement);

            final JsonElement weatherJsonElement = weatherApi.getWeatherData(cityRequestUrl.replaceAll("html", "json"));
            final double snowInches = computeOvernightSnowAccumulation(weatherJsonElement);
            final String dateString = computeDateString(weatherJsonElement);

            NotificationsUtil.showSuccessNotification(this, cityName, cityRequestUrl, dateString, snowInches);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Caught exception while parsing weather api data", e);
            NotificationsUtil.showFailureNotification(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location received, removing updates.");

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        googleApiClient.disconnect();
        getWeatherDataForLocation(location);
    }

    private void setNextAlarm(final Context context) {
        if (SDK_INT >= KITKAT) {
            final Calendar calendar = AlarmManagerHelper.getAlarmCalendar();
            final PendingIntent pendingIntent = WeatherUpdateService.getPendingIntent(context);
            AlarmManagerHelper.getAlarmManager(context).setExact(RTC, calendar.getTimeInMillis(), pendingIntent);

            Log.d(LOG_TAG, "On KitKat or newer, scheduling next alarm for " + calendar.getTime().toString());
        }
    }

    private JsonObject getLocationJsonObject(JsonElement locationJsonElement) {
        final JsonObject rootObject = locationJsonElement.getAsJsonObject();
        return rootObject.getAsJsonObject("location");
    }

    private String computeCityName(JsonElement locationJsonElement) {
        final JsonObject location = getLocationJsonObject(locationJsonElement);
        return location.get("city").getAsString();
    }

    private String computeCityRequestUrl(final JsonElement locationJsonElement) {
        final JsonObject location = getLocationJsonObject(locationJsonElement);
        return location.get("requesturl").getAsString();
    }

    private JsonObject getForecastDayJsonObject(final JsonElement weatherJsonElement) {
        final JsonObject rootObject = weatherJsonElement.getAsJsonObject();
        final JsonObject forecast = rootObject.getAsJsonObject("forecast");
        final JsonObject simpleForecast = forecast.getAsJsonObject("simpleforecast");
        return simpleForecast.getAsJsonArray("forecastday").get(0).getAsJsonObject();
    }

    private double computeOvernightSnowAccumulation(final JsonElement weatherJsonElement) {
        final JsonObject forecastDay = getForecastDayJsonObject(weatherJsonElement);
        final JsonObject snowNight = forecastDay.getAsJsonObject("snow_night");
        return snowNight.get("in").getAsDouble();
    }

    private String computeDateString(final JsonElement weatherJsonElement) {
        final JsonObject forecastDay = getForecastDayJsonObject(weatherJsonElement);
        final JsonObject date = forecastDay.getAsJsonObject("date");
        final String epoch = date.get("epoch").getAsString();

        final Date forecastDate = new Date(Long.parseLong(epoch) * 1000);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return simpleDateFormat.format(forecastDate);
    }

}
