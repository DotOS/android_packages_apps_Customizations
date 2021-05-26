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
package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class LockscreenWallpaperFragment() : Fragment() {

    private lateinit var wallpaper: ImageView
    private var wallpaperBase: Wallpaper? = null

    constructor(wallpaperBase: Wallpaper) : this() {
        this.wallpaperBase = wallpaperBase
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.item_wallpaper_preview_card_big, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val overlay: LinearLayout = view.findViewById(R.id.lockscreenOverlay)
        overlay.visibility = View.VISIBLE
        wallpaper = view.findViewById(R.id.wallpaperPreviewImage)
        if (wallpaperBase == null) {
            val wallpaperManager = WallpaperManager.getInstance(requireContext())
            var pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
            if (pfd == null) pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
            if (pfd != null)
                Glide.with(view)
                    .load(BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(wallpaper)
            else
                Glide.with(view)
                    .load(wallpaperManager.drawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(wallpaper)
        } else {
            Glide.with(view)
                .load(wallpaperBase!!.uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(wallpaper)
        }
    }
}