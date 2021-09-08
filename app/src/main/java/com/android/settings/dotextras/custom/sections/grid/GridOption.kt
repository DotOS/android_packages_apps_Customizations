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

import android.graphics.PorterDuff
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.android.settings.dotextras.R
import com.dot.ui.utils.ResourceHelper
import com.dot.ui.utils.getNormalizedSecondaryColor
import com.android.settings.dotextras.custom.views.GridTileDrawable

/**
 * Represents a grid layout option available in the current launcher.
 */
open class GridOption : Parcelable {
    val title: String
    private val mIsCurrent: Boolean
    private val mIconShapePath: String
    private val mTileDrawable: GridTileDrawable
    val name: String
    val rows: Int
    val cols: Int
    private val previewImageUri: Uri
    private val previewPagesCount: Int

    constructor(
        title: String, name: String, isCurrent: Boolean, rows: Int, cols: Int,
        previewImageUri: Uri, previewPagesCount: Int, iconShapePath: String,
    ) {
        this.title = title
        mIsCurrent = isCurrent
        mIconShapePath = iconShapePath
        mTileDrawable = GridTileDrawable(rows, cols, mIconShapePath)
        this.name = name
        this.rows = rows
        this.cols = cols
        this.previewImageUri = previewImageUri
        this.previewPagesCount = previewPagesCount
    }

    protected constructor(`in`: Parcel) {
        title = `in`.readString()
        mIsCurrent = `in`.readByte().toInt() != 0
        mIconShapePath = `in`.readString()
        name = `in`.readString()
        rows = `in`.readInt()
        cols = `in`.readInt()
        previewImageUri = `in`.readParcelable(Uri::class.java.classLoader)
        previewPagesCount = `in`.readInt()
        mTileDrawable = GridTileDrawable(rows, cols, mIconShapePath)
    }

    fun bindThumbnailTile(view: View) {
        val context = view.context
        if (mIsCurrent)
            mTileDrawable.setColorFilter(
                context.getNormalizedSecondaryColor(
                    ResourceHelper.getAccent(
                        context
                    )
                ), PorterDuff.Mode.ADD
            )
        else
            mTileDrawable.setColorFilter(
                ResourceHelper.getSecondaryTextColor(context),
                PorterDuff.Mode.ADD
            )
        (view.findViewById<View>(R.id.gridThumbnail) as ImageView)
            .setImageDrawable(mTileDrawable)
    }

    fun isActive(): Boolean {
        return mIsCurrent
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is GridOption) {
            return (TextUtils.equals(name, other.name)
                    && cols == other.cols && rows == other.rows)
        }
        return false
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(title)
        parcel.writeByte((if (mIsCurrent) 1 else 0).toByte())
        parcel.writeString(mIconShapePath)
        parcel.writeString(name)
        parcel.writeInt(rows)
        parcel.writeInt(cols)
        parcel.writeParcelable(previewImageUri, i)
        parcel.writeInt(previewPagesCount)
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + mIsCurrent.hashCode()
        result = 31 * result + mIconShapePath.hashCode()
        result = 31 * result + mTileDrawable.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + rows
        result = 31 * result + cols
        result = 31 * result + previewImageUri.hashCode()
        result = 31 * result + previewPagesCount
        return result
    }

    companion object {
        val CREATOR: Parcelable.Creator<GridOption> = object : Parcelable.Creator<GridOption> {
            override fun createFromParcel(`in`: Parcel): GridOption {
                return GridOption(`in`)
            }

            override fun newArray(size: Int): Array<GridOption?> {
                return arrayOfNulls(size)
            }
        }
    }
}