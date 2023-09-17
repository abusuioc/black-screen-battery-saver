package busu.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class BlackScotService extends Service implements ViewportController.ViewportInteractionListener {

    enum Action {
        STOP_SERVICE("stop"),
        START_SERVICE("start"),
        SHOW_TUTORIAL("tut");

        private final String mAction;

        Action(String action) {
            mAction = action;
        }

        public String getActionString() {
            return mAction;
        }

        @NonNull
        public static Action fromAcceptedActions(String value) {
            for (Action type : values()) {
                if (type.getActionString().equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Cannot create from " + value);
        }
    }

    public static State state = State.STOPPED;
    public final static String EVENT_STATUS_CHANGED = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";
    public final static String BROADCAST_CURRENT_STATE = "cst";

    private Preferences mPrefs;

    private ViewportController mVpCtrl;

    private SavingTracker mSavingTracker;

    private int mTutorialStep = 0;
    private static final int[] TUTORIAL_STEPS = {
            R.string.tutorial1,
            R.string.tutorial2,
            R.string.tutorial3,
            R.string.tutorial4,
            R.string.tutorial5,
            R.string.tutorial6
    };

    private final static int OPACITY_FULL = 100;
    private final static int OPACITY_SEE_THROUGH = 85;

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPrefs = new Preferences(this);
        mVpCtrl = new ViewportController(this, this);
        mSavingTracker = new SavingTracker();

        changeServiceState(State.STANDBY);
    }


    private void changeServiceState(State newState) {
        final State oldState = state;
        state = newState;
        applyCurrentServiceState(newState, oldState);
        LogUtil.logService("State changed from " + oldState + " to " + newState);
        sendBroadcast(new Intent(EVENT_STATUS_CHANGED)
                .setPackage(getPackageName())
                .putExtra(BROADCAST_CURRENT_STATE, newState));
    }

    private void applyCurrentServiceState(State currentState, State oldState) {
        switch (currentState) {
            case STANDBY:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                }
                break;
            case ACTIVE:
                addViewPort();
                mSavingTracker.recordSaving(mPrefs.getViewportHeight().getPercentage());
                break;
            case STOPPED:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                    showSavings();
                }
                stopSelf();
                break;
        }
    }

    @Override
    public void onDestroy() {
        changeServiceState(State.STOPPED);
        mVpCtrl = null;
        // Force end of ongoing tutorial because most likely users don't wanna see it again after a restart.
        endTutorial(false);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            executeAction(Action.fromAcceptedActions(intent.getAction()));
        } catch (Exception e) {
            LogUtil.logService("Unknown action from starting intent " + intent);
        }
        return START_STICKY;
    }

    private void executeAction(@NonNull Action action) {
        LogUtil.logService("Execute action: " + action + " in state: " + state);
        switch (action) {
            case STOP_SERVICE:
                changeServiceState(State.STOPPED);
                break;
            case START_SERVICE:
                changeServiceState(State.ACTIVE);
                break;
            case SHOW_TUTORIAL:
                mPrefs.setHasToShowTutorial(true);
                mTutorialStep = 0;
                mVpCtrl.showTutorial(TUTORIAL_STEPS[mTutorialStep]);
                break;
        }
    }

    private void changeViewportSize(Preferences.ViewportHeight size) {
        mSavingTracker.recordSaving(size.getPercentage());
        mPrefs.setViewportHeight(size);
        mVpCtrl.applyHoleHeightPercentage(size.getPercentage());
    }

    private void addViewPort() {
        mVpCtrl.applyHoleHeightPercentage(mPrefs.getViewportHeight().getPercentage());
        mVpCtrl.applyHoleVerticalGravity(mPrefs.getViewportGravity());
        mVpCtrl.setOpacity(mPrefs.isFullOpaque() ? OPACITY_FULL : OPACITY_SEE_THROUGH);
        if (mPrefs.hasToShowTutorial()) {
            mVpCtrl.showTutorial(TUTORIAL_STEPS[mTutorialStep]);
        }
        mVpCtrl.addToWindow();
    }

    private void removeViewPort() {
        mVpCtrl.removeFromWindow();
    }

    @Override
    public void onBlackClicked(ViewportController.ViewLayout black, float clickVerticalRatio) {
        if (mPrefs.getViewportHeight() == Preferences.ViewportHeight.ZERO) {
            //if black is full screen, the click will quickly restore a 1/2 viewport
            changeViewportSize(Preferences.ViewportHeight.HALF);
            incrementTutorial();
            LogUtil.logService("Click on: " + black.toString() + " while full screen black");
        } else {
            boolean isCenterRequested = (clickVerticalRatio <= 0.5f && black.gravity == Gravity.BOTTOM)
                    || (clickVerticalRatio > 0.5f && black.gravity == Gravity.TOP);
            mVpCtrl.changeHoleGravity(isCenterRequested, black.gravity);
            mPrefs.setViewportGravity(mVpCtrl.getHoleGravity());
            incrementTutorial();
            LogUtil.logService("Click on: " + black + ", center req: " + isCenterRequested + ", hole gravity: "
                    + ViewportController.getGravityString(mVpCtrl.getHoleGravity()));
        }
    }

    @Override
    public void onCloseClicked() {
        changeServiceState(State.STOPPED);
    }

    @Override
    public void onShowAppClicked() {
        Intent intent = new Intent(this, StarterActivity.class);
        intent.setAction(StarterActivity.ACTION_PREVENT_QUICKSTART);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onStartTutorialClicked() {
        executeAction(Action.SHOW_TUTORIAL);
    }

    @Override
    public void onSetHeightToZeroClicked() {
        changeViewportSize(Preferences.ViewportHeight.ZERO);
    }

    @Override
    public void onSetHeightToHalfClicked() {
        changeViewportSize(Preferences.ViewportHeight.HALF);
    }

    @Override
    public void onSetHeightToThirdClicked() {
        changeViewportSize(Preferences.ViewportHeight.THIRD);
    }

    @Override
    public void onSetTransparencyToOpaqueClicked() {
        mVpCtrl.setOpacity(OPACITY_FULL);
        mPrefs.setIsFullOpaque(true);
    }

    @Override
    public void onSetTransparencyToSeeThroughClicked() {
        mVpCtrl.setOpacity(OPACITY_SEE_THROUGH);
        mPrefs.setIsFullOpaque(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtil.logService("Configuration changed ");
        if (state == State.ACTIVE) {
            removeViewPort();
            mVpCtrl = new ViewportController(this, this);
            addViewPort();
        }
    }

    enum State {
        STOPPED,
        STANDBY,
        ACTIVE
    }

    private void incrementTutorial() {
        if (mPrefs.hasToShowTutorial()) {
            mTutorialStep++;
            if (mTutorialStep >= TUTORIAL_STEPS.length) {
                endTutorial(true);
            } else {
                mVpCtrl.showTutorial(TUTORIAL_STEPS[mTutorialStep]);
            }
        }
    }

    private void endTutorial(boolean hasToUpdateViewController) {
        mTutorialStep = TUTORIAL_STEPS.length;
        if (hasToUpdateViewController) {
            mVpCtrl.hideTutorial();
        }
        mPrefs.setHasToShowTutorial(false);
    }


    private void showSavings() {
        mSavingTracker.endSaving();
        int averagePercentage = mSavingTracker.getPercentageOfScreenUsedSinceSavingStarted();
        int totalSavingTimeInMinutes = (int) (mSavingTracker.getTotalSavingTime() / 60000);

        LogUtil.logService("Savings - duration: " + totalSavingTimeInMinutes + " & percentage = " + averagePercentage);

        final String message;
//        if (totalSavingTimeInMinutes <= 5) {
//            message = getString(R.string.saving_message_less_than_5minutes);
//        } else {
            final String percentageMessage = getSavingPercentageMessage(averagePercentage);
            message = getString(R.string.saving_message, totalSavingTimeInMinutes, percentageMessage);
//        }

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        View toastView = View.inflate(this, R.layout.saving_toast, null);
        ((TextView) toastView.findViewById(R.id.saving_text)).setText(message);
        toast.setView(toastView);
        toast.show();
    }

    private String getSavingPercentageMessage(int averagePercentage) {
        if (averagePercentage > 60) {
            return getString(R.string.saving_percentage_more_50);
        } else if (averagePercentage >= 40) {
            return getString(R.string.saving_percentage_around_50);
        } else if (averagePercentage >= 10) {
            return getString(R.string.saving_percentage_less_30);
        } else {
            return getString(R.string.saving_maximum);
        }
    }
}