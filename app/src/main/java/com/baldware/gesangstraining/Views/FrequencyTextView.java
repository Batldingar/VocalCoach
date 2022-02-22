package com.baldware.gesangstraining.Views;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.baldware.gesangstraining.AudioRecording.AudioRecorder;
import com.baldware.gesangstraining.AudioRecording.MemoryHandler;
import com.baldware.gesangstraining.Utils.NoteHandler;

public class FrequencyTextView extends androidx.appcompat.widget.AppCompatTextView {

    // Variables & Data Containers
    private MemoryHandler mMemoryHandler;
    private final CountDownTimer mFetchAndRedrawTimer;
    private boolean mNoteView;

    // Flags
    private static final int UPDATE_COUNTDOWN_IN_MIL = 33; // 33ms = theoretically about 30FPS but the recorder will most likely collect data too slowly

    /**
     * Constructor
     *
     * @param _context      The context the view will be embedded in
     * @param _attributeSet The attribute set for the view
     */
    public FrequencyTextView(Context _context, @Nullable AttributeSet _attributeSet) {
        super(_context, _attributeSet);

        mNoteView = false;

        mFetchAndRedrawTimer = new CountDownTimer(UPDATE_COUNTDOWN_IN_MIL, UPDATE_COUNTDOWN_IN_MIL) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                fetchAndRedraw();
                mFetchAndRedrawTimer.start();
            }
        }.start();
    }

    /**
     * Sets the memory handler which stores the data to be displayed
     *
     * @param _memoryHandler The memory handler storing the data to be displayed
     */
    public void setMemoryHandler(MemoryHandler _memoryHandler) {
        mMemoryHandler = _memoryHandler;
    }

    /**
     * Sets mNoteView which determines whether to print the frequency or the note
     *
     * @param _noteView The boolean to be set
     */
    public void setNoteView(boolean _noteView) {
        this.mNoteView = _noteView;
    }

    /**
     * Returns mFetchAndRedrawTimer
     *
     * @return mFetchAndRedrawTimer
     */
    public CountDownTimer getFetchAndRedrawTimer() {
        return mFetchAndRedrawTimer;
    }

    /**
     * Fetches data from the mMemoryHandler and sets views text to be displayed
     */
    public void fetchAndRedraw() {
        if (mMemoryHandler == null || mMemoryHandler.getXSize() == 0) {
            return;
        }

        float[] newestSpectrum = mMemoryHandler.get(mMemoryHandler.getXSize() - 1);

        int binIndex = 0;

        for (int i = 0; i < newestSpectrum.length; i++) {
            if (newestSpectrum[i] == 1f) {
                binIndex = i;
                break;
            }
        }

        // Calculating the frequency of the bin with the highest amplitude
        float highestAmplitudeFrequency = (((AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ * binIndex) + (AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ * (binIndex + 1))) / 2f);

        if (!mNoteView) {
            setText("Frequency: " + highestAmplitudeFrequency + "Hz");
        } else {
            setText("Nearest note: " + NoteHandler.getNote(highestAmplitudeFrequency));
        }
    }
}
