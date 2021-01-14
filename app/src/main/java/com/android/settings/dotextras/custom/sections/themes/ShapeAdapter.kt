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

class ShapeAdapter(
    private val overlayController: OverlayController,
    private val items: ArrayList<Shape>,
) :
    RecyclerView.Adapter<ShapeAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_shapes,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shape: Shape = items[position]
        holder.label.text = shape.label
        holder.preview.setImageDrawable(
            overlayController.Shapes().createShapeDrawable(holder.preview.context, shape.path)
        )
        holder.shapeLayout.setOnClickListener {
            select(position)
            overlayController.Shapes().setOverlay(shape.packageName, shape, holder)
        }
        updateSelection(shape, holder)
    }

    private fun updateSelection(shape: Shape, holder: ViewHolder) {
        val accentColor: Int = ResourceHelper.getAccent(holder.shapeLayout.context)
        if (shape.selected) {
            holder.shapeLayout.setBackgroundColor(accentColor)
            holder.shapeLayout.invalidate()
        } else {
            holder.shapeLayout.setBackgroundColor(
                ContextCompat.getColor(
                    holder.shapeLayout.context,
                    android.R.color.transparent
                )
            )
            holder.shapeLayout.invalidate()
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val shapeLayout: LinearLayout = view.findViewById(R.id.shapeLayout)
        val label: TextView = view.findViewById(R.id.shapeLabel)
        val preview: ImageView = view.findViewById(R.id.shapePreview)
    }

}