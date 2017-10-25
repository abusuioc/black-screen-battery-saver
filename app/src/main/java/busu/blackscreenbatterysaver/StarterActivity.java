package busu.blackscreenbatterysaver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by adibusu on 5/14/16.
 */
public class StarterActivity extends AppCompatActivity {

    public final static String TAG = "BSBS";

    public final static String ACTION_PREVENT_QUICKSTART = "com.busu.blackscreenbatterysaver.ACTION_PREVENT_QUICK";

    private Preferences mPrefs;

    private Button mBtnStartStop, mBtnTutorial;
    private TextView mStatus;
    private CheckBox mChkQuick, mChkSticky;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (TheService.EVENT_STATUS_CHANGED.equals(intent.getAction())) {
                    serviceStatusChanged(
                            (TheService.State) intent.getSerializableExtra(TheService.BROADCAST_CURRENT_STATE),
                            (TheService.State) intent.getSerializableExtra(TheService.BROADCAST_OLD_STATE));
                } /*else if (TheService.EVENT_PROPERTIES_CHANGED.equals(intent.getAction())) {
                }*/
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        mPrefs = new Preferences(this);
        if (hasToCancelActivityAndStartService(savedInstanceState != null)) {
            startTheService();
            return;
        }

        setContentView(R.layout.starter);

        mBtnStartStop = findViewById(R.id.sBtnStartStop);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TheService.state == TheService.State.ACTIVE) {
                    startTheService(TheService.ACTION_STOP, false);
                } else {
                    checkDrawOverlayPermission();
                }
            }
        });

        mBtnTutorial = findViewById(R.id.sBtnTutorial);
        mBtnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTheService(TheService.ACTION_TUTORIAL, true);
            }
        });

        mStatus = findViewById(R.id.sStatus);

        mChkQuick = findViewById(R.id.sChkQuickly);
        mChkQuick.setChecked(mPrefs.hasToQuickStart());
        mChkQuick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.setQuickStart(isChecked);
            }
        });

        mChkSticky = findViewById(R.id.sChkSticky);
        mChkSticky.setChecked(mPrefs.isStickyStandbyNotif());
        mChkSticky.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.setStickyStandby(isChecked);
            }
        });

        final TextView rate = findViewById(R.id.sTxtRate);
        rate.setPaintFlags(rate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppRating(StarterActivity.this);
            }
        });

        serviceStatusChanged(TheService.state, null);
    }

    private boolean hasToCancelActivityAndStartService(boolean isAfterConfigChange) {
        //call this so that it does the hack of getting first time ever starting the app a false and setting it to true for next uses
        boolean isQuick = mPrefs.hasToQuickStart();

        if (TheService.state == TheService.State.ACTIVE || !canDrawOverlay(this)) {
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

        if (isQuick && !ACTION_PREVENT_QUICKSTART.equals(startAction) && !isAfterConfigChange) {
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(TheService.EVENT_STATUS_CHANGED);
//        intentFilter.addAction(TheService.EVENT_PROPERTIES_CHANGED);
        registerReceiver(mReceiver, intentFilter);
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
                    setPositiveButton(R.string.overlay_proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQUEST_CODE);
                        }
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
        if (requestCode == REQUEST_CODE) {
            if (canDrawOverlay(this)) {
                startTheService();
            } else {
                Snackbar.make(mBtnStartStop, R.string.pleaseEnableOverlay, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void startTheService() {
        startTheService(TheService.ACTION_START, true);
    }

    /**
     * Start the service only on action start and send commands for the other actions only if the service is already started
     *
     * @param action
     * @param hasToCloseActivity
     */
    private void startTheService(String action, boolean hasToCloseActivity) {
        if (action == TheService.ACTION_START || TheService.state == TheService.State.ACTIVE) {
            startService(new Intent(StarterActivity.this, TheService.class).setAction(action));
            if (hasToCloseActivity) {
                finish();
            }
        }
    }

    private void serviceStatusChanged(TheService.State currentState, TheService.State oldState) {
        final boolean isStarted = (TheService.State.ACTIVE == currentState);
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