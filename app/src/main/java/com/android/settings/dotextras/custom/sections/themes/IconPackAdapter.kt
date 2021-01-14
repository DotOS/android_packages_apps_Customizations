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
package com.android.settings.dotextras.custom.sections.themes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.OverlayController

class IconPackAdapter(
    private val overlayController: OverlayController,
    private val items: ArrayList<IconPack>,
) :
    RecyclerView.Adapter<IconPackAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_icon_pack,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconPack: IconPack = items[position]
        val iconArray: ArrayList<ImageView> =
            arrayListOf(holder.icon0, holder.icon1, holder.icon2, holder.icon3)
        holder.label.text = iconPack.label
        for (i in iconPack.drawableNames.indices) {
            iconArray[i].setImageDrawable(
                overlayController.IconPacks().loadIconPreviewDrawable(
                    iconPack.drawableNames[i],
                    iconPack.packageName
                )
            )
        }
        holder.iconPackLayout.setOnClickListener {
            select(position)
            overlayController.IconPacks().setOverlay(iconPack.packageName, iconPack, holder)
        }
        updateSelection(iconPack, holder)
    }

    private fun updateSelection(iconPack: IconPack, holder: ViewHolder) {
        val accentColor: Int = ResourceHelper.getAccent(holder.iconPackLayout.context)
        if (iconPack.selected) {
            holder.iconPackLayout.setBackgroundColor(accentColor)
            holder.iconPackLayout.invalidate()
        } else {
            holder.iconPackLayout.setBackgroundColor(
                ContextCompat.getColor(
                    holder.iconPackLayout.context,
                    android.R.color.transparent
                )
            )
            holder.iconPackLayout.invalidate()
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconPackLayout: LinearLayout = view.findViewById(R.id.iconpack_layout)
        val label: TextView = view.findViewById(R.id.iconPackLabel)
        val icon0: ImageView = view.findViewById(R.id.icon0)
        val icon1: ImageView = view.findViewById(R.id.icon1)
        val icon2: ImageView = view.findViewById(R.id.icon2)
        val icon3: ImageView = view.findViewById(R.id.icon3)
    }

}