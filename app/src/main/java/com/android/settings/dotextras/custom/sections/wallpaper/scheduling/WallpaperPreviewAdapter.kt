package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.settings.dotextras.databinding.ItemSchedPickBinding

class WallpaperPreviewAdapter(
    var items: ArrayList<String>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<WallpaperPreviewAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaperUri = items[position]
        with(holder.binding) {
            imgAdd.isVisible = false
            imgPreview.load(wallpaperUri.toUri()) {
                crossfade(200)
            }
        }
    }

    private val layoutInflater by lazy {
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    data class ViewHolder(val binding: ItemSchedPickBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSchedPickBinding.inflate(layoutInflater, parent, false))
    }
}