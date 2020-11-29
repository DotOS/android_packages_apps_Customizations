package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.google.android.material.card.MaterialCardView
import java.io.IOException
import kotlin.math.roundToInt


class WallpaperPreviewAdapter(
    private val items: ArrayList<WallpaperPreview>,
    private val wallpaperManager: WallpaperManager,
) : RecyclerView.Adapter<WallpaperPreviewAdapter.ViewHolder>() {

    private val SELECT_PICTURE = 1

    private var selectedImagePath: String? = null

    private var contentResolver: ContentResolver? = null

    private var synteticActivity: Activity? = null

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_wallpaper_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaper: WallpaperPreview = items[position]
        contentResolver = holder.wallpaperPreview.context.contentResolver
        synteticActivity = holder.wallpaperPreview.context as Activity
        holder.wallpaperPreview.setImageDrawable(wallpaper.image)
        if (wallpaper.title != null) holder.title.text = wallpaper.title
        else holder.title.visibility = View.GONE
        if (wallpaper.type == wallpaper.GALLERY) {
            holder.wallpaperPreview.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    holder.wallpaperPreview.context as Activity, Intent.createChooser(
                        intent,
                        "Select Wallpaper"
                    ), SELECT_PICTURE, null
                )
            }
        }
        if (wallpaper.type == wallpaper.SYSTEM) {
            holder.wallpaperPreview.setOnClickListener {
                setWallpaper(wallpaperManager.builtInDrawable)
            }
            val layoutParams = holder.wallpaperHolder.layoutParams
            layoutParams.height =
                holder.wallpaperHolder.resources.getDimension(R.dimen.wallpaper_card_height)
                    .roundToInt()
            holder.wallpaperHolder.layoutParams = layoutParams
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri: Uri? = data.data
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    selectedImageUri
                )!!
                setWallpaper(bitmap)
            }
        }
    }

    private fun setWallpaper(bitmap: Bitmap) {
        val metrics = DisplayMetrics()
        val display: Display = synteticActivity!!.windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(bitmap, width, height, true)
        try {
            wallpaperManager.setBitmap(wallpaper)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setWallpaper(drawable: Drawable) {
        val metrics = DisplayMetrics()
        val display: Display = synteticActivity!!.windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, width, height, true)
        try {
            wallpaperManager.setBitmap(wallpaper)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.wallpaperTitle)
        val wallpaperPreview: ImageButton = view.findViewById(R.id.wallpaper_preview)
        val wallpaperHolder: MaterialCardView = view.findViewById(R.id.wall_holder)
    }

}