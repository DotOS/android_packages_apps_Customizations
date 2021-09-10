package com.android.settings.dotextras.custom.sections.wallpaper.colors

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.databinding.ItemWallpaperColorBinding
import com.dot.ui.utils.ResourceHelper

class WallpaperColorAdapter(val items: ArrayList<WallpaperColor>, private val activity: Activity) : RecyclerView.Adapter<WallpaperColorAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaperColor = items[position]
        with(holder.binding) {
            colorName.text = "${wallpaperColor.name} (${ResourceHelper.colorToHex(wallpaperColor.color)})"
            colorBackground.setBackgroundColor(wallpaperColor.color)
            if (ResourceHelper.isDark(wallpaperColor.color)) {
                colorName.setTextColor(Color.WHITE)
            } else {
                colorName.setTextColor(Color.DKGRAY)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWallpaperColorBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    private val layoutInflater by lazy {
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    data class ViewHolder(val binding: ItemWallpaperColorBinding) :
        RecyclerView.ViewHolder(binding.root)
}