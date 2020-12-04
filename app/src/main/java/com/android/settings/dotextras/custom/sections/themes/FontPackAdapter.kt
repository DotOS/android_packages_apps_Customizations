package com.android.settings.dotextras.custom.sections.themes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.getNormalizedColor
import com.android.settings.dotextras.system.OverlayController

class FontPackAdapter(
    private val overlayController: OverlayController,
    private val items: ArrayList<FontPack>
) :
    RecyclerView.Adapter<FontPackAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_font_pack,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fontPack: FontPack = items[position]
        holder.label.text = fontPack.label
        if (holder.fontHeadline.typeface != null) holder.fontHeadline.typeface =
            fontPack.headLineFont
        if (holder.fontBody.typeface != null) holder.fontBody.typeface = fontPack.bodyFont
        holder.fontLayout.setOnClickListener {
            select(position)
            overlayController.FontPacks().setOverlay(fontPack.packageName, fontPack, holder)
        }
        updateSelection(fontPack, holder)
    }

    private fun updateSelection(fontPack: FontPack, holder: ViewHolder) {
        val accentColor: Int = ResourceHelper.getAccent(holder.fontLayout.context)
        if (fontPack.selected) {
            holder.fontLayout.setBackgroundColor(accentColor)
            holder.fontLayout.invalidate()
        } else {
            holder.fontLayout.setBackgroundColor(
                ContextCompat.getColor(
                    holder.fontLayout.context,
                    android.R.color.transparent
                )
            )
            holder.fontLayout.invalidate()
        }
        val normalizedTextColor: Int = holder.itemView.context.getNormalizedColor(
            holder.fontLayout.background!!,
            fontPack.selected
        )
        holder.divider.setBackgroundColor(normalizedTextColor)
        holder.fontHeadline.setTextColor(normalizedTextColor)
        holder.fontBody.setTextColor(normalizedTextColor)
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fontLayout: LinearLayout = view.findViewById(R.id.shapeLayout)
        val label: TextView = view.findViewById(R.id.shapeLabel)
        val fontHeadline: TextView = view.findViewById(R.id.fontHeadline)
        val fontBody: TextView = view.findViewById(R.id.fontBody)
        val divider: View = view.findViewById(R.id.fontDivider)
    }

}