package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import androidx.recyclerview.widget.DiffUtil

class WallpaperSchedDiff(
    private val oldWalls: ArrayList<String>,
    private val newWalls: ArrayList<String>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldWalls.size

    override fun getNewListSize(): Int = newWalls.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldWalls[oldItemPosition] == newWalls[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUri = oldWalls[oldItemPosition]
        val newUri = newWalls[newItemPosition]
        return oldUri == newUri
    }
}