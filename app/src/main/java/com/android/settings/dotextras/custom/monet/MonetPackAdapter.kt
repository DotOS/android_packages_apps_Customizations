package com.android.settings.dotextras.custom.monet

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.databinding.ItemMonetColorPackBinding

class MonetPackAdapter(
    private val context: Context,
    private val colors: List<MonetPack>,
    private val onColorPicked: (Int) -> Unit
) : RecyclerView.Adapter<MonetPackAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount() = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMonetColorPackBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        val selectedWallpaperColor = PreferenceUtils(context).wallpaperColor
        color.selected = color.wallpaperColor == selectedWallpaperColor
        if (selectedWallpaperColor == -1)
            color.selected = position == 0
        with(holder.binding) {
            monetPackSelected.isVisible = color.selected
            monetPackLayout.setOnClickListener {
                onColorPicked.invoke(color.wallpaperColor)
                select(color.wallpaperColor)
            }
            val primaryAccent = color.getAccentPrimaryColor()
            val secondaryAccent = color.getAccentSecondaryColor()
            val tertiaryAccent = color.getAccentTertiaryColor()
            val backgroundColor = color.getBackgroundColor()
            if (primaryAccent != null) {
                monetAccentColor1.setImageColor(primaryAccent)
            }
            if (secondaryAccent != null) {
                monetAccentColor2.setImageColor(secondaryAccent)
            }
            if (tertiaryAccent != null) {
                monetAccentColor3.setImageColor(tertiaryAccent)
            }
            if (backgroundColor != null) {
                monetBackgroundColor.setImageColor(backgroundColor)
            }
            monetAccentColor1.isVisible = primaryAccent != null
            monetAccentColor2.isVisible = secondaryAccent != null
            monetAccentColor3.isVisible = tertiaryAccent != null
            monetBackgroundColor.isVisible = backgroundColor != null
        }
    }

    private fun select(color: Int) {
        for (i in colors.indices) {
            val selected = colors[i].selected
            colors[i].selected = colors[i].wallpaperColor == color
            if (selected != colors[i].selected) notifyItemChanged(i)
        }
    }

    private fun ImageView.setImageColor(color: Int) {
        setBackgroundColor(color)
    }

    data class ViewHolder(val binding: ItemMonetColorPackBinding) :
        RecyclerView.ViewHolder(binding.root)

}