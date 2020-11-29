package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.Activity
import android.app.WallpaperInfo
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.google.android.material.card.MaterialCardView
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import kotlin.math.roundToInt

class LiveWallpaperAdapter(private val items: ArrayList<ResolveInfo>) :
    RecyclerView.Adapter<LiveWallpaperAdapter.ViewHolder>() {

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
        val resolveInfo: ResolveInfo = items[position]
        val layoutParams = holder.wallpaperHolder.layoutParams
        layoutParams.height =
            holder.wallpaperHolder.resources.getDimension(R.dimen.wallpaper_card_height)
                .roundToInt()
        holder.wallpaperHolder.layoutParams = layoutParams
        holder.wallpaperPreview.setOnClickListener {
            selectLiveWallpaper(holder.wallpaperPreview.context, resolveInfo)
        }
        holder.wallpaperPreview.setImageDrawable(
            getPreview(
                holder.wallpaperPreview.context,
                resolveInfo
            )
        )
    }

    private fun selectLiveWallpaper(context: Context, resolveInfo: ResolveInfo) {
        var info: WallpaperInfo? = null
        try {
            info = WallpaperInfo(context, resolveInfo)
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val component: ComponentName = info!!.component
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        startActivityForResult(context as Activity, intent, 155, null)
    }

    private fun getPreview(context: Context, resolveInfo: ResolveInfo): Drawable {
        var info: WallpaperInfo? = null
        try {
            info = WallpaperInfo(context, resolveInfo)
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return info!!.loadThumbnail(context.packageManager)
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wallpaperPreview: ImageButton = view.findViewById(R.id.wallpaper_preview)
        val wallpaperHolder: MaterialCardView = view.findViewById(R.id.wall_holder)
    }

}