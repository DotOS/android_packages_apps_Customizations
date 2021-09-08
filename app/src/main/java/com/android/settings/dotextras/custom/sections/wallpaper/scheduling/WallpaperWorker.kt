package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dot.ui.utils.setWallpaper
import com.dot.ui.utils.uriToDrawable
import java.io.IOException
import kotlin.collections.ArrayList

class WallpaperWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    var wallpapers = ArrayList<Wallpaper>()

    var usedCount = 0

    override fun doWork(): Result {
        val wallpaperArray = inputData.getStringArray("wallpapers")
        if (wallpaperArray != null) {
            for (wallpaper in wallpaperArray) {
                wallpapers.add(Wallpaper(wallpaper.toUri()))
            }
        }
        return try {
            if (usedCount != wallpapers.size) {
                applyWallpaper(WallpaperManager.getInstance(context))
            } else {
                SchedPrefs.deletePack(context)
            }
            Result.success()
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun applyWallpaper(wallpaperManager: WallpaperManager) {
        usedCount = 0
        for (image in wallpapers) {
            if (image.used) usedCount++
            else {
                wallpaperManager.setWallpaper(context.resources, context.uriToDrawable(image.uri)) {
                    Log.d("DotWorker", "Wallpaper $image set at ${System.currentTimeMillis()}")
                    image.used = true
                }
                break
            }
        }
    }

    class Wallpaper(val uri: Uri) {
        var used = false
    }

}