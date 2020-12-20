package com.android.settings.dotextras.custom.sections.wallpaper

import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.Serializable

typealias onWallpaperChanged = ((wallpaper: Drawable, type: Type) -> Unit)?
typealias onWallpaperPressed = (() -> Unit)?

enum class Type {
    HOME, LOCKSCREEN, BOTH
}

class WallpaperBase(var drawable: Drawable?) : Serializable {

    constructor(url: String?) : this(drawable = null) {
        this.url = url
    }

    var GALLERY: Int = 0
    var SYSTEM: Int = 1
    var LIVE_SYSTEM: Int = 2
    var WEB: Int = 3

    var listener: onWallpaperChanged = null
    var onPressed: onWallpaperPressed = null

    var path: String? = null
    var url: String? = null
    var uri: Uri? = null
    var title: String? = null
    var type: Int = GALLERY
    var category: String? = null
}