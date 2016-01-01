package busu.blackscreenbatterysaver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Created by adibusu on 5/30/16.
 */
public class NotificationsHelper {

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
        builder.setSmallIcon(R.mipmap.ic_launcher);
        RemoteViews body = getComplexNotificationView();
        builder.setContent(body);

        PendingIntent intent1p2 = buildPendingIntentFor(TheService.ACTION_SIZE_1P2);
        PendingIntent intent1p3 = buildPendingIntentFor(TheService.ACTION_SIZE_1P3);
        PendingIntent intentStop = buildPendingIntentFor(TheService.ACTION_STOP);
        PendingIntent intentTutorial = buildPendingIntentFor(TheService.ACTION_TUTORIAL);

        body.setOnClickPendingIntent(R.id.notifSize1p2, intent1p2);
        body.setOnClickPendingIntent(R.id.notifSize1p3, intent1p3);
        body.setOnClickPendingIntent(R.id.notifStop, intentStop);
        body.setOnClickPendingIntent(R.id.notifSettings, buildShowSettingsPendingIntent());
        body.setOnClickPendingIntent(R.id.notifTutorial, intentTutorial);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        return builder;
    }

    private PendingIntent buildPendingIntentFor(String action) {
        Intent intent = new Intent(action, null, mService, TheService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mService, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent buildShowSettingsPendingIntent() {
        Intent intent = new Intent(mService, StarterActivity.class);
        intent.setAction(StarterActivity.ACTION_PREVENT_QUICKSTART);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private RemoteViews getComplexNotificationView() {
        RemoteViews notificationView = new RemoteViews(
                mService.getPackageName(),
                R.layout.notification);
        return notificationView;
    }

    public NotificationCompat.Builder buildServiceStarted() {
        NotificationCompat.Builder builder = createBuilder();
        builder.setTicker(mService.getResources().getString(R.string.service_started));
        return builder;
    }

    public void fireNotification(NotificationCompat.Builder builder) {
        mNotificationsManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancelNotification() {
        mNotificationsManager.cancel(NOTIFICATION_ID);
    }
}
