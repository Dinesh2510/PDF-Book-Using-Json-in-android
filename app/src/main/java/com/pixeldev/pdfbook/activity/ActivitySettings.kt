package com.pixeldev.pdfbook.activity

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.helper.SharedPref
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.DecimalFormat

class ActivitySettings : AppCompatActivity() {
    var btn_ask: LinearLayout? = null
    var btn_clear_cache: ImageView? = null
    var btn_privacy_policy: LinearLayout? = null
    var materialToolbar: MaterialToolbar? = null
    var sharedPref: SharedPref? = null
    var theme_text_mode: MaterialTextView? = null
    var txt_cache_size: MaterialTextView? = null

    /* access modifiers changed from: protected */
    public override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref2 = SharedPref(this)
        sharedPref = sharedPref2
        if (sharedPref2.loadNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        txt_cache_size = findViewById<View>(R.id.txt_cache_size) as MaterialTextView
        theme_text_mode = findViewById<View>(R.id.theme_text_mode) as MaterialTextView
        initializeCache()

        val imageView = findViewById<View>(R.id.btn_clear_cache) as ImageView
        btn_clear_cache = imageView
        imageView.setOnClickListener { clearCache() }
        val linearLayout = findViewById<View>(R.id.btn_ask) as LinearLayout
        btn_ask = linearLayout
        linearLayout.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.email)))
            i.putExtra( Intent.EXTRA_SUBJECT, "Subject")
            i.putExtra(Intent.EXTRA_TEXT, "Write your message...")
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (e: ActivityNotFoundException) {
            }
        }
        val linearLayout2 = findViewById<View>(R.id.btn_privacy_policy) as LinearLayout
        btn_privacy_policy = linearLayout2
        linearLayout2.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(resources.getString(R.string.privacy_policy_url))
            startActivity(i)
        }
        val choices = arrayOf<CharSequence>(
            resources.getString(R.string.dark_theme),
            resources.getString(R.string.light_theme)
        )
        val sharedPrefere = getSharedPreferences(SHARED_PREFS, 0)
        val counter = sharedPrefere.getString("text", "1")!!.toInt()
        val title = resources.getString(R.string.theme_Setting)
        val cancel = resources.getString(R.string.cancel)
        theme_text_mode!!.text =
            sharedPrefere.getString(TEXT_MODE, resources.getString(R.string.light_theme))
        findViewById<View>(R.id.linear_themes).setOnClickListener(View.OnClickListener {
            MaterialAlertDialogBuilder(this@ActivitySettings).setTitle(title as CharSequence)
                .setNegativeButton(
                    cancel as CharSequence,
                    null as DialogInterface.OnClickListener?
                ).setSingleChoiceItems(
                choices,
                counter,
                DialogInterface.OnClickListener { dialog, which ->
                    val checkedItemPosition =
                        (dialog as AlertDialog).listView.checkedItemPosition
                    if (checkedItemPosition != -1) {
                        Toast.makeText(
                            this@ActivitySettings,
                            choices[checkedItemPosition],
                            Toast.LENGTH_LONG
                        ).show()
                        val editor =
                            getSharedPreferences(SHARED_PREFS, 0).edit()
                        editor.putString("text", checkedItemPosition.toString())
                        editor.putString(
                            TEXT_MODE,
                            choices[checkedItemPosition] as String
                        )
                        editor.apply()
                        when (checkedItemPosition) {
                            0 -> {
                                sharedPref!!.setNightModeState(true)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                theme_text_mode!!.text = resources.getString(R.string.dark_theme)
                                return@OnClickListener
                            }

                            1 -> {
                                sharedPref!!.setNightModeState(false)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                theme_text_mode!!.text = resources.getString(R.string.light_theme)
                                return@OnClickListener
                            }

                            else -> {
                                sharedPref!!.setDefault(true)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                        }
                    }
                }).show()
        })
    }

    fun clearCache() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setMessage(R.string.msg_clear_cache)
        dialog.setPositiveButton(R.string.yes) { dialogInterface, i ->
            clearCacheNow(
                dialogInterface,
                i
            )
        }
        dialog.setNegativeButton(
            R.string.dialog_option_cancel,
            null as DialogInterface.OnClickListener?
        )
        dialog.show()
    }

    private fun clearCacheNow(dialogInterface: DialogInterface, i: Int) {
        try {
            FileUtils.deleteDirectory(cacheDir)
            FileUtils.deleteDirectory(externalCacheDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle(R.string.msg_clearing_cache)
        progressDialog.setMessage(getString(R.string.msg_please_wait))
        progressDialog.setCancelable(false)
        progressDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ setCacheData(progressDialog) }, 3000)
    }

    private fun setCacheData(progressDialog: ProgressDialog) {
        val materialTextView = txt_cache_size
        materialTextView!!.text =
            resources.getString(R.string.sub_setting_clear_cache_start) + " 0 Bytes " + resources.getString(
                R.string.sub_setting_clear_cache_end
            )
        Toast.makeText(this, getString(R.string.msg_cache_cleared), Toast.LENGTH_SHORT).show()
        progressDialog.dismiss()
    }

    private fun initializeCache() {
        val materialTextView = txt_cache_size
        materialTextView!!.text =
            getString(R.string.sub_setting_clear_cache_start) + " " + readableFileSize(
                getDirSize(cacheDir) + 0 + getDirSize(externalCacheDir)
            ) + " " + getString(R.string.sub_setting_clear_cache_end)
    }

    fun getDirSize(dir: File?): Long {
        var size: Long = 0
        for (file in dir!!.listFiles()) {
            if (file != null && file.isDirectory) {
                size += getDirSize(file)
            } else if (file != null && file.isFile) {
                size += file.length()
            }
        }
        return size
    }

    companion object {
        const val SHARED_PREFS = "save_values"
        const val TEXT = "text"
        const val TEXT_MODE = "text_mode"
        fun readableFileSize(size: Long): String {
            if (size <= 0) {
                return "0 Bytes"
            }
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            val stringBuilder = StringBuilder()
            val decimalFormat = DecimalFormat("#,##0.#")
            val d = size.toDouble()
            val pow = Math.pow(1024.0, digitGroups.toDouble())
            java.lang.Double.isNaN(d)
            stringBuilder.append(decimalFormat.format(d / pow))
            stringBuilder.append(" ")
            stringBuilder.append(arrayOf("Bytes", "KB", "MB", "GB", "TB")[digitGroups])
            return stringBuilder.toString()
        }
    }
}