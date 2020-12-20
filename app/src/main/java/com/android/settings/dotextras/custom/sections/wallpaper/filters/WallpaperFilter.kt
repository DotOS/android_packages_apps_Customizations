package com.android.settings.dotextras.custom.sections.wallpaper.filters

typealias onWallpaperFilterChanged = ((filter: String) -> Unit)?

class WallpaperFilter(val category: String) {
    var selected = false
    var listener: onWallpaperFilterChanged = null
}