package com.android.settings.dotextras.custom.monet

import android.content.Context
import android.provider.Settings

class PreferenceUtils(val context: Context) {

    var wallpaperColor: Int
    get() {
        return Settings.Secure.getInt(context.contentResolver, "monet_wallpaper_color_picker", -1)
    }
    set(value) {
        Settings.Secure.putInt(context.contentResolver, "monet_wallpaper_color_picker", value)
    }
}