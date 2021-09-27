/*
 * Copyright (C) 2021 The dotOS Project
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
package com.android.settings.dotextras.custom.views

import android.Manifest
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import coil.load
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.sections.clock.onHandleCallback
import com.android.settings.dotextras.custom.sections.grid.*
import com.android.settings.dotextras.custom.sections.grid.GridOptionPreviewer
import com.android.settings.dotextras.custom.sections.grid.LauncherWallpaperPreviewer
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper

class WallpaperPreview(
    activity: AppCompatActivity,
    launcherContainer: ViewGroup,
    lockscreenContainer: ImageView,
    wallpaper: Wallpaper
) {
    private var mGridManager: GridOptionsManager = GridOptionsManager(
        LauncherGridOptionsProvider(activity, activity.getString(R.string.grid_control_metadata_name))
    )
    private var mGridOptionPreviewer: LauncherWallpaperPreviewer =
        LauncherWallpaperPreviewer(mGridManager, launcherContainer, wallpaper)
    private var mClockManager: BaseClockManager
    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"

    init {
        mGridManager.fetchOptions({ options ->
            run {
                if (options != null && activity.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    val optionsCompat: ArrayList<GridOptionCompat> = ArrayList()
                    for (option in options) {
                        val gridCompat = GridOptionCompat(option)
                        gridCompat.listener = {
                            run {
                                mGridOptionPreviewer.setGridOption(it)
                            }
                        }
                        if (option.isActive()) {
                            mGridOptionPreviewer.setGridOption(option)
                            mGridOptionPreviewer.mGridOptionSurface?.setZOrderOnTop(true)
                        }
                        optionsCompat.add(gridCompat)
                    }
                }
            }
        }, false)
        mClockManager = object : BaseClockManager(ContentProviderClockProvider(activity)) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                callback?.invoke(true)
            }
            override fun lookUpCurrentClock(): String =
                activity.intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
        }
        mClockManager.fetchOptions({ options ->
            run {
                if (options != null) {
                    val cm = ClockManager(activity.contentResolver, ContentProviderClockProvider(activity))
                    val optionsCompat = ArrayList<ClockfaceCompat>()
                    for (option in options) {
                        val opt = ClockfaceCompat(option)
                        opt.selected = opt.clockface.isActive(cm)
                        optionsCompat.add(opt)
                        if (opt.selected) {
                            opt.clockface.bindPreviewTile(activity, lockscreenContainer)
                        }
                    }
                }
            }
        }, false)
    }

}

class WallpaperPreviewSystem(
    activity: AppCompatActivity,
    launcherContainer: ViewGroup,
    lockscreenContainer: ImageView,
    lockscreenBackground: ImageView
) {
    private var mGridManager: GridOptionsManager = GridOptionsManager(
        LauncherGridOptionsProvider(activity, activity.getString(R.string.grid_control_metadata_name))
    )
    private var mGridOptionPreviewer: GridOptionPreviewer =
        GridOptionPreviewer(mGridManager, launcherContainer)
    private var mClockManager: BaseClockManager
    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"

    init {
        mGridManager.fetchOptions({ options ->
            run {
                if (options != null) {
                    val optionsCompat: ArrayList<GridOptionCompat> = ArrayList()
                    for (option in options) {
                        val gridCompat = GridOptionCompat(option)
                        gridCompat.listener = {
                            run {
                                mGridOptionPreviewer.setGridOption(it)
                            }
                        }
                        if (option.isActive()) {
                            mGridOptionPreviewer.setGridOption(option)
                        }
                        optionsCompat.add(gridCompat)
                    }
                }
            }
        }, false)
        mClockManager = object : BaseClockManager(ContentProviderClockProvider(activity)) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                callback?.invoke(true)
            }
            override fun lookUpCurrentClock(): String =
                activity.intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
        }
        mClockManager.fetchOptions({ options ->
            run {
                if (options != null) {
                    val cm = ClockManager(activity.contentResolver, ContentProviderClockProvider(activity))
                    val optionsCompat = ArrayList<ClockfaceCompat>()
                    for (option in options) {
                        val opt = ClockfaceCompat(option)
                        opt.selected = opt.clockface.isActive(cm)
                        optionsCompat.add(opt)
                        if (opt.selected) {
                            opt.clockface.bindPreviewTile(activity, lockscreenContainer)
                        }
                    }
                }
            }
        }, false)
        val wallpaperManager = WallpaperManager.getInstance(activity)
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
            if (pfd != null)
                lockscreenBackground.load(BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)) { crossfade(500) }
            else
                lockscreenBackground.load(wallpaperManager.drawable) { crossfade(500) }
        }
    }

}