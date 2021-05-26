package com.android.settings.dotextras.custom.sections.lab

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.MonetManager
import com.google.android.material.card.MaterialCardView

class MonetColorAdapter(val items: ArrayList<MonetColor>) : RecyclerView.Adapter<MonetColorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_monet_color, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorSwatch: MonetColor = items[position]
        val monetManager = MonetManager(context = holder.itemView.context)
        holder.card.setCardBackgroundColor(colorSwatch.color)
        holder.card.isClickable = true
        var paletteType = 0
        when (colorSwatch.name) {
            "Vibrant" -> paletteType = 0
            "Light Vibrant" -> paletteType = 1
            "Dark Vibrant" -> paletteType = 2
            "Dominant" -> paletteType = 3
            "Muted" -> paletteType = 4
            "Light Muted" -> paletteType = 5
            "Dark Muted" -> paletteType = 6
        }
        holder.card.setOnClickListener {
            Toast.makeText(holder.itemView.context.applicationContext, "${colorSwatch.name} Palette applied.", Toast.LENGTH_SHORT).show()
            monetManager.setPaletteType(paletteType)
        }
        holder.paletteName.text = colorSwatch.name
        holder.paletteHex.text = ResourceHelper.colorToHex(colorSwatch.color)
        holder.paletteHex.setTextColor(colorSwatch.textColorBody)
        holder.paletteName.setTextColor(colorSwatch.textColorTitle)
        if (paletteType == monetManager.getPaletteType()) {
            holder.paletteChecked.visibility = View.VISIBLE
        } else {
            holder.paletteChecked.visibility = View.GONE
        }
        holder.paletteChecked.imageTintList = ColorStateList.valueOf(colorSwatch.textColorBody)
        holder.paletteChecked.backgroundTintList = ColorStateList.valueOf(colorSwatch.textColorTitle)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.requireViewById(R.id.monetColorCard)
        val paletteName: TextView = view.requireViewById(R.id.paletteName)
        val paletteHex: TextView = view.requireViewById(R.id.paletteHex)
        val paletteChecked: ImageView = view.requireViewById(R.id.paletteChecked)
    }

}