package com.lubenard.oring_reminder.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import com.lubenard.oring_reminder.CurrentSessionWidgetProvider
import com.shockwave.pdfium.PdfiumCore
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class Utils {

    companion object {
        val TAG: String = "Utils"

        /**
         * Apply theme based on newValue
         * @param newValue the new Theme to apply
         */
        fun applyTheme(newValue: String) {
            when (newValue) {
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "white" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "battery_saver" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        /**
         * Apply language based on newValue
         * @param context context
         * @param newValue the new Language to apply
         */
        fun applyLanguage(context: Context, newValue: String) {
            when (newValue) {
                "en" -> setAppLocale(context, "en-us")
                "fr" -> setAppLocale(context, "fr")
                "de" -> setAppLocale(context, "de")
                else -> setAppLocale(context, Resources.getSystem().configuration.locale.language)
            }
        }

        /**
         * Change language
         * @param context
         * @param localeCode localCode to apply
         */
        fun setAppLocale(context: Context, localeCode: String) {
            // TODO: Broken due to change to KT
            DateUtils.Companion.setAppLocale(localeCode)
            val myLocale = Locale(localeCode)
            val res = context.resources
            val displayMetrics = res.displayMetrics
            val conf = res.configuration
            conf.locale = myLocale
            res.updateConfiguration(conf, displayMetrics)
        }

        /**
         * Instantly update the widget
         * @param context
         */
        fun updateWidget(context: Context) {
            //if (CurrentSessionWidgetProvider.isThereAWidget) {
            Log.d(TAG, "Updating Widget")
            val intent = Intent(context, CurrentSessionWidgetProvider::class.java)
            context.sendBroadcast(intent)
            //}
        }

        /**
         * @return return the corresponding pending intent flag according to android version
         */
        fun getIntentMutableFlag(): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                return PendingIntent.FLAG_UPDATE_CURRENT and PendingIntent.FLAG_MUTABLE
            return PendingIntent.FLAG_UPDATE_CURRENT
        }

        /**
         * Hide keyboard
         * @param context
         * @param v windowToken
         */
        fun hideKbd(context: Context, v: IBinder) {
            val inputMethodManager: InputMethodManager =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(v, 0)
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
        fun generatePdfThumbnail(ctx: Context, pdfUri: String) {
            val pageNumber = 0
            val pdfiumCore = PdfiumCore(ctx)
            try {
                // http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
                Log.d(TAG, "Creating thumbnail for $pdfUri")
                val fd = ctx.contentResolver.openFileDescriptor(Uri.parse("file://$pdfUri"), "r")
                val pdfDocument = pdfiumCore.newDocument(fd)
                pdfiumCore.openPage(pdfDocument, pageNumber)
                val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber)
                val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber)
                val bmp = Bitmap.createBitmap(width, (height / 100) * 75, Bitmap.Config.ARGB_8888)
                pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height)
                val os = FileOutputStream("$pdfUri.jpg")
                Log.d(TAG, "FileOutputStream is on $pdfUri.jpg")
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, os)
                pdfiumCore.closeDocument(pdfDocument) // important!
            } catch (e: Exception) {
                Log.e(TAG, "Error while generating Pdf thumbnail: ", e)
            }
        }

        /**
         * Copy a file into internal storage
         * @param mcoContext
         * @param sFileName name to new file
         * @param datasUri Uri of file to copy
         */
        // Very useful https://mkyong.com/java/how-to-write-to-file-in-java-fileoutputstream-example/
        fun writeFileOnInternalStorage(mcoContext: Context, sFileName: String, datasUri: Uri) {
            try {
                val file = File(mcoContext.filesDir, sFileName)
                val outputStream = FileOutputStream(file)
                val inputStream = mcoContext.contentResolver.openInputStream(datasUri)
                while ((inputStream?.available() ?: 0) > 0)
                    outputStream.write(inputStream!!.read())
                inputStream?.close()
                outputStream.close()
                Log.d(TAG, "Wrote file to ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error while importing file $sFileName", e)
            }
        }
    }
}