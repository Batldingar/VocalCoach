package com.baldware.gesangstraining.AudioRecording;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.jtransforms.fft.FloatFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An audio recorder built upon java's AudioRecord
 * The class starts two threads, one of which handles audio recording
 * the other handles audio processing
 */
public class AudioRecorder {

    // Variables & Data Containers
    private AudioRecord mAudioRecord;
    private float[] mAudioBuffer;
    private final MemoryHandler mMemoryHandler;
    private ProcessingRunnable mProcessingRunnable;
    private boolean mRecording;
    private int mWindowSize;

    // Flags
    private static final String TAG = "AudioRecorder";
    private static final int SAMPLE_RATE_IN_HZ = 8000;                      // Samples per second recorded (-> changes height/width of custom views)
    public static final int FREQUENCY_RESOLUTION_IN_HZ = 5;                 // Delta between two frequency bins (-> changes accuracy at cost of performance)
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT; // 32 bit single precision (range: [-1.0, 1.0]

    /**
     * Constructor (creates an audio record instance and audio buffer)
     *
     * @param _activity The activity instance to be used
     *                  for the creation of an audio record instance
     *                  and for permission checking
     */
    public AudioRecorder(Activity _activity, MemoryHandler _memoryHandler) {
        mMemoryHandler = _memoryHandler;

        // Getting the minimum required buffer size for creation of an audio record instance in bytes
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);

        // Checking for errors
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.d(TAG, "Recording parameters are not supported by the hardware or an invalid parameter was used");
            return;
        } else {
            Log.d(TAG, "Determined minimum buffer size " + minBufferSize + " bytes.");
        }

        // Ensure minBufferSize is an even number for the stft to work
        if (minBufferSize % 2 != 0) {
            minBufferSize++;
        }

        // Checking for audio recording permissions
        if (ActivityCompat.checkSelfPermission(_activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for missing RECORD_AUDIO permission!");
            ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            return;
        }

        // Calculating the necessary window size
        mWindowSize = (int) Math.ceil((double) SAMPLE_RATE_IN_HZ / (double) FREQUENCY_RESOLUTION_IN_HZ);

        // Ensure mWindowSize is an even number for the stft to work
        if (mWindowSize % 2 != 0) {
            mWindowSize++;
        }

        // Trying to use the actually needed buffer size for one window instead of the minimum required
        int actualBufferSize = Math.max(minBufferSize, mWindowSize);

        // Creating an audio record instance
        try {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, actualBufferSize);
        } catch (IllegalArgumentException _e) {
            Log.d(TAG, "Failed to create AudioRecord instance!");
            _e.printStackTrace();
            return;
        }

        mAudioBuffer = new float[actualBufferSize];

        mRecording = false;
    }

    /**
     * Starts a separate thread that records audio data continuously
     * The data is being saved in the audio buffer
     * When the buffer is full the data gets sent to a processing thread
     * and the buffer is being cleared so that it can be filled again
     *
     * @return A flag determining whether the start was successful
     */
    public boolean startRecording() {
        if (mRecording || mAudioRecord == null || mAudioBuffer == null || mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING || mAudioRecord.getRecordingState() == AudioRecord.STATE_UNINITIALIZED) {
            return false;
        }

        // Starting the (continuously running) processing thread
        mProcessingRunnable = new ProcessingRunnable(mWindowSize, mMemoryHandler);
        Thread processingThread = new Thread(mProcessingRunnable);
        processingThread.start();

        mRecording = true;

        // Starting the audio recording thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

                try {
                    mAudioRecord.startRecording();
                } catch (IllegalStateException _e) {
                    _e.printStackTrace();
                }

                while (true) {
                    int bufferIndex = 0; // Starting at position 0 in the buffer
                    int floatsToBeRequested = mAudioBuffer.length; // Requesting the max number of floats the buffer can hold

                    // mAudioBuffer has at least minimumBufferSize so record.read can not overflow it
                    // but the recording only happens until the buffer is filled with mWindowSize floats
                    // they can then be sent to processing (only mWindowSize values will be used)
                    while (bufferIndex < mWindowSize) {
                        int actuallyReadFloats = mAudioRecord.read(mAudioBuffer, bufferIndex, floatsToBeRequested, AudioRecord.READ_NON_BLOCKING);

                        if (actuallyReadFloats < 0) {
                            Log.d(TAG, "An error occurred while reading audio data!");
                            switch (actuallyReadFloats) {
                                case AudioRecord.ERROR:
                                    Log.d(TAG, "The error is unknown!");
                                    break;
                                case AudioRecord.ERROR_BAD_VALUE:
                                    Log.d(TAG, "The values don't resolve to valid data and indices!");
                                    break;
                                case AudioRecord.ERROR_INVALID_OPERATION:
                                    Log.d(TAG, "The audio record instance isn't properly initialized!");
                                    break;
                                case AudioRecord.ERROR_DEAD_OBJECT:
                                    Log.d(TAG, "The audio record instance isn't valid anymore and needs to be recreated!");
                                    break;
                            }
                        }

                        // Move the buffer index
                        bufferIndex += actuallyReadFloats;

                        // Update the max requestable number of floats
                        floatsToBeRequested = mAudioBuffer.length - bufferIndex;
                    }

                    mProcessingRunnable.loadAudioBuffer(mAudioBuffer, mWindowSize);

                    if (!mRecording) {
                        break;
                    }
                }

                stopAudioRecord();
            }
        }).start();

        return true;
    }

    /**
     * Stops recording audio data on the recording thread safely after finishing
     * the current audio read cycle
     */
    public void stopRecording() {
        mRecording = false;
    }

    /**
     * Returns mRecording
     *
     * @return mRecording
     */
    public boolean isRecording() {
        return mRecording;
    }

    /**
     * Stops mAudioRecord and releases it
     * This method has to be called from the recording thread!
     * Attention: After calling this method the AudioRecorder can
     * not be started again!
     */
    private void stopAudioRecord() {
        if (mProcessingRunnable != null) {
            mProcessingRunnable.stop();
        }

        if (mAudioRecord != null && mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
            try {
                mAudioRecord.stop();
            } catch (IllegalStateException _e) {
                _e.printStackTrace();
            }

            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    /**
     * Creates a string representation of the recorded audio data
     * that mAudioBuffer currently holds
     * This method will only return data when mAudioRecord is in a non recording state
     *
     * @return The string representation of the recorded audio data
     */
    @NonNull
    @Override
    public String toString() {
        if (mAudioRecord != null && mAudioBuffer != null) {
            if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                return "Unable to print audio data, currently recording!";
            } else {
                return "The recorded audio data: " + Arrays.toString(mAudioBuffer);
            }
        } else {
            return "Unable to print audio data, recorder isn't properly initialized!";
        }
    }

    /**
     * Returns mAudioBuffer
     *
     * @return The audio buffer
     */
    public float[] getAudioBuffer() {
        return mAudioBuffer;
    }
}

