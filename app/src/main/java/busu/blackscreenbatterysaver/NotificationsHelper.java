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

    private NotificationCompat.Builder createBuilder(ChangeNotificationBody changer) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSmallIcon(getNotificationIcon());
        RemoteViews body = getComplexNotificationView();
        builder.setContent(body);

        PendingIntent intent1p2 = buildPendingIntentFor(TheService.ACTION_SIZE_1P2);
        PendingIntent intent1p3 = buildPendingIntentFor(TheService.ACTION_SIZE_1P3);
        PendingIntent intentFull = buildPendingIntentFor(TheService.ACTION_SIZE_FULL);
        PendingIntent intentStop = buildPendingIntentFor(TheService.ACTION_STOP);
        PendingIntent intentTutorial = buildPendingIntentFor(TheService.ACTION_TUTORIAL);

        body.setOnClickPendingIntent(R.id.notifSize1p2, intent1p2);
        body.setOnClickPendingIntent(R.id.notifSize1p3, intent1p3);
        body.setOnClickPendingIntent(R.id.notifSizeFull, intentFull);
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

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.notif_icon : R.mipmap.ic_launcher;
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

    public static class ChangeHeightSelection implements ChangeNotificationBody {
        private int mCurrentHeight;

        final int COUNT_HEIGHT_VIEWS = 3;

        final int[] heightViews = new int[]{R.id.notifSize1p2, R.id.notifSize1p3, R.id.notifSizeFull};
        final int[] heightDrawables = new int[]{R.drawable.bkg_1p2, R.drawable.bkg_1p3, R.drawable.bkg_full};
        final int[] heightDrawablesSel = new int[]{R.drawable.bkg_1p2_sel, R.drawable.bkg_1p3_sel, R.drawable.bkg_full_sel};
        final int[] heights = new int[]{Preferences.HOLE_HEIGHT_PERCENTAGE_1P2, Preferences.HOLE_HEIGHT_PERCENTAGE_1P3, Preferences.HOLE_HEIGHT_PERCENTAGE_FULL};

        ChangeHeightSelection(int currentHeight) {
            mCurrentHeight = currentHeight;
        }

        @Override
        public void alterBody(RemoteViews body) {
            for (int i = 0; i < COUNT_HEIGHT_VIEWS; i++) {
                if (mCurrentHeight == heights[i]) {
                    body.setInt(heightViews[i], "setBackgroundResource", heightDrawablesSel[i]);
                } else {
                    body.setInt(heightViews[i], "setBackgroundResource", heightDrawables[i]);
                }
            }
        }
    }
}