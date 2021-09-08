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
package com.android.settings.dotextras.custom.sections.clock

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.text.TextUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.android.settings.dotextras.custom.sections.clock.utils.Asset
import com.bumptech.glide.Glide

class Clockface private constructor(
    private val mTitle: String?,
    val id: String?,
    private val previewAsset: Asset?,
    private val mThumbnail: Asset?,
) {

    fun bindThumbnailTile(view: ImageView) {
        mThumbnail!!.loadDrawableWithTransition(
            view.context, view, 50, null,
            view.resources.getColor(android.R.color.transparent, null)
        )
    }

    fun bindPreviewTile(activity: AppCompatActivity, view: ImageView) {
        previewAsset!!.loadDrawableWithTransition(
            view.context, view, 500, object : Asset.DrawableLoadedListener {
                override fun onDrawableLoaded(bitmap: Bitmap) {
                    if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                        val handler = Handler(activity.mainLooper)
                        handler.post {
                            if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                                Glide.with(activity).clear(view)
                                view.setImageDrawable(BitmapDrawable(view.resources, replaceColor(bitmap)))
                            }
                        }
                    }
                }
            },
            view.resources.getColor(android.R.color.transparent, null)
        )
    }

    fun bindPreviewTile(activity: FragmentActivity, view: ImageView) {
        previewAsset!!.loadDrawableWithTransition(
            view.context, view, 500, object : Asset.DrawableLoadedListener {
                override fun onDrawableLoaded(bitmap: Bitmap) {
                    if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                        val handler = Handler(activity.mainLooper)
                        handler.post {
                            if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                                Glide.with(activity).clear(view)
                                view.setImageDrawable(BitmapDrawable(view.resources, replaceColor(bitmap)))
                            }
                        }
                    }
                }
            },
            view.resources.getColor(android.R.color.transparent, null)
        )
    }

    private fun replaceColor(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, 1 * width, 0, 0, width, height)
        for (x in pixels.indices) {
            if (pixels[x] == Color.BLACK) pixels[x] = 0
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    fun getTitle(): String? = mTitle

    fun isActive(manager: ClockManager): Boolean {
        val currentClock: String = manager.lookUpCurrentClock()
        // Empty clock Id is the default system clock
        return (TextUtils.isEmpty(currentClock) && TextUtils.isEmpty(id)
                || id != null && id == currentClock)
    }

    class Builder {
        private var mTitle: String? = null
        private var mId: String? = null
        private var mPreview: Asset? = null
        private var mThumbnail: Asset? = null
        fun build(): Clockface {
            return Clockface(mTitle, mId, mPreview, mThumbnail)
        }

        fun setTitle(title: String?): Builder {
            mTitle = title
            return this
        }

        fun setId(id: String?): Builder {
            mId = id
            return this
        }

        fun setPreview(preview: Asset?): Builder {
            mPreview = preview
            return this
        }

        fun setThumbnail(thumbnail: Asset?): Builder {
            mThumbnail = thumbnail
            return this
        }
    }
}