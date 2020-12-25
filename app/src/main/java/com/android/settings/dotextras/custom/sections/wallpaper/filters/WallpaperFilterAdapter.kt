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
package com.android.settings.dotextras.custom.sections.wallpaper.filters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.google.android.material.chip.Chip

class WallpaperFilterAdapter(private val items: ArrayList<WallpaperFilter>) :
    RecyclerView.Adapter<WallpaperFilterAdapter.ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_wall_filter,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaperFilter: WallpaperFilter = items[position]
        holder.chip.text = wallpaperFilter.category
        holder.chip.isChecked = wallpaperFilter.selected
        if (!holder.chip.isSelected) {
            holder.chip.isCheckable = true
            holder.chip.setOnCheckedChangeListener { _, isChecked ->
                run {
                    wallpaperFilter.selected = isChecked
                    if (isChecked && !recyclerView.isComputingLayout) {
                        select(position)
                        wallpaperFilter.listener?.invoke(wallpaperFilter.category)
                    }
                }
            }
        } else {
            holder.chip.isCheckable = false
            holder.chip.setOnCheckedChangeListener { _, _ -> holder.chip.isChecked = true }
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
        }
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chip: Chip = view.findViewById(R.id.filterChip)
    }

}