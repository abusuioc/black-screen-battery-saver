package busu.blackscreenbatterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickSettingsTileService extends TileService {
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        blackScotServiceStatusChangedTo(BlackScotService.state);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        blackScotServiceStatusChangedTo(BlackScotService.state);
        IntentFilter intentFilter = new IntentFilter(BlackScotService.EVENT_STATUS_CHANGED);
        ContextCompat.registerReceiver(this, blackScotServiceStatusReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        blackScotServiceStatusChangedTo(BlackScotService.state);
        unregisterReceiver(blackScotServiceStatusReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        switch (tile.getState()) {
            case Tile.STATE_ACTIVE:
                sendActionToBlackScotService(BlackScotService.Action.STOP_SERVICE);
                break;
            case Tile.STATE_INACTIVE:
                sendActionToBlackScotService(BlackScotService.Action.START_SERVICE);
                break;
            default:
        }
        // Updates to the tile state will be received trough the broadcast receiver.
    }

    private final BroadcastReceiver blackScotServiceStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (BlackScotService.EVENT_STATUS_CHANGED.equals(intent.getAction())) {
                    blackScotServiceStatusChangedTo(
                            (BlackScotService.State) intent.getSerializableExtra(BlackScotService.BROADCAST_CURRENT_STATE));
                }
            }
        }
    };

    private void blackScotServiceStatusChangedTo(BlackScotService.State currentState) {
        Tile tile = getQsTile();
        final boolean isStarted = (BlackScotService.State.ACTIVE == currentState);
        final boolean isEnabled = StarterActivity.canDrawOverlay(this);
        if (!isEnabled) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else if (isStarted) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    private void sendActionToBlackScotService(BlackScotService.Action action) {
        startService(new Intent(this, BlackScotService.class).setAction(action.getActionString()));
    }
}
