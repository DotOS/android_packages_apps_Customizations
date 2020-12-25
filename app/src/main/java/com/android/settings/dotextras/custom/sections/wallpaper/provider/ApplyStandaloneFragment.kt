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
package com.android.settings.dotextras.custom.sections.wallpaper.provider

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Type
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.CropImageView
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage.getActivityResult
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.ApplyForDialogFragment
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.CurrentWallpaperAdapter
import com.android.settings.dotextras.custom.utils.getFileName
import com.android.settings.dotextras.custom.utils.removeExtension
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException


class ApplyStandaloneFragment(val wallpaper: WallpaperBase) : Fragment() {

    private lateinit var wallpaperManager: WallpaperManager

    private lateinit var currentPager: ViewPager2
    private lateinit var applyButton: MaterialButton
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_wallpaper_apply_standalone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyButton = view.findViewById(R.id.wp_apply_button)
        title = view.findViewById(R.id.wp_title)
        val chipRotate: Chip = view.findViewById(R.id.chipCrop)
        val uriB = wallpaper.uri
        chipRotate.setOnClickListener {
            CropImage.activity(uriB)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowCounterRotation(true)
                .setAllowRotation(true)
                .setAspectRatio(9, 18)
                .setOutputUri(Uri.EMPTY)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .start(requireContext(), this)
        }
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        currentPager = view.findViewById(R.id.standalonePager)
        val pagerLeft: ImageButton = view.findViewById(R.id.wallLeft)
        val pagerRight: ImageButton = view.findViewById(R.id.wallRight)
        if (wallpaper.uri != null) {
            title.text = requireContext().getFileName(wallpaper.uri!!).removeExtension()
        }
        pagerLeft.setOnClickListener {
            currentPager.setCurrentItem(currentPager.currentItem - 1, true)
        }
        pagerRight.setOnClickListener {
            currentPager.setCurrentItem(currentPager.currentItem + 1, true)
        }
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val scaledWallpaper =
            Bitmap.createScaledBitmap(drawableToBitmap(wallpaper.drawable!!)!!, width, height, true)
        wallpaper.drawable = BitmapDrawable(resources, scaledWallpaper)
        currentPager.adapter = CurrentWallpaperAdapter(requireActivity(), wallpaper)
        applyButton.setOnClickListener {
            ApplyForDialogFragment(wallpaper).show(parentFragmentManager, tag)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = getActivityResult(data)
            val resultUri: Uri? = result!!.uri
            val drawable = Drawable.createFromStream(
                requireContext().contentResolver.openInputStream(resultUri),
                resultUri.toString()
            )
            wallpaper.drawable = drawable
            wallpaper.uri = resultUri
            currentPager.adapter = CurrentWallpaperAdapter(requireActivity(), wallpaper)
            currentPager.adapter!!.notifyDataSetChanged()
        }
    }

    private fun setWallpaper(drawable: Drawable, flag: Int) {
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, width, height, true)
        doAsync {
            try {
                wallpaperManager.setBitmap(wallpaper, null, true, flag)
                uiThread { afterApply(flag) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setWallpaper(drawable: Drawable) {
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, width, height, true)
        doAsync {
            try {
                wallpaperManager.bitmap = wallpaper
                uiThread { afterApply(null) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun afterApply(flag: Int?) {
        wallpaper.listener?.invoke(
            wallpaper.drawable!!, if (flag != null) {
                if (flag == WallpaperManager.FLAG_LOCK) Type.LOCKSCREEN else Type.HOME
            } else Type.BOTH
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}