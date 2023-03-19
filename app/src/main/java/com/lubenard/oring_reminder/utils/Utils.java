package com.lubenard.oring_reminder.utils;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import com.lubenard.oring_reminder.CurrentSessionWidgetProvider;
import com.lubenard.oring_reminder.MainActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class Utils {

    private static final String TAG = "Utils";

    /**
     * Apply theme based on newValue
     * @param newValue the new Theme to apply
     */
    public static void applyTheme(String newValue) {
        switch (newValue) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "white":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "battery_saver":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Apply language based on newValue
     * @param context context
     * @param newValue the new Language to apply
     */
    public static void applyLanguage(Context context, String newValue) {
        switch (newValue) {
            case "en":
                setAppLocale(context,"en-us");
                break;
            case "fr":
                setAppLocale(context, "fr");
                break;
            case "de":
                setAppLocale(context,"de");
                break;
            case "system":
            default:
                setAppLocale(context, Resources.getSystem().getConfiguration().locale.getLanguage());
                break;
        }
    }

    /**
     * Check if the input string is valid
     * @param text the given input string
     * @return 1 if the string is valid, else 0
     */
    //TODO: Refactor this method to make return boolean
    public static boolean isDateSane(String text) {
        if (text.equals("") || text.equals("NOT SET YET") || DateUtils.getdateParsed(text) == null)
            return false;
        return true;
    }

    /**
     * Change language
     * @param context
     * @param localeCode localCode to apply
     */
    public static void setAppLocale(Context context, String localeCode) {
        DateUtils.setAppLocale(localeCode);
        Locale myLocale = new Locale(localeCode);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    /**
     * Send a notification on the 'normal' channel
     * @param context current Context
     * @param title Notification title
     * @param content notification body
     * @param drawable drawable icon
     */
    public static void sendNotification(Context context, String title, String content, int drawable) {
        // First let's create the intent
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the notification manager and build it
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "NORMAL_CHANNEL");
        permNotifBuilder.setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
    }

    /**
     * Instantly update the widget
     * @param context
     */
    public static void updateWidget(Context context) {
        //if (CurrentSessionWidgetProvider.isThereAWidget) {
        Log.d(TAG, "Updating Widget");
        Intent intent = new Intent(context, CurrentSessionWidgetProvider.class);
        context.sendBroadcast(intent);
        //}
    }

    /**
     * @return return the corresponding pending intent flag according to android version
     */
    public static int getIntentMutableFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }

    public static void hideKbd(Context context, IBinder v) {
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v,0);
    }

    /**
     * Generate a image from upper half of pdf's first page.
     * It is used as 'preview' feature in the pdf listview.
     * To avoid recreating this each time we load the vue, which take a lot of time, we only create
     * it once, and save it into a file
     * @param ctx Context
     * @param pdfUri original pdf url
     */
    // Code for this function has been found here
    // https://stackoverflow.com/questions/38828396/generate-thumbnail-of-pdf-in-android
    public static void generatePdfThumbnail(Context ctx, String pdfUri) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(ctx);
        try {
            // http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            Log.d(TAG, "Creating thumbnail for " + pdfUri);
            ParcelFileDescriptor fd = ctx.getContentResolver().openFileDescriptor(Uri.parse("file://" + pdfUri), "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, (height / 100) * 75, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            OutputStream os = new FileOutputStream(pdfUri + ".jpg");
            Log.d(TAG, "FileOutputStream is on " + pdfUri + ".jpg");
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, os);
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch(Exception e) {
            //todo with exception
            Log.d(TAG, "EXCEPTION: " + e);
        }
    }

    /**
     * Copty a file into internal storage
     * @param mcoContext
     * @param sFileName name to new file
     * @param datasUri Uri of file to copy
     */
    // Very useful https://mkyong.com/java/how-to-write-to-file-in-java-fileoutputstream-example/
    public static void writeFileOnInternalStorage(Context mcoContext, String sFileName, Uri datasUri) {
        try {
            File file = new File(mcoContext.getFilesDir(), sFileName);
            FileOutputStream fop = new FileOutputStream(file);
            InputStream inputStream = mcoContext.getContentResolver().openInputStream(datasUri);
            while (inputStream.available() > 0)
                fop.write(inputStream.read());
            fop.close();
            Log.d(TAG, "Wrote file to " + file.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
