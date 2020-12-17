package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R


class LockscreenWallpaperFragment : Fragment() {

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
        val overlay: LinearLayout = view.findViewById(R.id.lockscreenOverlay)
        overlay.visibility = View.VISIBLE
        val wallpaper: ImageView = view.findViewById(R.id.wallpaperPreviewImage)
        val wallpaperManager = WallpaperManager.getInstance(requireContext())
        var pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
        if (pfd == null) pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
        wallpaper.setImageBitmap(BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor))
    }


}