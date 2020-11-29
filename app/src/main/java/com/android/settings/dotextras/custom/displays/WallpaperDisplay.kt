package com.android.settings.dotextras.custom.displays

import android.app.WallpaperManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R

class WallpaperDisplay : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wallpaper: ImageView = view.findViewById(R.id.defaultWallpaper)
        val wallpaperManager = WallpaperManager.getInstance(requireContext())
        wallpaper.setImageDrawable(wallpaperManager.drawable)
    }
}