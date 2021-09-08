package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import java.io.Serializable

class WallpaperPack(var wallpapers: ArrayList<String>): Serializable {

    var scheduledTime: ScheduledTime? = null
    var scheduledMode: Mode? = null
    var identifier: Int? = null

    enum class Mode {
        ONCE, DAILY, WEEKLY
    }

    class ScheduledTime(var hour: Int, var minute: Int): Serializable
}