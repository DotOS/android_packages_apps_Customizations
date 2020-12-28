/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.settings.dotextras.custom.sections.grid

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.WorkerThread
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.PreviewUtils
import com.android.settings.dotextras.system.OverlayController
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList

/**
 * Abstracts the logic to retrieve available grid options from the current Launcher.
 */
class LauncherGridOptionsProvider(context: Context, authorityMetadataKey: String?) {
    private val mContext: Context = context
    private val mPreviewUtils: PreviewUtils = PreviewUtils(context, authorityMetadataKey)
    private var mOptions: ArrayList<GridOption>? = null
    fun areGridsAvailable(): Boolean {
        return mPreviewUtils.supportsPreview()
    }

    /**
     * Retrieve the available grids.
     * @param reload whether to reload grid options if they're cached.
     */
    @WorkerThread
    fun fetch(reload: Boolean): ArrayList<GridOption>? {
        if (!areGridsAvailable()) {
            return null
        }
        if (mOptions != null && !reload) {
            return mOptions
        }
        val resolver = mContext.contentResolver
        val iconPath = mContext.resources.getString(Resources.getSystem().getIdentifier(
            OverlayController.Constants.CONFIG_ICON_MASK,
            "string",
            OverlayController.Constants.ANDROID_PACKAGE))
        try {
            resolver.query(mPreviewUtils.getUri(LIST_OPTIONS), null, null, null,
                null).use { c ->
                mOptions = ArrayList()
                while (c.moveToNext()) {
                    val name = c.getString(c.getColumnIndex(COL_NAME))
                    val rows = c.getInt(c.getColumnIndex(COL_ROWS))
                    val cols = c.getInt(c.getColumnIndex(COL_COLS))
                    val previewCount = c.getInt(c.getColumnIndex(COL_PREVIEW_COUNT))
                    val isSet =
                        java.lang.Boolean.valueOf(c.getString(c.getColumnIndex(COL_IS_DEFAULT)))
                    val title =
                        if (GRID_NAME_NORMAL == name) mContext.getString(R.string.default_theme_title) else mContext.getString(
                            R.string.grid_title_pattern, cols, rows)
                    mOptions!!.add(GridOption(title, name, isSet, rows, cols,
                        mPreviewUtils.getUri(PREVIEW), previewCount, iconPath))
                }
                Glide.get(mContext).clearDiskCache()
            }
        } catch (e: Exception) {
            mOptions = null
        }
        return mOptions
    }

    /**
     * Request rendering of home screen preview via Launcher to Wallpaper using SurfaceView
     * @param name      the grid option name
     * @param bundle    surface view request bundle generated from
     * [com.android.wallpaper.util.SurfaceViewUtils.createSurfaceViewRequest].
     */
    fun renderPreview(name: String?, bundle: Bundle): Bundle {
        bundle.putString("name", name)
        return mPreviewUtils.renderPreview(bundle)
    }

    fun applyGrid(name: String?): Int {
        val values = ContentValues()
        values.put("name", name)
        return mContext.contentResolver.update(mPreviewUtils.getUri(DEFAULT_GRID), values,
            null, null)
    }

    companion object {
        private const val LIST_OPTIONS = "list_options"
        private const val PREVIEW = "preview"
        private const val DEFAULT_GRID = "default_grid"
        private const val COL_NAME = "name"
        private const val COL_ROWS = "rows"
        private const val COL_COLS = "cols"
        private const val COL_PREVIEW_COUNT = "preview_count"
        private const val COL_IS_DEFAULT = "is_default"

        // Normal gird size name
        private const val GRID_NAME_NORMAL = "normal"

        private const val METADATA_KEY_PREVIEW_VERSION = "preview_version"
    }

}