/**
 * A runnable for calculating the stft with audio data
 */
class ProcessingRunnable implements Runnable {

    private final ArrayList<WindowMaker> mWindowMakerList;
    private final int mWindowSize;
    private final MemoryHandler mMemoryHandler;
    private boolean mRunning;

    public ProcessingRunnable(int _windowSize, MemoryHandler _memoryHandler) {
        mWindowMakerList = new ArrayList<>();
        mWindowSize = _windowSize;
        mMemoryHandler = _memoryHandler;
        mRunning = true;
    }

    @Override
    public void run() {
        while (mRunning || !getWindowMakerListThreadSafe().isEmpty()) {
            if (!getWindowMakerListThreadSafe().isEmpty() && getWindowMakerListThreadSafe().get(0) != null) {
                while (!getWindowMakerListThreadSafe().get(0).isEmpty()) {

                    // 1) Using a window function
                    float[] window = getWindowMakerListThreadSafe().get(0).applyVonHann(mWindowSize);

                    // 2) Performing stft
                    FloatFFT_1D floatFFT_1D = new FloatFFT_1D(mWindowSize);
                    floatFFT_1D.realForward(window);

                    // 3) Calculating the proper spectrum data
                    float[] spectrum = new float[(window.length / 2) - 2]; // don't take the first two indices because of jtransforms output layout (see documentation)
                    for (int i = 2; i < spectrum.length + 2; i++) {
                        spectrum[i - 2] = (float) Math.sqrt(Math.pow(window[i * 2], 2) + Math.pow(window[i * 2 + 1], 2));
                    }

                    // 4) Normalizing the spectrum amplitudes
                    float maxAmplitude = 0;
                    for (int i = 0; i < spectrum.length; i++) {
                        maxAmplitude = Math.max(maxAmplitude, spectrum[i]);
                    }

                    if (maxAmplitude > 0) {
                        for (int i = 0; i < spectrum.length; i++) {
                            spectrum[i] /= maxAmplitude;
                        }
                    }

                    mMemoryHandler.add(spectrum);
                }

                getWindowMakerListThreadSafe().remove(0);
            } else {
                if (!getWindowMakerListThreadSafe().isEmpty()) {
                    getWindowMakerListThreadSafe().remove(0);
                }
            }
        }
    }

    public void loadAudioBuffer(float[] _audioBuffer, int mWindowSize) {
        getWindowMakerListThreadSafe().add(new WindowMaker(_audioBuffer, mWindowSize));
    }

    private synchronized ArrayList<WindowMaker> getWindowMakerListThreadSafe() {
        return mWindowMakerList;
    }

    public void stop() {
        mRunning = false;
    }
}
