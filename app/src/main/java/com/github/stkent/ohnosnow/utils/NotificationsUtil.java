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

    public static void showSnowNotification(final Context context, final String dateString, final double predictedAccumulationInches) {
        Log.d(LOG_TAG, "Showing snow notification.");

        final Builder builder = new Builder(context)
                .setContentTitle("Snow!")
                .setContentText(dateString + ": " + predictedAccumulationInches + " inches of snow expected overnight");

        notify(context, builder);
    }

    public static void showNoShowNotification(final Context context, final String dateString) {
        Log.d(LOG_TAG, "Showing no snow notification.");

        final Builder builder = new Builder(context)
                .setContentTitle("No snow!")
                .setContentText(dateString + ": no overnight snow predicted.");

        notify(context, builder);
    }

    public static void showFailureNotification(final Context context) {
        Log.d(LOG_TAG, "Showing failure notification.");

        final Builder builder = new Builder(context)
                .setContentTitle("Uh oh!")
                .setContentText("Failed to retrieve weather forecast.");

        notify(context, builder);
    }

    private static void notify(final Context context, final Builder builder) {
        builder.setSmallIcon(R.drawable.ic_notification);

        final Intent wundergroundIntent = new Intent(ACTION_VIEW, Uri.parse("http://www.wunderground.com/cgi-bin/findweather/hdfForecast?query=Ferndale%2C+MI"));
        final PendingIntent wundergroundPendingIntent = PendingIntent.getActivity(context, 0, wundergroundIntent, 0);
        builder.setContentIntent(wundergroundPendingIntent);
        builder.setAutoCancel(true);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(APP_NOTIFICATION_ID, builder.build());
    }

}
