/*
 * Copyright (C) 2021 The dotOS Project
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.android.settings.dotextras.databinding.ItemWallpaperPreviewBinding

class WallpaperPreviewAdapter(
    private var items: ArrayList<Wallpaper>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<WallpaperPreviewAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private lateinit var wallpaperBase: Wallpaper

    @SuppressLint("NotifyDataSetChanged")
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
        return ViewHolder(
            ItemWallpaperPreviewBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaper: Wallpaper = items[position]
        wallpaperBase = wallpaper
        with(holder.binding) {
            imgLoading.visibility = View.VISIBLE
            if (wallpaper.uri != null) {
                wallpaperPreview.load(Uri.parse(wallpaper.uri)) {
                    diskCachePolicy(CachePolicy.ENABLED)
                    memoryCachePolicy(CachePolicy.ENABLED)
                    crossfade(300)
                    listener(onError = { _, _ ->
                        imgLoading.visibility = View.GONE
                        Toast.makeText(activity, "Error loading wallpaper $position", Toast.LENGTH_SHORT)
                            .show()
                    }, onSuccess = { _, _ ->
                        imgLoading.visibility = View.GONE
                    })
                }
            } else if (wallpaper.url != null) {
                wallpaperPreview.load(wallpaper.url) {
                    diskCachePolicy(CachePolicy.ENABLED)
                    memoryCachePolicy(CachePolicy.ENABLED)
                    crossfade(300)
                    listener(onError = { _, _ ->
                        imgLoading.visibility = View.GONE
                        Toast.makeText(activity, "Error loading wallpaper $position", Toast.LENGTH_SHORT)
                            .show()
                    }, onSuccess = { _, _ ->
                        imgLoading.visibility = View.GONE
                    })
                }
            }
            wallpaperPreview.setOnClickListener {
                val intent = Intent(activity, WallpaperApplyActivity::class.java)
                intent.putExtra("wallpaperObject", wallpaper)
                activity.startActivity(intent)
            }
        }
    }

    data class ViewHolder(val binding: ItemWallpaperPreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

}