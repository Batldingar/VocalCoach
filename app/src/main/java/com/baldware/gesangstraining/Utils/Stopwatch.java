package com.baldware.gesangstraining.Utils;

import android.util.Log;

/**
 * A stopwatch tool for making performance testing easy
 */
public class Stopwatch {

    // Variables
    private static long mTime;

    // Flags
    private static final String TAG = "Stopwatch";

    // Enums
    public enum Unit {
        SEC,
        MILLI,
        MICRO,
        NANO
    }

    /**
     * Records the current time in mTime
     */
    public static void start() {
        mTime = System.nanoTime();
    }

    /**
     * Records the current time and logs the difference to mTime (time of last start() call)
     *
     * @param _unit The unit to be used for displaying the time difference
     */
    public static void stop(Unit _unit) {
        long delta = (System.nanoTime() - mTime);

        switch (_unit) {
            case SEC:
                Log.d(TAG, "Stopwatch-Time: " + (delta / 1000000000) + "s");
                break;
            case MILLI:
                Log.d(TAG, "Stopwatch-Time: " + (delta / 1000000) + "ms");
                break;
            case MICRO:
                Log.d(TAG, "Stopwatch-Time: " + (delta / 1000) + "µs");
                break;
            case NANO:
                Log.d(TAG, "Stopwatch-Time: " + (delta) + "ns");
                break;
        }
    }
}