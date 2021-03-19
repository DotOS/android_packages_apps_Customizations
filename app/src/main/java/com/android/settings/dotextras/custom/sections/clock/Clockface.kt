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

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.clock.utils.Asset

class Clockface private constructor(
    private val mTitle: String?,
    val id: String?,
    val previewAsset: Asset?,
    private val mThumbnail: Asset?,
) {

    fun bindThumbnailTile(view: View) {
        val thumbView = view.findViewById<ImageView>(R.id.defaultClockPreview)
        mThumbnail!!.loadDrawableWithTransition(
            thumbView.context, thumbView, 50, null,
            thumbView.resources.getColor(android.R.color.transparent, null)
        )
    }

    fun bindPreviewTile(view: View) {
        val thumbView = view.findViewById<ImageView>(R.id.defaultClockPreview)
        previewAsset!!.loadDrawableWithTransition(
            thumbView.context, thumbView, 50, null,
            thumbView.resources.getColor(android.R.color.transparent, null)
        )
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