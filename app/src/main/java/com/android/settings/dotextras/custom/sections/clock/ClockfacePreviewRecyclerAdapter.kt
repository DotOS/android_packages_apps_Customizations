package com.android.settings.dotextras.custom.sections.clock
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
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class ClockfacePreviewRecyclerAdapter(
    private val clockManager: ClockManager,
    private val items: ArrayList<ClockfaceCompat>,
) :
    RecyclerView.Adapter<ClockfacePreviewRecyclerAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_clockface_preview,
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
        holder.accentButton.imageTintList =
            ColorStateList.valueOf(ResourceHelper.getAccent(holder.itemView.context))
        clockfaceCompat.clockface.bindPreviewTile(holder.itemView)
        holder.clockfaceChip.text = clockfaceCompat.clockface.getTitle()
        holder.clockfaceLayout.setOnClickListener {
            clockManager.apply(clockfaceCompat.clockface) {}
            Snackbar.make(holder.itemView, "Clockface '${clockfaceCompat.clockface.getTitle()}' applied.", Snackbar.LENGTH_SHORT).show()
            select(position)
            updateSelection(clockfaceCompat, holder)
        }
        updateSelection(clockfaceCompat, holder)
    }

    private fun updateSelection(clockfaceCompat: ClockfaceCompat, holder: ViewHolder) {
        holder.clockfaceChip.isChecked = clockfaceCompat.selected
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i);
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val accentButton: AppCompatImageView = view.findViewById(R.id.clockface_button_accent)
        val clockfaceChip: Chip = view.findViewById(R.id.clockfaceChip)
        val clockfaceLayout: LinearLayout = view.findViewById(R.id.clockfaceLayout)
    }
}