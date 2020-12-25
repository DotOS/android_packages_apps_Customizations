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
package com.android.settings.dotextras.custom.views

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.PathParser

/**
 * Drawable that draws a grid rows x cols of icon shapes adjusting their size to fit within its
 * bounds.
 */
class GridTileDrawable(private val mCols: Int, private val mRows: Int, path: String?) : Drawable() {
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mShapePath: Path = PathParser.createPathFromPathData(path)
    private val mTransformedPath: Path
    private val mScaleMatrix: Matrix
    private var mCellSize = -1f
    private var mMarginTop = 0f
    private var mMarginLeft = 0f
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mCellSize =
            (bounds.height().toFloat() / mRows).coerceAtMost(bounds.width().toFloat() / mCols)
        val spaceBetweenIcons: Float = mCellSize * ((1 - ICON_SCALE) / 2)
        mMarginTop = (bounds.height() - mCellSize * mRows) / 2 + spaceBetweenIcons
        mMarginLeft = (bounds.width() - mCellSize * mCols) / 2 + spaceBetweenIcons
        val scaleFactor = mCellSize * ICON_SCALE / PATH_SIZE
        mScaleMatrix.setScale(scaleFactor, scaleFactor)
        mShapePath.transform(mScaleMatrix, mTransformedPath)
    }

    override fun draw(canvas: Canvas) {
        for (r in 0 until mRows) {
            for (c in 0 until mCols) {
                val saveCount = canvas.save()
                val x = c * mCellSize + mMarginLeft
                val y = r * mCellSize + mMarginTop
                canvas.translate(x, y)
                canvas.drawPath(mTransformedPath, mPaint)
                canvas.restoreToCount(saveCount)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    companion object {
        // Path is expected configuration in following dimension: [100 x 100]))
        private const val PATH_SIZE = 100f

        // We want each "icon" using 80% of the available size, so there's 10% padding on each side
        private const val ICON_SCALE = .8f
    }

    init {
        mTransformedPath = Path(mShapePath)
        mScaleMatrix = Matrix()
    }
}