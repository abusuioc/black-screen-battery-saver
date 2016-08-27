package busu.blackscreenbatterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by adibusu on 6/19/16.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (StarterActivity.canDrawOverlay(context)) {
            final NotificationsHelper notifsHelper = new NotificationsHelper(context);
            notifsHelper.startStandbyNotification();
        }
    }
}
