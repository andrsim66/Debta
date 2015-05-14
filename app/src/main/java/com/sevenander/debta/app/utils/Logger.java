package com.sevenander.debta.app.utils;

/**
 * Created by andrii on 02.03.15.
 */

import android.util.Log;

import java.util.ArrayList;

/**
 * A Wrapper around the Android log for ease of use
 */
public class Logger {

    private static final String TAG = "debta";

    public static void d(String msg) {
        ArrayList<String> messageList = splitString(msg);
        for (String message : messageList) {
            Log.d(TAG, message);
        }
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        Log.e(TAG, msg, tr);
    }

    /**
     * Divides a string into chunks of a given character size.
     *
     * @param text      String text to be sliced
     * @param sliceSize int Number of characters
     * @return ArrayList<String>   Chunks of strings
     */
    public static ArrayList<String> splitString(String text, int sliceSize) {
        ArrayList<String> textList = new ArrayList<String>();
        if (text != null) {
            String aux;
            int left = -1, right = 0;
            int charsLeft = text.length();
            while (charsLeft != 0) {
                left = right;
                if (charsLeft >= sliceSize) {
                    right += sliceSize;
                    charsLeft -= sliceSize;
                } else {
                    right = text.length();
                    aux = text.substring(left, right);
                    charsLeft = 0;
                }
                aux = text.substring(left, right);
                textList.add(aux);
            }
        } else {
            textList.add("You want to Log.d (*null*)!");
        }
        return textList;
    }

    /**
     * Divides a string into chunks.
     *
     * @param text String text to be sliced
     * @return ArrayList<String>
     */
    public static ArrayList<String> splitString(String text) {
        return splitString(text, 1000);
    }
}
