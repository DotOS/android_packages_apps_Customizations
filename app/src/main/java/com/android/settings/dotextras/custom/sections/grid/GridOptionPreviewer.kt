/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.android.settings.dotextras.custom.utils.SurfaceViewUtils.createSurfaceViewRequest
import com.android.settings.dotextras.custom.utils.WorkspaceSurfaceHolderCallback
import com.bumptech.glide.Glide

/** A class to load the [GridOption] preview to the view.  */
internal class GridOptionPreviewer(
    private val mGridManager: GridOptionsManager,
    private val mPreviewContainer: ViewGroup,
) {
    private var mGridOptionSurface: SurfaceView? = null
    private var mGridOption: GridOption? = null
    private var mSurfaceCallback: GridOptionSurfaceHolderCallback? = null

    /** Loads the Grid option into the container view.  */
    fun setGridOption(gridOption: GridOption?) {
        mGridOption = gridOption
        if (mGridOption != null) {
            updateWorkspacePreview()
        }
    }

    /** Releases the view resource.  */
    fun release() {
        if (mGridOptionSurface != null) {
            mSurfaceCallback!!.cleanUp()
            mGridOptionSurface = null
        }
        mPreviewContainer.removeAllViews()
    }

    @SuppressLint("MissingPermission")
    private fun updateWorkspacePreview() {
        // Reattach SurfaceView to trigger #surfaceCreated to update preview for different option.
        mPreviewContainer.removeAllViews()
        val wallpaperPreview = AppCompatImageView(mPreviewContainer.context)
        val wallpaperManager = WallpaperManager.getInstance(mPreviewContainer.context)
        val pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
        if (pfd != null)
            Glide.with(mPreviewContainer)
                .load(BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor))
                .into(wallpaperPreview)
        else
            Glide.with(mPreviewContainer)
                .load(wallpaperManager.drawable)
                .into(wallpaperPreview)
        wallpaperPreview.scaleType = ImageView.ScaleType.CENTER_CROP
        wallpaperPreview.adjustViewBounds = true
        if (mSurfaceCallback != null) {
            mSurfaceCallback!!.resetLastSurface()
        }
        if (mGridOptionSurface == null) {
            mGridOptionSurface = SurfaceView(mPreviewContainer.context)
            mGridOptionSurface!!.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            mGridOptionSurface!!.setZOrderOnTop(true)
            mSurfaceCallback = GridOptionSurfaceHolderCallback(mGridOptionSurface!!,
                mGridOptionSurface!!.context)
            mGridOptionSurface!!.holder.addCallback(mSurfaceCallback)
            mGridOptionSurface!!.holder.setFormat(PixelFormat.TRANSPARENT)
        }
        mPreviewContainer.addView(wallpaperPreview)
        mPreviewContainer.addView(mGridOptionSurface)
    }

    private inner class GridOptionSurfaceHolderCallback(
        workspaceSurface: SurfaceView,
        context: Context,
    ) : WorkspaceSurfaceHolderCallback(workspaceSurface, context) {
        override fun surfaceCreated(holder: SurfaceHolder) {
            if (mGridOption != null) {
                super.surfaceCreated(holder)
            }
        }

        override fun renderPreview(workspaceSurface: SurfaceView?): Bundle? {
            return mGridManager.renderPreview(
                createSurfaceViewRequest(workspaceSurface!!),
                mGridOption!!.name)
        }
    }
}