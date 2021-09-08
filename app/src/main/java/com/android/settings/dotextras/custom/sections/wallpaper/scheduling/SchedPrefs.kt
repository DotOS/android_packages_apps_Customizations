package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import android.content.Context
import com.dot.ui.utils.deserialize

open class SchedPrefs {

    companion object {
        fun applyPack(context: Context, serializedPack: String) {
            val prefs = context.getSharedPreferences("scheduler", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("wallpaperPackObject", serializedPack)
            editor.apply()
        }

        fun exists(context: Context): Boolean {
            val prefs = context.getSharedPreferences("scheduler", Context.MODE_PRIVATE)
            val pack = prefs.getString("wallpaperPackObject", null)?.deserialize() as WallpaperPack?
            return pack != null
        }

        fun getPack(context: Context): WallpaperPack? {
            val prefs = context.getSharedPreferences("scheduler", Context.MODE_PRIVATE)
            return prefs.getString("wallpaperPackObject", null)?.deserialize() as WallpaperPack?
        }

        fun deletePack(context: Context) {
            val prefs = context.getSharedPreferences("scheduler", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("wallpaperPackObject", null)
            editor.apply()
        }
    }

}