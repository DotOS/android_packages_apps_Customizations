package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.settings.dotextras.databinding.ItemSchedPickBinding
import kotlin.random.Random

class WallpaperPackAdapter(
    var items: ArrayList<String>,
    private val activity: AppCompatActivity,
    private val callback: Callback
) : RecyclerView.Adapter<WallpaperPackAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaperUri = items[position]
        with(holder.binding) {
            val isFirst = wallpaperUri == "%HEADER%"
            imgAdd.isVisible = isFirst
            imgPreview.isVisible = !isFirst
            imgLoading.isVisible = false
            imgAdd.setOnClickListener {
                callback.addNewImage(this@WallpaperPackAdapter)
            }
            if (!isFirst) {
                imgPreview.load(wallpaperUri.toUri()) {
                    crossfade(200)
                }
                imgPreview.isClickable = true
                imgPreview.setOnLongClickListener {
                    callback.onImageDeleted(this@WallpaperPackAdapter, wallpaperUri.toUri())
                    true
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateWallpapers(list: ArrayList<String>) {
        items = list
        notifyDataSetChanged()
    }

    fun buildPack(): WallpaperPack {
        val pack = WallpaperPack(items)
        pack.identifier = Random.nextInt(1000, 9999) * 100 + items.size
        return pack
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

    interface Callback {
        fun addNewImage(adapter: WallpaperPackAdapter)
        fun onImageDeleted(adapter: WallpaperPackAdapter, uri: Uri)
    }
}