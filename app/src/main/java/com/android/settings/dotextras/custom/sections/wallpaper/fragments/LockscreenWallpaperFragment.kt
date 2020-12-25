package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase
import com.bumptech.glide.Glide


class LockscreenWallpaperFragment() : Fragment() {

    private lateinit var wallpaper: ImageView
    private var wallpaperBase: WallpaperBase? = null

    constructor(wallpaperBase: WallpaperBase) : this() {
        this.wallpaperBase = wallpaperBase
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val normal = inflater.inflate(R.layout.item_wallpaper_preview_card, container, false)
        val big = inflater.inflate(R.layout.item_wallpaper_preview_card_big, container, false)
        return if (wallpaperBase == null) normal else big
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val overlay: LinearLayout = view.findViewById(R.id.lockscreenOverlay)
        overlay.visibility = View.VISIBLE
        wallpaper = view.findViewById(R.id.wallpaperPreviewImage)
        if (wallpaperBase == null) {
            val wallpaperManager = WallpaperManager.getInstance(requireContext())
            var pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
            if (pfd == null) pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
            if (pfd != null)
                Glide.with(view)
                    .load(BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor))
                    .into(wallpaper)
            else
                Glide.with(view)
                    .load(wallpaperManager.drawable)
                    .into(wallpaper)
        } else {
            Glide.with(view)
                .load(wallpaperBase!!.drawable)
                .into(wallpaper)
        }
    }
}