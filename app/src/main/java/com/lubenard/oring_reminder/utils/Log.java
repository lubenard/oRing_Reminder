package com.lubenard.oring_reminder.utils;

import android.content.Context;

import com.lubenard.oring_reminder.BuildConfig;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class Log {

    static OutputStreamWriter outputStreamWriter;

    public Log(Context context) {
        try {
            outputStreamWriter = new OutputStreamWriter(context.openFileOutput("logs.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(String.format("---------- App Start at %s ----------", Utils.getdateFormatted(new Date())));
        } catch (Exception e) {
            android.util.Log.e("Exception", "File opening failed: " + e);
        }
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

    private static void writeToFile(String tag, String data) {
        try {
            outputStreamWriter.write(String.format("[%s] %s\n", tag, data));
            outputStreamWriter.flush();
        } catch (Exception e) {
            android.util.Log.e("Exception", "File write failed: " + e);
        }
    }

    public void closeFile() {
        try {
            outputStreamWriter.write("---------- App closed ----------");
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
