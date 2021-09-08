package com.dot.ui.utils

import android.app.WallpaperManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.StrictMode
import android.util.DisplayMetrics
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import com.dot.ui.R
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import java.io.IOException
import java.io.InputStream
import java.net.URL

fun WallpaperManager.setWallpaper(resources: Resources, drawable: Drawable, flag: Int, onSuccess: () -> Unit) {
    val display: DisplayMetrics = resources.displayMetrics
    val screenWidth = display.widthPixels
    val screenHeight = display.heightPixels
    suggestDesiredDimensions(screenWidth, screenHeight)
    val width = desiredMinimumWidth
    val height = desiredMinimumHeight
    val wallpaper = Bitmap.createScaledBitmap(drawable.toBitmap()!!, width, height, true)
    task {
        try {
            setBitmap(wallpaper, null, true, flag)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    } successUi {
        onSuccess
    }
}

fun WallpaperManager.setWallpaper(resources: Resources, drawable: Drawable, onSuccess: () -> Unit) {
    val display: DisplayMetrics = resources.displayMetrics
    val screenWidth = display.widthPixels
    val screenHeight = display.heightPixels
    suggestDesiredDimensions(screenWidth, screenHeight)
    val width = desiredMinimumWidth
    val height = desiredMinimumHeight
    val wallpaper = Bitmap.createScaledBitmap(drawable.toBitmap()!!, width, height, true)
    task {
        try {
            setBitmap(wallpaper, null, true, WallpaperManager.FLAG_LOCK)
            setBitmap(wallpaper, null, true, WallpaperManager.FLAG_SYSTEM)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    } successUi {
        onSuccess
    }
}

fun Drawable.toBitmap(): Bitmap? {
    val drawable = this
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

fun Context.uriToDrawable(uri: Uri): Drawable {
    val drawable = Drawable.createFromStream(contentResolver.openInputStream(uri), uri.toString())
    return scaleCropToFit(drawable.toBitmap()!!).toDrawable(resources)
}

fun Context.urlToDrawable(urlString: String): Drawable {
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
    return try {
        val bitmap = BitmapDrawable(
            Resources.getSystem(),
            BitmapFactory.decodeStream(URL(urlString).content as InputStream)
        )
        scaleCropToFit(bitmap.bitmap).toDrawable(resources)
    } catch (e: IOException) {
        ResourcesCompat.getDrawable(resources, android.R.color.transparent, theme)!!
    }
}

fun Context.scaleCropToFit(original: Bitmap): Bitmap {
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