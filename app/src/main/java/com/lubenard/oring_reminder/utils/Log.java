package com.lubenard.oring_reminder.utils;

import com.lubenard.oring_reminder.BuildConfig;

public class Log {

    public static void v(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.v(tag, message);
    }

    public static void v(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG)
            android.util.Log.v(tag, message, throwable);
    }

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.d(tag, message);
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG)
            android.util.Log.d(tag, message, throwable);
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.i(tag, message);
    }

    public static void i(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG)
            android.util.Log.i(tag, message, throwable);
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.w(tag, message);
    }

    public static void w(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG)
            android.util.Log.w(tag, message, throwable);
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.e(tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG)
            android.util.Log.e(tag, message, throwable);
    }


    public static void wtf(String tag, String message) {
        if (BuildConfig.DEBUG)
            android.util.Log.wtf(tag, message);
    }

    public static void wtf(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG)
            android.util.Log.wtf(tag, message, throwable);
    }
}
