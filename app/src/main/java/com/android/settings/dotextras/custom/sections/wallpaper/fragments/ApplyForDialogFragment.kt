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

import android.app.WallpaperManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.android.settings.dotextras.custom.views.ExpandableLayout
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import java.io.IOException
import java.io.InputStream
import java.net.URL

class ApplyForDialogFragment(val wallpaper: Wallpaper) : DialogFragment() {

    private lateinit var wallpaperManager: WallpaperManager

    private lateinit var forHome: LinearLayout
    private lateinit var forLockscreen: LinearLayout
    private lateinit var forBoth: LinearLayout
    private lateinit var expandable: ExpandableLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.item_wallpaper_for, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forHome = view.findViewById(R.id.wp_apply_home)
        forLockscreen = view.findViewById(R.id.wp_apply_lockscreen)
        forBoth = view.findViewById(R.id.wp_apply_both)
        expandable = view.findViewById(R.id.wp_choice)
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        val isSystem = wallpaper.uri != null
        val drawable: Drawable = if (isSystem) {
            uriToDrawable(Uri.parse(wallpaper.uri))
        } else {
            urlToDrawable(wallpaper.url!!)
        }
        forHome.setOnClickListener { setWallpaper(drawable, WallpaperManager.FLAG_SYSTEM) }
        forLockscreen.setOnClickListener { setWallpaper(drawable, WallpaperManager.FLAG_LOCK) }
        forBoth.setOnClickListener { setWallpaper(drawable) }
    }

    private fun uriToDrawable(uri: Uri): Drawable {
        val drawable = Drawable.createFromStream(
            requireContext().contentResolver.openInputStream(uri),
            uri.toString()
        )
        return scaleCropToFit(drawableToBitmap(drawable)!!).toDrawable(resources)
    }

    private fun urlToDrawable(urlString: String): Drawable {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        return try {
            val bitmap = BitmapDrawable(
                Resources.getSystem(),
                BitmapFactory.decodeStream(URL(urlString).content as InputStream)
            )
            scaleCropToFit(bitmap.bitmap).toDrawable(resources)
        } catch (e: IOException) {
            ResourcesCompat.getDrawable(resources, R.drawable.ic_close, requireContext().theme)!!
        }
    }

    private fun scaleCropToFit(original: Bitmap): Bitmap {
        val display: DisplayMetrics = resources.displayMetrics
        val targetWidth = display.widthPixels
        val targetHeight = display.heightPixels
        val width = original.width
        val height = original.height
        val widthScale = targetWidth.toFloat() / width.toFloat()
        val heightScale = targetHeight.toFloat() / height.toFloat()
        val scaledWidth: Float
        val scaledHeight: Float
        var startY = 0
        var startX = 0
        if (widthScale > heightScale) {
            scaledWidth = targetWidth.toFloat()
            scaledHeight = height * widthScale
            startY = ((scaledHeight - targetHeight) / 2).toInt()
        } else {
            scaledHeight = targetHeight.toFloat()
            scaledWidth = width * heightScale
            startX = ((scaledWidth - targetWidth) / 2).toInt()
        }
        val scaledBitmap = Bitmap.createScaledBitmap(
            original,
            scaledWidth.toInt(), scaledHeight.toInt(), true
        )
        return Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight)
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requireDialog().setCanceledOnTouchOutside(true)
        requireDialog().window!!.setGravity(Gravity.BOTTOM)
        requireDialog().window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun setWallpaper(drawable: Drawable, flag: Int) {
        val display: DisplayMetrics = resources.displayMetrics
        val screenWidth = display.widthPixels
        val screenHeight = display.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, width, height, true)
        task {
            try {
                wallpaperManager.setBitmap(wallpaper, null, true, flag)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } successUi {
            requireActivity().runOnUiThread(this::dismiss)
        }
    }

    private fun setWallpaper(drawable: Drawable) {
        val display: DisplayMetrics = resources.displayMetrics
        val screenWidth = display.widthPixels
        val screenHeight = display.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, width, height, true)
        task {
            try {
                wallpaperManager.setBitmap(wallpaper)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } successUi {
            requireActivity().runOnUiThread(this::dismiss)
        }
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