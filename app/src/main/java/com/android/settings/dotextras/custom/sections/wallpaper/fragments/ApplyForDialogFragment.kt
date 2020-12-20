package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import android.app.WallpaperManager
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Type
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase
import com.android.settings.dotextras.custom.sections.wallpaper.onDismiss
import com.android.settings.dotextras.custom.views.ExpandableLayout
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException

class ApplyForDialogFragment(val wallpaper: WallpaperBase) : DialogFragment() {

    var dismissListener: onDismiss = null

    private lateinit var wallpaperManager: WallpaperManager

    private lateinit var forHome: LinearLayout
    private lateinit var forLockscreen: LinearLayout
    private lateinit var forBoth: LinearLayout
    private lateinit var expandable: ExpandableLayout

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        requireDialog().window.setGravity(Gravity.BOTTOM)
        requireDialog().window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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