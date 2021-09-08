package com.dot.gamedashboard.bubble

import android.content.Context

open class Settings {

    companion object {

        val PREF_SHOW_QC = "show_qc"

        val PREF_SHOW_SCREENSHOT = "show_screenshot_pill"
        val PREF_SHOW_SCREENRECORD = "show_screenrecord_pill"
        val PREF_SHOW_DND = "show_dnd_pill"
    }

    fun apply(context: Context, setting: String, value: Boolean) {
        val prefs = context.getSharedPreferences("gdashboard_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(setting, value)
        editor.apply()
    }

    fun isEnabled(context: Context, setting: String): Boolean {
        val prefs = context.getSharedPreferences("gdashboard_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(setting, true)
    }
}