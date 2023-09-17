package busu.blackscreenbatterysaver;

import android.os.SystemClock;

public class SavingTracker {

    private int percentOfUsedScreenSinceStart;

    private int lastViewportHeightPercentage;
    private long lastTimestamp;
    private long startTimestamp;

    private boolean isInUse = false;

    private long getTimeDifferenceSince(long timestamp) {
        final long dif = getCurrentTime() - timestamp;
        return dif < 0 ? 0 : dif;
    }

    /**
     * To be called when the saving starts and every time there is a change in the viewport height.
     *
     * @param percentageOfScreenHeight
     */
    public void recordSaving(int percentageOfScreenHeight) {
        if (isInUse) {
            // the viewport height changed, recompute savings up till now
            percentOfUsedScreenSinceStart = computePercentOfScreenUsedSinceSavingStarted();
        } else {
            // first call to record savings (service started)
            startTimestamp = getCurrentTime();
            percentOfUsedScreenSinceStart = 0;
            isInUse = true;
        }
        lastViewportHeightPercentage = percentageOfScreenHeight;
        lastTimestamp = getCurrentTime();
    }

    private int computePercentOfScreenUsedSinceSavingStarted() {
        long durationSinceStart = getTimeDifferenceSince(startTimestamp);
        long durationSinceLastViewportHeightChange = getTimeDifferenceSince(lastTimestamp);
        long durationForWhichCurrentPercentageWasCalculated = durationSinceStart - durationSinceLastViewportHeightChange;
        return (int) updateWeightedAverage(
                percentOfUsedScreenSinceStart,
                durationForWhichCurrentPercentageWasCalculated,
                lastViewportHeightPercentage,
                durationSinceLastViewportHeightChange
        );
    }

    /**
     * Updates a weighted average with a new value:weight pair. To be used with positive numbers to avoid division by 0.
     *
     * @param currentWeightedAverage     the current weighted average to be updated
     * @param weightSumForCurrentAverage the sum of the weights used to far
     * @param value                      the new value to be added
     * @param weight                     the weight of this new value
     * @return the new weighted average
     */
    private long updateWeightedAverage(long currentWeightedAverage, long weightSumForCurrentAverage, long value, long weight) {
        if (weightSumForCurrentAverage + weight <= 0L) return currentWeightedAverage;
        else
            return (currentWeightedAverage * weightSumForCurrentAverage + value * weight) / (weightSumForCurrentAverage + weight);
    }

    /**
     * Called when the saving is done. Any further call to @recordSaving will reset the counter.
     */
    public void endSaving() {
        isInUse = false;
        percentOfUsedScreenSinceStart = computePercentOfScreenUsedSinceSavingStarted();
        lastTimestamp = getCurrentTime();
    }

    public int getPercentageOfScreenUsedSinceSavingStarted() {
        if (isInUse) {
            percentOfUsedScreenSinceStart = computePercentOfScreenUsedSinceSavingStarted();
            lastTimestamp = getCurrentTime();
        }
        return percentOfUsedScreenSinceStart;
    }

    public long getTotalSavingTime() {
        if (isInUse) {
            return getTimeDifferenceSince(startTimestamp);
        } else {
            return lastTimestamp - startTimestamp;
        }

    }

    private long getCurrentTime() {
        return SystemClock.uptimeMillis();
    }

}
