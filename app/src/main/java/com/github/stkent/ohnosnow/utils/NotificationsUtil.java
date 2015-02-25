package com.github.stkent.ohnosnow.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.stkent.ohnosnow.R;

import static android.app.Notification.Builder;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.ACTION_VIEW;
import static com.github.stkent.ohnosnow.utils.Constants.LOG_TAG;

public class NotificationsUtil {

    private static final int APP_NOTIFICATION_ID = 0x456;

    public static void showSuccessNotification(final Context context, final String cityName, final String cityRequestUrl, final String dateString, final double snowInches) {
        Log.d(LOG_TAG, "Showing success notification.");

        final Intent weatherIntent = new Intent(ACTION_VIEW, Uri.parse("http://www.wunderground.com/" + cityRequestUrl));
        final PendingIntent weatherPendingIntent = PendingIntent.getActivity(context, 0, weatherIntent, 0);

        final String contentString = dateString + ": " + (snowInches > 0 ? snowInches + "\" of snow overnight." : "No overnight snow.");

        final Builder builder = new Builder(context)
                .setContentTitle(cityName)
                .setContentText(contentString)
                .setContentIntent(weatherPendingIntent)
                .setAutoCancel(true);

        notify(context, builder);
    }

    public static void showFailureNotification(final Context context) {
        Log.d(LOG_TAG, "Showing failure notification.");

        final Builder builder = new Builder(context)
                .setContentTitle("Uh oh!")
                .setContentText("Failed to retrieve weather data :(");

        notify(context, builder);
    }

    private static void notify(final Context context, final Builder builder) {
        builder.setSmallIcon(R.drawable.ic_notification);
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(APP_NOTIFICATION_ID, builder.build());
    }

}
