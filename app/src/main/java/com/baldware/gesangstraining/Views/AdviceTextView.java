package com.baldware.gesangstraining.Views;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.baldware.gesangstraining.AudioRecording.AudioRecorder;
import com.baldware.gesangstraining.AudioRecording.MemoryHandler;
import com.baldware.gesangstraining.R;

import java.text.DecimalFormat;

public class AdviceTextView extends androidx.appcompat.widget.AppCompatTextView {

    // Variables & Data Containers
    private MemoryHandler mMemoryHandler;
    private final CountDownTimer mFetchAndRedrawTimer;
    private Mode mMode;

    // Flags
    private static final int UPDATE_COUNTDOWN_IN_MIL = 33; // 33ms = theoretically about 30FPS but the recorder will most likely collect data too slowly

    // Enums
    public enum Mode {
        SINGER_FORMANT,
        NOISE
    }

    /**
     * Constructor
     *
     * @param _context      The context the view will be embedded in
     * @param _attributeSet The attribute set for the view
     */
    public AdviceTextView(Context _context, @Nullable AttributeSet _attributeSet) {
        super(_context, _attributeSet);

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
     * Sets the advice mode
     *
     * @param _mode The mode to be selected
     */
    public void setAdviceMode(Mode _mode) {
        mMode = _mode;
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
        if (mMemoryHandler == null || mMemoryHandler.getXSize() == 0 || mMode == null) {
            return;
        }

        float[] newestSpectrum = mMemoryHandler.get(mMemoryHandler.getXSize() - 1);

        switch (mMode) {
            case SINGER_FORMANT:
                float energyPercentage = frequencyBandEnergy(newestSpectrum, 2600, 3400);
                setText("Singer formant: " + new DecimalFormat("#.##").format(energyPercentage) + "%");

                if (energyPercentage > 7.5f) {
                    setBackgroundColor(AdviceTextView.this.getResources().getColor(R.color.green, null));
                } else {
                    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey));
                }
                break;
            case NOISE:
                float noisePercentage = frequencyBandEnergy(newestSpectrum, 3400, (newestSpectrum.length - 1) * AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ);
                setText("Noise: " + new DecimalFormat("#.##").format(noisePercentage) + "%");

                if (noisePercentage > 5f) {
                    setBackgroundColor(AdviceTextView.this.getResources().getColor(R.color.red, null));
                } else {
                    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey));
                }
                break;
        }
    }

    private float frequencyBandEnergy(float[] _spectrum, int _lowerBoundFrequency, int _higherBoundFrequency) {
        int lowerBoundBin = _lowerBoundFrequency / AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ;
        int upperBoundBin = _higherBoundFrequency / AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ;

        float windowEnergy = 0;
        for (int i = lowerBoundBin; i < upperBoundBin; i++) {
            windowEnergy += _spectrum[i];
        }

        float spectrumEnergy = 0;
        for (int i = 0; i < _spectrum.length; i++) {
            spectrumEnergy += _spectrum[i];
        }

        return (windowEnergy / (spectrumEnergy / 100f));
    }
}
