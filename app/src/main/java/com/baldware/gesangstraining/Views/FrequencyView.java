package com.baldware.gesangstraining.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import com.baldware.gesangstraining.AudioRecording.AudioRecorder;
import com.baldware.gesangstraining.AudioRecording.MemoryHandler;
import com.baldware.gesangstraining.Utils.NoteHandler;

/**
 * A view able to display a spectrogram by using the data stored in a memory handler
 */
public class FrequencyView extends View {

    // Variables & Data Containers
    private MemoryHandler mMemoryHandler;
    private final Paint mPaint;
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private boolean mNoteView;
    private final CountDownTimer mFetchAndRedrawTimer;

    // Flags
    private static final int UPDATE_COUNTDOWN_IN_MIL = 33; // 33ms = theoretically about 30FPS but the recorder will most likely collect data too slowly
    private static final int SIZE_FACTOR = 500;

    /**
     * Constructor
     *
     * @param _context      The context the view will be embedded in
     * @param _attributeSet The attribute set for the view
     */
    public FrequencyView(Context _context, AttributeSet _attributeSet) {
        super(_context, _attributeSet);

        mPaint = new Paint();
        mBitmap = null;
        mWidth = 0;
        mHeight = 0;
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
     * Fetches data from the mMemoryHandler and draws it on mCanvas
     */
    private void fetchAndRedraw() {
        if (mMemoryHandler == null || mMemoryHandler.getXSize() == 0) {
            return;
        }

        float[] newestSpectrum = mMemoryHandler.get(mMemoryHandler.getXSize() - 1);

        mWidth = newestSpectrum.length;
        mHeight = SIZE_FACTOR; // Actually 1 * SIZE_FACTOR as all stft values are normalized (to a max of 1)

        float[] dataArray = new float[mWidth * mHeight];

        // Get values
        for (int y = 0; y < mHeight; y++) {
            for (int x = 0; x < mWidth; x++) {
                if ((mHeight - y) <= newestSpectrum[x] * SIZE_FACTOR) {
                    dataArray[x + (y * mWidth)] = 1;
                }
            }
        }

        if (mNoteView) {
            int binIndex = 0;

            for (int x = 0; x < mWidth; x++) {
                if (newestSpectrum[x] == 1f) {
                    binIndex = x;
                    break;
                }
            }

            float highestAmplitudeFrequency = (((AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ * binIndex) + (AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ * (binIndex + 1))) / 2f);
            float nearestNoteFrequency = NoteHandler.getNearestNoteFrequency(highestAmplitudeFrequency);
            if (nearestNoteFrequency != -1f) {
                int nearestNoteBin = (int) (nearestNoteFrequency / AudioRecorder.FREQUENCY_RESOLUTION_IN_HZ);

                if (dataArray[nearestNoteBin] != 1f) {
                    for (int y = 0; y < mHeight; y++) {
                        dataArray[nearestNoteBin + (y * mWidth)] = -1f;
                    }
                }
            }
        }

        // To RGB conversion (from: [0, 1] to: [0, 255]
        int[] rgbArray = new int[dataArray.length];
        for (int i = 0; i < rgbArray.length; i++) {

            // Apply color coding
            float amplitudeHueScaled = (float) i / (float) rgbArray.length * 360f * dataArray[i];

            int rgba = 0;

            if (amplitudeHueScaled != 0) {
                rgba = Color.HSVToColor(new float[]{amplitudeHueScaled, 1f, 1f}); // for rgb set saturation to 1f
            }

            if (mNoteView) {
                if (dataArray[i] == -1) {
                    rgba = Color.argb(255, 0, 0, 0);
                }
            }

            rgbArray[i] = (Color.alpha(rgba) & 0xff) << 24 | (Color.red(rgba) & 0xff) << 16 | (Color.green(rgba) & 0xff) << 8 | (Color.blue(rgba) & 0xff);
        }

        mBitmap = Bitmap.createBitmap(rgbArray, mWidth, mHeight, Bitmap.Config.ARGB_8888);

        invalidate();       // needed on view appearance change (performs a redraw)
        requestLayout();    // needed on view size or shape change
    }

    /**
     * Regulates view dimensions
     *
     * @param _widthMeasureSpec  The widthMeasureSpec
     * @param _heightMeasureSpec The heightMeasureSpec
     */
    @Override
    protected void onMeasure(int _widthMeasureSpec, int _heightMeasureSpec) {
        int widthSize = getMeasurement(_widthMeasureSpec, getDesiredWidth());
        int heightSize = getMeasurement(_heightMeasureSpec, getDesiredHeight());
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * Gets the desired view width
     * (calculated by adding the widths of all elements contained by the view)
     *
     * @return The desired view width
     */
    private int getDesiredWidth() {
        return mWidth;
    }

    /**
     * Gets the desired view height
     * (calculated by adding the heights of all elements contained by the view)
     *
     * @return The desired view height
     */
    private int getDesiredHeight() {
        return mHeight;
    }

    /**
     * Calculates the proper view size
     * taking the parent view bounding and view content into consideration
     *
     * @param _measureSpec The measureSpec
     * @param _contentSize The size of the view content
     * @return The proper view size
     */
    private int getMeasurement(int _measureSpec, int _contentSize) {
        int specMode = MeasureSpec.getMode(_measureSpec);
        int specSize = MeasureSpec.getSize(_measureSpec);
        int resultSize = 0;

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                // Size can be chosen freely
                resultSize = _contentSize;
                break;
            case MeasureSpec.AT_MOST:
                // Size can be chosen freely, but has to be up to spec
                resultSize = Math.min(_contentSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                // Size hast to be spec size
                resultSize = specSize;
                break;
        }

        return resultSize;
    }

    /**
     * Draws mBitmap onto the canvas therefore creating the view image
     * Called onDraw (on invalidate())
     *
     * @param _canvas The canvas to be drawn on
     */
    @Override
    protected void onDraw(Canvas _canvas) {
        if (mBitmap != null) {
            _canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }


}
