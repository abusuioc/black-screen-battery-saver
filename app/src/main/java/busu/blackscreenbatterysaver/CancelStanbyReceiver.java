package busu.blackscreenbatterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by adibusu on 8/27/16.
 */

public class CancelStanbyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationsHelper.cancelStandbyNotification(context);
    }
}
