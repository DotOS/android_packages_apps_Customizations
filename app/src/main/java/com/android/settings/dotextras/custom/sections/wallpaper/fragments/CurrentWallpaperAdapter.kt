package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase

class CurrentWallpaperAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private var wallpaperBase: WallpaperBase? = null
    private var fragmentList = arrayListOf(
        HomeWallpaperFragment(),
        LockscreenWallpaperFragment()
    )

    constructor(fragmentActivity: FragmentActivity, wallpaperBase: WallpaperBase) : this(
        fragmentActivity
    ) {
        this.wallpaperBase = wallpaperBase
        fragmentList = arrayListOf(
            HomeWallpaperFragment(wallpaperBase),
            LockscreenWallpaperFragment(wallpaperBase)
        )
    }

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}