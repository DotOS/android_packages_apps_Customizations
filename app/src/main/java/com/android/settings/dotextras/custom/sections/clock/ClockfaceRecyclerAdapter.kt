package com.android.settings.dotextras.custom.sections.clock
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
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.dot.ui.utils.ResourceHelper
import com.google.android.material.card.MaterialCardView

class ClockfaceRecyclerAdapter(
    private val clockManager: ClockManager,
    private val callback: ClockfaceCallback,
    private val items: ArrayList<ClockfaceCompat>,
) :
    RecyclerView.Adapter<ClockfaceRecyclerAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_clockface_option,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clockfaceCompat: ClockfaceCompat = items[position]
        val shouldSelect = clockfaceCompat.clockface.isActive(clockManager)
        if (shouldSelect) clockfaceCompat.selected = true
        clockfaceCompat.clockface.bindThumbnailTile(holder.clockThumbnail)
        holder.clockTitle.text = clockfaceCompat.clockface.getTitle()
        holder.clockLayout.setOnClickListener {
            clockManager.apply(clockfaceCompat.clockface) {}
            select(position)
            updateSelection(clockfaceCompat, holder)
        }
        updateSelection(clockfaceCompat, holder)
    }

    private fun updateSelection(clockfaceCompat: ClockfaceCompat, holder: ViewHolder) {
        val accentColor: Int = ResourceHelper.getAccent(holder.clockCard.context)
        if (clockfaceCompat.selected) {
            holder.clockCard.strokeColor = accentColor
            callback.onApply(clockfaceCompat)
        } else {
            holder.clockCard.strokeColor = holder.itemView.resources.getColor(
                com.android.internal.R.color.monet_background_secondary_device_default,
                holder.itemView.context.theme)
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            val selected = items[i].selected
            items[i].selected = pos == i
            if (selected != items[i].selected) notifyItemChanged(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clockThumbnail: AppCompatImageView = view.findViewById(R.id.clockThumbnail)
        val clockTitle: TextView = view.findViewById(R.id.clockTitle)
        val clockLayout: LinearLayout = view.findViewById(R.id.clockLayout)
        val clockCard: MaterialCardView = view.findViewById(R.id.clockCard)
    }

    interface ClockfaceCallback {
        fun onApply(clockfaceCompat: ClockfaceCompat)
    }
}