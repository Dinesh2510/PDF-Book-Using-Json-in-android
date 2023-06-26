package com.pixeldev.pdfbook.helper

import android.content.Context
import android.content.SharedPreferences

class SharedPref(context: Context) {
    var sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("filename", 0)
    }

    fun setNightModeState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("NightMode", state!!,)
        editor.commit()
    }

    fun setDefault(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Dafault", state!!,)
        editor.commit()
    }

    fun loadNightModeState(): Boolean {
        return java.lang.Boolean.valueOf(sharedPreferences.getBoolean("NightMode", false))
    }
}