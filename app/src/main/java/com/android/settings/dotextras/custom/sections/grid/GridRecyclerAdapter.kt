package com.android.settings.dotextras.custom.sections.grid
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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar

class GridRecyclerAdapter(
    private val gridManager: GridOptionsManager,
    private val items: ArrayList<GridOptionCompat>,
) :
    RecyclerView.Adapter<GridRecyclerAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_grid_option,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gridOptionCompat: GridOptionCompat = items[position]
        gridOptionCompat.gridOption.bindThumbnailTile(holder.gridThumbnail)
        gridOptionCompat.selected = gridOptionCompat.gridOption.isActive()
        holder.gridTitle.text = gridOptionCompat.gridOption.title
        holder.gridLayout.setOnClickListener {
            gridManager.apply(gridOptionCompat.gridOption) {
                run {
                    gridOptionCompat.selected = it
                }
            }
            gridOptionCompat.listener?.invoke(gridOptionCompat.gridOption)
            Snackbar.make(holder.itemView,
                "Grid '${gridOptionCompat.gridOption.title}' applied.",
                Snackbar.LENGTH_SHORT).show()
            select(position)
            updateSelection(gridOptionCompat, holder)
        }
        holder.gridLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val scaleDownX = ObjectAnimator.ofFloat(
                        holder.gridCard,
                        "scaleX", 0.9f
                    )
                    val scaleDownY = ObjectAnimator.ofFloat(
                        holder.gridCard,
                        "scaleY", 0.9f
                    )
                    scaleDownX.duration = 200
                    scaleDownY.duration = 200
                    val scaleDown = AnimatorSet()
                    scaleDown.play(scaleDownX).with(scaleDownY)
                    scaleDown.start()
                }
                MotionEvent.ACTION_UP -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.gridCard, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.gridCard, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
                MotionEvent.ACTION_CANCEL -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.gridCard, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.gridCard, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
            }
            false
        }
        updateSelection(gridOptionCompat, holder)
    }

    private fun updateSelection(gridOptionCompat: GridOptionCompat, holder: ViewHolder) {
        val accentColor: Int = ResourceHelper.getAccent(holder.gridLayout.context)
        if (gridOptionCompat.selected) {
            holder.gridLayout.setBackgroundColor(accentColor)
            holder.gridLayout.invalidate(true)
        } else {
            holder.gridLayout.setBackgroundColor(ContextCompat.getColor(holder.gridLayout.context,
                android.R.color.transparent))
            holder.gridLayout.invalidate(true)
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i);
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gridThumbnail: AppCompatImageView = view.findViewById(R.id.gridThumbnail)
        val gridTitle: TextView = view.findViewById(R.id.gridTitle)
        val gridLayout: LinearLayout = view.findViewById(R.id.gridLayout)
        val gridCard: MaterialCardView = view.findViewById(R.id.gridCard)
    }
}