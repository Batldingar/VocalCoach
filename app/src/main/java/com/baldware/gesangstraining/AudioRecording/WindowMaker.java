package com.baldware.gesangstraining.AudioRecording;

import java.util.Arrays;

/**
 * Creates windows with a certain size from an audio buffer
 * The class stores the buffer locally and cuts away windows until it is empty
 * Provides multiple window functions such as "applyRectangle"
 */
public class WindowMaker {

    // Variables & Data Containers
    private float[] mAudioBuffer;

    /**
     * Constructor
     *
     * @param _audioBuffer The audio buffer to be used for the window making
     */
    public WindowMaker(float[] _audioBuffer, int mWindowSize) {
        mAudioBuffer = Arrays.copyOf(_audioBuffer, mWindowSize);
    }

    /**
     * Applies a rectangle function on the first entries of mAudioBuffer
     * and removes the used values
     * Uses padding in case of valueCount being bigger
     * than the size of data left in mAudioBuffer
     *
     * @param _valueCount The number of values of mAudioBuffer to be used
     * @return An array holding the generated window
     */
    public float[] applyRectangle(int _valueCount) {
        float[] resultArray = new float[_valueCount];
        boolean appliedPadding = false;

        for (int i = 0; i < _valueCount; i++) {
            if (i < mAudioBuffer.length) {
                resultArray[i] = mAudioBuffer[i];
            } else {
                resultArray[i] = 0f;
                appliedPadding = true;
            }
        }

        if (!appliedPadding) {
            removeUsed(_valueCount);
        } else {
            removeUsed(mAudioBuffer.length);
        }

        return resultArray;
    }

    /**
     * Applies a hanning function on the first entries of mAudioBuffer
     * and removes the used values
     * Uses padding in case of valueCount being bigger
     * than the size of data left in mAudioBuffer
     *
     * @param _valueCount The number of values of mAudioBuffer to be used
     * @return An array holding the generated window
     */
    public float[] applyVonHann(int _valueCount) {
        float[] resultArray = new float[_valueCount];
        boolean appliedPadding = false;

        for (int i = 0; i < _valueCount; i++) {
            if (i < mAudioBuffer.length) {
                float vonHannWeight = (float) (0.5 * (1 - Math.cos(2.0 * Math.PI * i / (double) _valueCount)));
                resultArray[i] = mAudioBuffer[i] * vonHannWeight;
            } else {
                resultArray[i] = 0f;
                appliedPadding = true;
            }
        }

        if (!appliedPadding) {
            removeUsed(_valueCount);
        } else {
            removeUsed(mAudioBuffer.length);
        }

        return resultArray;
    }

    /**
     * Removes a given number of values from the beginning of mAudioBuffer
     *
     * @param _valueCount The number of values to be removed
     */
    private void removeUsed(int _valueCount) {
        float[] newAudioBuffer = new float[mAudioBuffer.length - _valueCount];

        for (int i = 0; i < newAudioBuffer.length; i++) {
            newAudioBuffer[i] = mAudioBuffer[i + _valueCount];
        }

        mAudioBuffer = newAudioBuffer;
    }

    /**
     * Computes whether mAudioBuffer is empty or not
     *
     * @return 1 is mAudioBuffer is empty
     * 0 otherwise
     */
    public boolean isEmpty() {
        return (mAudioBuffer.length == 0);
    }
}
