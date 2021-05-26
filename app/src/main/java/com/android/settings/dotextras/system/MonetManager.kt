package com.android.settings.dotextras.system

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.res.MonetWannabe
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.ParcelFileDescriptor
import com.android.internal.graphics.palette.Palette
import com.android.settings.dotextras.custom.sections.lab.MonetColor
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.MonetManager.InternalEngine.getBitmap

class MonetManager(val context: Context) {

    private val VIBRANT = 0
    private val LIGHT_VIBRANT = 1
    private val DARK_VIBRANT = 2
    private val DOMINANT = 3
    private val MUTED = 4
    private val LIGHT_MUTED = 5
    private val DARK_MUTED = 6

    private val monetWannabe = MonetWannabe(context)
    private val featureManager = FeatureManager(context.contentResolver)

    fun isEnabled(): Boolean = MonetWannabe.isMonetEnabled(context)

    fun getInactiveAccent(): Int = MonetWannabe.getInactiveAccent(context)

    fun getBackgroundColor(): Int = monetWannabe.accentColorBackground

    fun getOverlayColor(): Int =
        if (ResourceHelper.lightsOut(context))
            monetWannabe.accentColorOverlayDark
        else
            monetWannabe.accentColorOverlayLight

    fun getPaletteType(): Int = featureManager.Secure().getInt(featureManager.Secure().MONET_PALETTE, VIBRANT)

    fun setPaletteType(paletteType: Int) {
        featureManager.Secure().setInt(featureManager.Secure().MONET_PALETTE, paletteType)
        featureManager.Secure().setInt(featureManager.Secure().MONET_BASE_ACCENT, MonetWannabe.updateMonet(context))
    }

    fun getColorAmount(): Int = featureManager.Secure().getInt(featureManager.Secure().MONET_COLOR_GEN, MonetWannabe.DEFAULT_COLOR_GEN)

    fun getSwatchFromTarget(bitmap: Bitmap): Palette.Swatch {
        val palette = Palette.from(bitmap).maximumColorCount(getColorAmount()).generate()
        return when (getPaletteType()) {
            VIBRANT -> palette.vibrantSwatch
            LIGHT_VIBRANT -> palette.lightVibrantSwatch
            DARK_VIBRANT -> palette.darkVibrantSwatch
            DOMINANT -> palette.dominantSwatch
            MUTED -> palette.mutedSwatch
            LIGHT_MUTED -> palette.lightMutedSwatch
            DARK_MUTED -> palette.darkMutedSwatch
            else -> palette.vibrantSwatch
        }
    }

    fun getSwatchFromTarget(drawable: Drawable): Palette.Swatch {
        val palette = Palette.from(InternalEngine.drawableToBitmap(drawable)).maximumColorCount(getColorAmount()).generate()
        return when (getPaletteType()) {
            VIBRANT -> if (palette.vibrantSwatch != null) palette.vibrantSwatch else palette.swatches[0]
            LIGHT_VIBRANT -> if (palette.lightVibrantSwatch != null) palette.lightVibrantSwatch else palette.swatches[0]
            DARK_VIBRANT -> if (palette.darkVibrantSwatch != null) palette.darkVibrantSwatch else palette.swatches[0]
            DOMINANT -> if (palette.dominantSwatch != null) palette.dominantSwatch else palette.swatches[0]
            MUTED -> if (palette.mutedSwatch != null) palette.mutedSwatch else palette.swatches[0]
            LIGHT_MUTED -> if (palette.lightMutedSwatch != null) palette.lightMutedSwatch else palette.swatches[0]
            DARK_MUTED -> if (palette.darkMutedSwatch != null) palette.darkMutedSwatch else palette.swatches[0]
            else -> palette.swatches[0]
        }
    }

    fun getPalette(): Palette {
        return Palette.from(getBitmap(context)).maximumColorCount(getColorAmount()).generate()
    }

    fun getPaletteColors(): ArrayList<MonetColor> {
        val colors = ArrayList<MonetColor>()
        val palette = getPalette()
        if (palette.vibrantSwatch != null)
        colors.add(MonetColor(palette.vibrantSwatch.rgb, palette.vibrantSwatch.bodyTextColor, palette.vibrantSwatch.titleTextColor, "Vibrant"))
        if (palette.lightVibrantSwatch != null)
        colors.add(MonetColor(palette.lightVibrantSwatch.rgb, palette.lightVibrantSwatch.bodyTextColor, palette.lightVibrantSwatch.titleTextColor, "Light Vibrant"))
        if (palette.darkVibrantSwatch != null)
        colors.add(MonetColor(palette.darkVibrantSwatch.rgb, palette.darkVibrantSwatch.bodyTextColor, palette.darkVibrantSwatch.titleTextColor, "Dark Vibrant"))
        if (palette.dominantSwatch != null)
        colors.add(MonetColor(palette.dominantSwatch.rgb, palette.dominantSwatch.bodyTextColor, palette.dominantSwatch.titleTextColor, "Dominant"))
        if (palette.mutedSwatch != null)
        colors.add(MonetColor(palette.mutedSwatch.rgb, palette.mutedSwatch.bodyTextColor, palette.mutedSwatch.titleTextColor, "Muted"))
        if (palette.lightMutedSwatch != null)
        colors.add(MonetColor(palette.lightMutedSwatch.rgb, palette.lightMutedSwatch.bodyTextColor, palette.lightMutedSwatch.titleTextColor, "Light Muted"))
        if (palette.darkMutedSwatch != null)
        colors.add(MonetColor(palette.darkMutedSwatch.rgb, palette.darkMutedSwatch.bodyTextColor, palette.darkMutedSwatch.titleTextColor, "Dark Muted"))
        return colors
    }

    private object InternalEngine {
        fun getBitmap(context: Context): Bitmap? {
            val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(context)
            @SuppressLint("MissingPermission")
            val pfd: ParcelFileDescriptor? = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
            return if (pfd != null) {
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
            } else {
                drawableToBitmap(wallpaperManager.drawable)
            }
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap {
            val bit: Bitmap
            return if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                bit = if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                    Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                } else {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }
                val canvas = Canvas(bit)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bit
            }
        }
    }

}