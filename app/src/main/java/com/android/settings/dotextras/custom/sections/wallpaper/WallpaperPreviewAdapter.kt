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

import android.content.ContentResolver
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.item_wallpaper_preview_card_big.*

class WallpaperPreviewAdapter(private var items: ArrayList<Wallpaper>, private val activity: AppCompatActivity) : RecyclerView.Adapter<WallpaperPreviewAdapter.ViewHolder>() {

    private lateinit var contentResolver: ContentResolver

    private lateinit var wallpaperBase: Wallpaper

    fun glideListener(holder: ViewHolder) = object: RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            holder.loader.visibility = View.GONE
            Toast.makeText(holder.loader.context, "Error loading wallpaper", Toast.LENGTH_SHORT).show()
            if (e != null) Log.e("GlideException", e.stackTrace.toString())
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            holder.loader.visibility = View.GONE
            return false
        }

    }

    fun updateList(list: ArrayList<Wallpaper>) {
        if (items.isEmpty()) {
            items = list
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(WallpaperDiff(items, list), true)
            diffResult.dispatchUpdatesTo(this)
            items.clear()
            items.addAll(list)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_wallpaper_preview, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaper: Wallpaper = items[position]
        wallpaperBase = wallpaper
        holder.loader.visibility = View.VISIBLE
        if (wallpaper.uri != null) {
            Glide.with(activity)
                .load(Uri.parse(wallpaper.uri))
                .listener(glideListener(holder))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.wallpaperPreview)
        } else if (wallpaper.url != null) {
            Glide.with(holder.wallpaperPreview)
                .load(Uri.parse(wallpaper.url))
                .listener(glideListener(holder))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.wallpaperPreview)
        }
        holder.wallpaperPreview.setOnClickListener {
            val intent = Intent(activity, WallpaperApplyActivity::class.java)
            intent.putExtra("wallpaperObject", wallpaper)
            activity.startActivity(intent)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wallpaperPreview: ImageButton = view.findViewById(R.id.wallpaper_preview)
        val loader: ProgressBar = view.findViewById(R.id.imgLoading)
    }

}