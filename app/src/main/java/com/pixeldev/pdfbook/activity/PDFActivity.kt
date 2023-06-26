package com.pixeldev.pdfbook.activity

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.helper.SharedPref
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PDFActivity : AppCompatActivity() {
    var book_url: String? = null
    var pdfView: PDFView? = null
    var progressDialog: ProgressDialog? = null
    var sharedPref: SharedPref? = null

    /* access modifiers changed from: protected */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfactivity)
        book_url = intent.getStringExtra("book_url")
        pdfView = findViewById<View>(R.id.pdfView) as PDFView
        sharedPref = SharedPref(this)
        val progressDialog2 = ProgressDialog(this)
        progressDialog = progressDialog2
        progressDialog2.setMessage(resources.getString(R.string.loading))
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        DownloadFilesTask().execute(*arrayOf(book_url))
    }

    private inner class DownloadFilesTask : AsyncTask<String?, Void?, InputStream?>() {
        /* access modifiers changed from: protected */
        override fun doInBackground(vararg string: String?): InputStream? {
            return try {
                val httpURLConnection = URL(string[0]).openConnection() as HttpURLConnection
                if (httpURLConnection.responseCode == 200) {
                    BufferedInputStream(httpURLConnection.inputStream)
                } else null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        /* access modifiers changed from: protected */
        public override fun onPostExecute(inputStream: InputStream?) {
            Handler().postDelayed({ progressDialog!!.dismiss() }, 4000)
            if (sharedPref!!.loadNightModeState()) {
                pdfView!!.fromStream(inputStream).enableSwipe(true).swipeHorizontal(true)
                    .enableDoubletap(true).defaultPage(0).enableAntialiasing(true).spacing(0)
                    .pageFitPolicy(FitPolicy.WIDTH).fitEachPage(true).pageSnap(true).pageFling(true)
                    .nightMode(true).load()
            } else {
                pdfView!!.fromStream(inputStream).enableSwipe(true).swipeHorizontal(true)
                    .enableDoubletap(true).defaultPage(0).enableAntialiasing(true).spacing(0)
                    .pageFitPolicy(FitPolicy.WIDTH).fitEachPage(true).pageSnap(true).pageFling(true)
                    .nightMode(false).load()
            }
        }
    }
}