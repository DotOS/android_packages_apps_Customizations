package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import android.annotation.UiThread
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Type
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase
import com.android.settings.dotextras.custom.views.ExpandableLayout
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException


class ApplyDialogFragment(val wallpaper: WallpaperBase, val position: Int) : DialogFragment() {

    private lateinit var wallOverlay: ImageView
    private lateinit var homeOverlay: RelativeLayout
    private lateinit var lockOverlay: LinearLayout
    private lateinit var wallpaperManager: WallpaperManager

    private lateinit var applyButton: MaterialButton
    private lateinit var forHome: LinearLayout
    private lateinit var forLockscreen: LinearLayout
    private lateinit var forBoth: LinearLayout
    private lateinit var expandable: ExpandableLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.item_wallpaper_apply, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wallOverlay = view.findViewById(R.id.wallpaperPreviewImage)
        homeOverlay = view.findViewById(R.id.homescreenOverlay)
        lockOverlay = view.findViewById(R.id.lockscreenOverlay)
        applyButton = view.findViewById(R.id.wp_apply_button)
        forHome = view.findViewById(R.id.wp_apply_home)
        forLockscreen = view.findViewById(R.id.wp_apply_lockscreen)
        forBoth = view.findViewById(R.id.wp_apply_both)
        expandable = view.findViewById(R.id.wp_choice)
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        when (position) {
            0 -> homeOverlay.visibility = View.VISIBLE
            1 -> lockOverlay.visibility = View.VISIBLE
        }
        Glide.with(requireContext())
            .load(wallpaper.drawable)
            .into(wallOverlay)
        applyButton.setOnClickListener { expandable.setExpanded(expand = true, animate = true) }
        forHome.setOnClickListener {
            setWallpaper(
                wallpaper.drawable!!,
                WallpaperManager.FLAG_SYSTEM
            )
        }
        forLockscreen.setOnClickListener {
            setWallpaper(
                wallpaper.drawable!!,
                WallpaperManager.FLAG_LOCK
            )
        }
        forBoth.setOnClickListener { setWallpaper(wallpaper.drawable!!) }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requireDialog().setCanceledOnTouchOutside(true)
        requireDialog().window.setGravity(Gravity.TOP)
        requireDialog().window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun setWallpaper(bitmap: Bitmap, flag: Int) {
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(bitmap, width, height, true)
        doAsync {
            try {
                wallpaperManager.setBitmap(wallpaper, null, true, flag)
                uiThread { afterApply(flag) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
        Toast.makeText(requireContext(), "Wallpaper applied", Toast.LENGTH_SHORT).show()
        wallpaper.listener?.invoke(
            wallpaper.drawable!!, if (flag != null) {
                if (flag == WallpaperManager.FLAG_LOCK) Type.LOCKSCREEN else Type.HOME
            } else Type.BOTH
        )
        dismiss()
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