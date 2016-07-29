package busu.blackscreenbatterysaver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.view.View;
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

    private NotificationCompat.Builder createBuilder(ChangeNotificationBody changer) {
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

        if (changer != null) {
            changer.alterBody(body);
        }
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

    private void fireNotification(NotificationCompat.Builder builder) {
        mNotificationsManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void startOrUpdateNotification(ChangeNotificationBody changer) {
        NotificationCompat.Builder builder = createBuilder(changer);
        fireNotification(builder);
    }


    public interface ChangeNotificationBody {
        void alterBody(RemoteViews body);
    }

    public void cancelNotification() {
        mNotificationsManager.cancel(NOTIFICATION_ID);
    }

    public static class ChangeVisibilityOfHeight implements ChangeNotificationBody {
        private boolean mIsHalf;

        ChangeVisibilityOfHeight(boolean isHeightHalfCurrently) {
            mIsHalf = isHeightHalfCurrently;
        }

        @Override
        public void alterBody(RemoteViews body) {
            if (mIsHalf) {
                body.setViewVisibility(R.id.notifSize1p2, View.GONE);
                body.setViewVisibility(R.id.notifSize1p3, View.VISIBLE);
            } else {
                body.setViewVisibility(R.id.notifSize1p2, View.VISIBLE);
                body.setViewVisibility(R.id.notifSize1p3, View.GONE);
            }
        }
    }
}
