package busu.blackscreenbatterysaver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class StarterActivity extends AppCompatActivity {

    public final static String ACTION_PREVENT_QUICKSTART = "com.busu.blackscreenbatterysaver.ACTION_PREVENT_QUICK";

    private Button mBtnStartStop, mBtnTutorial;
    private TextView mStatus;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (BlackScotService.EVENT_STATUS_CHANGED.equals(intent.getAction())) {
                    serviceStatusChanged(
                            (BlackScotService.State) intent.getSerializableExtra(BlackScotService.BROADCAST_CURRENT_STATE));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        if (hasToCancelActivityAndStartService(savedInstanceState != null)) {
            startTheService();
            return;
        }

        setContentView(R.layout.starter);

        mBtnStartStop = findViewById(R.id.sBtnStartStop);
        mBtnStartStop.setOnClickListener(v -> {
            if (BlackScotService.state == BlackScotService.State.ACTIVE) {
                startTheService(BlackScotService.Action.STOP_SERVICE, false);
            } else {
                checkDrawOverlayPermission();
            }
        });

        mBtnTutorial = findViewById(R.id.sBtnTutorial);
        mBtnTutorial.setOnClickListener(v -> startTheService(BlackScotService.Action.SHOW_TUTORIAL, true));

        mStatus = findViewById(R.id.sStatus);

        final TextView rate = findViewById(R.id.sTxtRate);
        rate.setPaintFlags(rate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        rate.setOnClickListener(v -> openAppRating(StarterActivity.this));

        serviceStatusChanged(BlackScotService.state);
    }

    private boolean hasToCancelActivityAndStartService(boolean isAfterConfigChange) {
        if (BlackScotService.state == BlackScotService.State.ACTIVE || !canDrawOverlay(this)) {
            //start activity if service is already running: users want to configure smth
            return false;
        }

        Intent startIntent = getIntent();
        if (startIntent == null) {
            //this should never happen
            startIntent = new Intent(this, StarterActivity.class);
            startIntent.setAction(ACTION_PREVENT_QUICKSTART);
        }
        //extract action from intent so that the intent can be updated
        String startAction = startIntent.getAction();

        //once the activity started, prevent not starting after rotation
        startIntent.setAction(ACTION_PREVENT_QUICKSTART);
        setIntent(startIntent);

        return !ACTION_PREVENT_QUICKSTART.equals(startAction) && !isAfterConfigChange;
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BlackScotService.EVENT_STATUS_CHANGED);
        ContextCompat.registerReceiver(this, mReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    public final static int REQUEST_CODE = 1;

    public void checkDrawOverlayPermission() {
        if (!canDrawOverlay(this)) {
            new AlertDialog.Builder(this).setCancelable(true).
                    setMessage(R.string.overlay_enabling_dialog).
                    setPositiveButton(R.string.overlay_proceed, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE);
                    }).
                    setNegativeButton(R.string.overlay_cancel, null).
                    create().show();

        } else {
            startTheService();
        }
    }


    public static boolean canDrawOverlay(Context context) {
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (canDrawOverlay(this)) {
                startTheService();
            } else {
                Snackbar.make(mBtnStartStop, R.string.pleaseEnableOverlay, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void startTheService() {
        startTheService(BlackScotService.Action.START_SERVICE, true);
    }

    /**
     * Start the service only on action start and send commands for the other actions only if the service is already started.
     */
    private void startTheService(BlackScotService.Action action, boolean hasToCloseActivity) {
        if (action == BlackScotService.Action.START_SERVICE || BlackScotService.state == BlackScotService.State.ACTIVE) {
            startService(new Intent(StarterActivity.this, BlackScotService.class).setAction(action.getActionString()));
            if (hasToCloseActivity) {
                finish();
            }
        }
    }

    private void serviceStatusChanged(BlackScotService.State currentState) {
        final boolean isStarted = (BlackScotService.State.ACTIVE == currentState);
        //
        mBtnStartStop.setText(isStarted ? R.string.btn_stop : R.string.btn_start);
        mStatus.setText(isStarted ? R.string.status_started : R.string.status_stopped);
        mStatus.setTextColor(isStarted ? Color.GREEN : Color.RED);
        //
        if (isStarted) {
            mBtnTutorial.setVisibility(View.VISIBLE);
        } else {
            mBtnTutorial.setVisibility(View.GONE);
        }
    }

    public static void openAppRating(Context context) {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
            context.startActivity(webIntent);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}