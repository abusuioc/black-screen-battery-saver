package busu.com.blackscreenbatterysaver;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Created by adibusu on 5/30/16.
 */
public class NotificationsHelper {

    private final static int NOTIFICATION_ID = 998822;

    private Service mService;

    NotificationsHelper(Service theService) {
        mService = theService;
    }

    private NotificationCompat.Builder createBuilder() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // build a complex notification, with buttons and such
            //
            builder.setContent(getComplexNotificationView());
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

    private RemoteViews getComplexNotificationView() {
        RemoteViews notificationView = new RemoteViews(
                mService.getPackageName(),
                R.layout.notification
        );

        return notificationView;
    }

    public NotificationCompat.Builder buildServiceStarted() {
        NotificationCompat.Builder builder = createBuilder();
        builder.setTicker("Service started");
        return builder;
    }

    public void fireNotification(NotificationCompat.Builder builder) {
        NotificationManager mNotifyMgr =
                (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(NOTIFICATION_ID, builder.build());
    }
}
