package com.baldware.gesangstraining.AudioRecording;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A thread safe memory handler
 * The class uses a counter variable as a locking mechanism
 * To avoid simultaneous list operations from multiple threads
 */
public class MemoryHandler implements Serializable {

    // Variables & Data Containers
    private final ArrayList<float[]> mArrayList = new ArrayList<>();
    private int mElementCounter = 0;

    /**
     * Adds an array of data to mArrayList
     *
     * @param dataArray The data array to be added
     */
    public synchronized void add(float[] dataArray) {
        mArrayList.add(dataArray);
        mElementCounter++;
    }

    /**
     * Gets an array of data from mArrayList
     *
     * @param index The index of the array to be returned
     * @return The array of data at the requested index
     */
    public synchronized float[] get(int index) {
        if (index < mElementCounter) {
            return mArrayList.get(index);
        } else {
            return null;
        }
    }

    /**
     * Gets a concatenated array holding all data available in mArrayList
     *
     * @return The array concatenated from all arrays in mArrayList
     */
    public synchronized float[] get() {
        if (mArrayList.isEmpty()) {
            return new float[0];
        }

        int width = mElementCounter;
        int height = mArrayList.get(0).length;

        float[] newArray = new float[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newArray[x + (y * width)] = mArrayList.get(x)[y];
            }
        }

        return newArray;
    }

    /**
     * Returns mElementCounter
     *
     * @return mElementCounter
     */
    public int getXSize() {
        return mElementCounter;
    }

    /**
     * Returns the length of the arrays in mArrayList
     *
     * @return The length of the arrays in mArrayList
     */
    public int getYSize() {
        if (mArrayList.isEmpty()) {
            return 0;
        } else {
            return mArrayList.get(0).length;
        }
    }
}
