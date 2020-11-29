package com.android.settings.dotextras.custom.sections.wallpaper

import android.graphics.drawable.Drawable

class WallpaperPreview(var image: Drawable) {

    var GALLERY: Int = 0
    var SYSTEM: Int = 1
    var LIVE_SYSTEM: Int = 2

    var path: String? = null
    var title: String? = null
    var type: Int = GALLERY
}