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
package com.android.settings.dotextras.custom.sections.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type
import com.dot.ui.system.FeatureManager

class ListBottomSheetAdapter(
    val featureType: Int,
    val feature: String,
    val default: Int,
    private val items: ArrayList<Option>,
    private val callback: ListBottomSheet.Callback
) : RecyclerView.Adapter<ListBottomSheetAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListBottomSheetAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_object, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListBottomSheetAdapter.ViewHolder, position: Int) {
        val option: Option = items[position]
        val featureManager = FeatureManager(holder.itemView.context.contentResolver)
        when (featureType) {
            Type.SYSTEM -> option.selected =
                featureManager.System().getInt(feature, default) == option.value
            Type.SECURE -> option.selected =
                featureManager.Secure().getInt(feature, default) == option.value
            Type.GLOBAL -> option.selected =
                featureManager.Global().getInt(feature, default) == option.value
        }
        holder.itemEntry.text = option.entry
        holder.itemRadio.isChecked = option.selected
        holder.itemLayout.setOnClickListener {
            if (!option.selected) {
                when (featureType) {
                    Type.SYSTEM -> featureManager.System().setInt(feature, option.value)
                    Type.SECURE -> featureManager.Secure().setInt(feature, option.value)
                    Type.GLOBAL -> featureManager.Global().setInt(feature, option.value)
                }
                select(position)
                holder.itemRadio.isChecked = option.selected
                callback.onApply(option.entry)
            }
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i)
        }
    }

    override fun getItemCount(): Int = items.size

    fun getSelected(): Option? {
        for (option in items) {
            if (option.selected) return option
        }
        return null
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemRadio: RadioButton = view.findViewById(R.id.list_item_radio)
        val itemEntry: TextView = view.findViewById(R.id.list_item_entry)
        val itemLayout: LinearLayout = view.findViewById(R.id.list_item_layout)
    }

    class Option(val entry: String, val value: Int) {
        var selected = false
    }
}