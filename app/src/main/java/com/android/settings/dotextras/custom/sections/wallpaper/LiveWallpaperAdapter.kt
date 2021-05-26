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