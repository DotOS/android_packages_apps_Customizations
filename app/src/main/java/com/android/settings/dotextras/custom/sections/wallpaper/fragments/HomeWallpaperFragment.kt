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
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase


class HomeWallpaperFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_wallpaper_preview_card, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val overlay: RelativeLayout = view.findViewById(R.id.homescreenOverlay)
        overlay.visibility = View.VISIBLE
        val wallpaper: ImageView = view.findViewById(R.id.wallpaperPreviewImage)
        val wallpaperManager = WallpaperManager.getInstance(requireContext())
        val pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
        BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
        wallpaper.setImageDrawable(wallpaperManager.drawable)
    }
}