package com.lubenard.oring_reminder.utils;

import android.content.Context;

import com.lubenard.oring_reminder.BuildConfig;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class Log {

    static OutputStreamWriter outputStreamWriter;
    static Boolean isLogEnabled = false;

    public static void setIsLogEnabled(Boolean isLogEnabled) {
        android.util.Log.d("CustomLogger", "File logging is " + isLogEnabled);
        Log.isLogEnabled = isLogEnabled;
    }

    public Log(Context context, Boolean isLogEnabled) {
        Log.isLogEnabled = isLogEnabled;
        openFile(context);
    }

    public static void v(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.v(tag, message);
        writeToFile(tag, message);
    }

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.d(tag, message);
        writeToFile(tag, message);
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.i(tag, message);
        writeToFile(tag, message);
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.w(tag, message);
        writeToFile(tag, message);
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.e(tag, message);
        writeToFile(tag, message);
    }

    public static void wtf(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.wtf(tag, message);
        writeToFile(tag, message);
    }

    private static void openFile(Context context) {
        try {
            outputStreamWriter = new OutputStreamWriter(context.openFileOutput("logs.txt", Context.MODE_PRIVATE));
            if (isLogEnabled)
                outputStreamWriter.write(String.format("---------- App Start at %s ----------", DateUtils.getdateFormatted(new Date())));
        } catch (Exception e) {
            android.util.Log.e("Exception", "File opening failed: " + e);
        }
    }

    private static void writeToFile(String tag, String data) {
        if (isLogEnabled) {
            try {
                outputStreamWriter.write(String.format("[%s] %s\n", tag, data));
                outputStreamWriter.flush();
            } catch (Exception e) {
                android.util.Log.e("Exception", "File write failed: " + e);
            }
        }
    }

    public void closeFile() {
        try {
            if (isLogEnabled)
                outputStreamWriter.write("---------- App closed ----------");
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
