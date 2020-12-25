/*
 * Copyright (C) 2020 The dotOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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