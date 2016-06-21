package busu.com.blackscreenbatterysaver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Created by adibusu on 5/30/16.
 */
public class NotificationsHelper {

    public final static String ACTION_SIZE_1P2 = "1p2";
    public final static String ACTION_SIZE_1P3 = "1p3";
    public final static String ACTION_POS_TOP = "p_top";
    public final static String ACTION_POS_CENTER = "p_center";
    public final static String ACTION_POS_BOTTOM = "p_bottom";
    public final static String ACTION_STOP = "stop";
    public final static String ACTION_START = "start";

    private final static int NOTIFICATION_ID = 998822;

    private Service mService;
    private NotificationManager mNotificationsManager;

    public NotificationsHelper(Service theService) {
        mService = theService;
        mNotificationsManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private NotificationCompat.Builder createBuilder() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // build a complex notification, with buttons and such
            //
            RemoteViews body = getComplexNotificationView();
            builder.setContent(body);

            PendingIntent intent1p2 = buildPendingIntentFor(ACTION_SIZE_1P2);
            PendingIntent intent1p3 = buildPendingIntentFor(ACTION_SIZE_1P3);
            PendingIntent intentPtop = buildPendingIntentFor(ACTION_POS_TOP);
            PendingIntent intentPcenter = buildPendingIntentFor(ACTION_POS_CENTER);
            PendingIntent intentPbottom = buildPendingIntentFor(ACTION_POS_BOTTOM);
            PendingIntent intentStop = buildPendingIntentFor(ACTION_STOP);

            body.setOnClickPendingIntent(R.id.notifSize1p2, intent1p2);
            body.setOnClickPendingIntent(R.id.notifSize1p3, intent1p3);
            body.setOnClickPendingIntent(R.id.notifPosUp, intentPtop);
            body.setOnClickPendingIntent(R.id.notifPosCenter, intentPcenter);
            body.setOnClickPendingIntent(R.id.notifPosDown, intentPbottom);
            body.setOnClickPendingIntent(R.id.notifStop, intentStop);
        } else {
            // Build a simpler notification, without buttons
            //
            builder.setContentTitle("title")
                    .setContentText("content");
        }
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        return builder;
    }

    private PendingIntent buildPendingIntentFor(String action) {
        Intent intent = new Intent(action, null, mService, TheService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mService, 0, intent, 0);
        return pendingIntent;
    }

    private RemoteViews getComplexNotificationView() {
        RemoteViews notificationView = new RemoteViews(
                mService.getPackageName(),
                R.layout.notification);

        return notificationView;
    }

    public NotificationCompat.Builder buildServiceStarted() {
        NotificationCompat.Builder builder = createBuilder();
        builder.setTicker("Service started");
        return builder;
    }

    public void fireNotification(NotificationCompat.Builder builder) {
        mNotificationsManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancelNotification() {
        mNotificationsManager.cancel(NOTIFICATION_ID);
    }
}
