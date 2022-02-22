package com.baldware.gesangstraining.Utils;

import android.content.Context;
import android.widget.Toast;

import com.baldware.gesangstraining.AudioRecording.MemoryHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

/**
 * A helper for saving and loading memoryHandlers
 * The class does this by using (de-)serialization
 */
public class FileHandler {

    /**
     * Returns the names of all files in internal app specific storage
     *
     * @param _context The current context
     * @return The names of all files in internal app specific storage
     */
    public static String[] getFileNames(Context _context) {
        return _context.fileList();
    }

    /**
     * Serializes a memoryHandler and saves the data to a file
     * The file gets the current date assigned as name
     * The file is saved in internal app specific storage
     *
     * @param _context       The current context
     * @param _memoryHandler The memoryHandler to be saved
     */
    public static void save(Context _context, MemoryHandler _memoryHandler) {
        String fileName = Calendar.getInstance().getTime().toString();

        try {
            FileOutputStream fileOutputStream = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(_memoryHandler);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a saved memoryHandler from internal app specific storage
     * Attention: May return null!
     *
     * @param _context  The current context
     * @param _fileName The fileName specifying the file to be loaded
     * @return A deserialized memoryHandler instance OR null
     */
    public static MemoryHandler load(Context _context, String _fileName) {
        MemoryHandler memoryHandler = null;

        try {
            FileInputStream fileInputStream = _context.openFileInput(_fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            memoryHandler = (MemoryHandler) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return memoryHandler;
    }

    /**
     * Deletes all saved files in internal app specific storage
     * Attention: Must be called by UI thread!
     *
     * @param _context The current context
     */
    public static void deleteSaveFiles(Context _context) {
        String[] fileNames = getFileNames(_context);

        for (String fileName : fileNames) {
            File file = new File(_context.getFilesDir(), fileName);
            if (!file.delete()) {
                Toast.makeText(_context, "Unable to delete file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
