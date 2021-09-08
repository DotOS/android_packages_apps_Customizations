package com.android.settings.dotextras.custom.sections.wallpaper

import androidx.recyclerview.widget.DiffUtil

class WallpaperDiff(
    private val oldWalls: ArrayList<Wallpaper>,
    private val newWalls: ArrayList<Wallpaper>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldWalls.size

    override fun getNewListSize(): Int = newWalls.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldWalls[oldItemPosition] == newWalls[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUri = oldWalls[oldItemPosition].uri
        val newUri = newWalls[newItemPosition].uri
        val oldUrl = oldWalls[oldItemPosition].url
        val newUrl = newWalls[newItemPosition].url
        return if (oldUri != null) oldUri == newUri
        else oldUrl == newUrl
    }
}