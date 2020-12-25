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

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.OverlayController

class AccentAdapter(
    private val overlayController: OverlayController,
    private val items: ArrayList<Accent>
) :
    RecyclerView.Adapter<AccentAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_accentcolor,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val accent: Accent = items[position]
        val nightModeFlags: Int = holder.preview.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> holder.preview.imageTintList =
                ColorStateList.valueOf(
                    overlayController.AccentColors().getDarkAccentColor(accent.packageName)
                )
            Configuration.UI_MODE_NIGHT_NO -> holder.preview.imageTintList =
                ColorStateList.valueOf(
                    overlayController.AccentColors().getLightAccentColor(accent.packageName)
                )
        }

        holder.preview.setOnClickListener {
            select(position)
            overlayController.AccentColors().setOverlay(accent.packageName, accent, holder)
        }
        updateSelection(accent, holder)
    }

    private fun updateSelection(accent: Accent, holder: ViewHolder) {
        if (accent.selected) {
            holder.preview.backgroundTintList =
                ColorStateList.valueOf(ResourceHelper.getAccent(holder.preview.context))
        } else {
            holder.preview.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        holder.preview.context,
                        android.R.color.transparent
                    )
                )
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i);
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val preview: ImageButton = view.findViewById(R.id.accentColorCircle)
    }

}