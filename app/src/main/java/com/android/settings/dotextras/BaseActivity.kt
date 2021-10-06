/*
 * Copyright (C) 2021 The dotOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dotextras

import android.animation.LayoutTransition
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.custom.sections.SettingsFragmentSheet
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.sections.grid.*
import com.dot.ui.system.items.AccentColor
import com.dot.ui.system.adapters.AccentPickerV2Adapter
import com.android.settings.dotextras.custom.sections.fragments.FragmentThemeSettings
import com.android.settings.dotextras.custom.sections.wallpaper.WallpapersActivity
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.custom.views.WallpaperPreviewSystem
import com.android.settings.dotextras.databinding.DashboardLayoutBinding
import com.dot.ui.system.MonetManager
import com.dot.ui.system.OverlayController
import com.dot.ui.utils.overlayController
import com.google.android.material.tabs.TabLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import com.android.settings.dotextras.custom.stats.Constants
import com.android.settings.dotextras.custom.stats.StatsBuilder
import com.android.settings.dotextras.custom.utils.MaidService
import com.google.android.material.button.MaterialButton

class BaseActivity : AppCompatActivity(),
    TabLayout.OnTabSelectedListener {

    lateinit var binding: DashboardLayoutBinding

    private lateinit var monetManager: MonetManager
    private var systemItems = ArrayList<AccentColor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = DashboardLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                val context = this@BaseActivity
                val sharedprefStats = getSharedPreferences("dotStatsPrefs", Context.MODE_PRIVATE)
                statsPreference.isVisible = sharedprefStats.getBoolean(Constants.IS_FIRST_LAUNCH, true)
                val allowStats = statsPreference.findViewById<MaterialButton>(R.id.allowStats)
                val dismissStats = statsPreference.findViewById<MaterialButton>(R.id.dismissStats)
                allowStats.setOnClickListener {
                    val editor: SharedPreferences.Editor = sharedprefStats!!.edit()
                    editor.putBoolean(Constants.ALLOW_STATS, true)
                    editor.putBoolean(Constants.IS_FIRST_LAUNCH, false)
                    editor.apply()
                    StatsBuilder(getSharedPreferences("dotStatsPrefs", MODE_PRIVATE)).push(context)
                    statsPreference.isVisible = false
                }
                dismissStats.setOnClickListener {
                    val editor: SharedPreferences.Editor = sharedprefStats!!.edit()
                    editor.putBoolean(Constants.ALLOW_STATS, false)
                    editor.putBoolean(Constants.IS_FIRST_LAUNCH, false)
                    editor.apply()
                    statsPreference.isVisible = false
                }
                startService(Intent(context, MaidService::class.java))
                monetManager = MonetManager(context)
                appblayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                dashboardToolbar.canGoBack(context)
                themeSelector.addOnTabSelectedListener(context)
                updatePreviews()
                launchSettings.setOnClickListener {
                    SettingsFragmentSheet().show(supportFragmentManager, "settings")
                }
                changeWallpaper.isEnabled = isWallpapersSupported()
                changeWallpaper.setOnClickListener {
                    val p1 = Pair.create(topCard as View?, "topCard")
                    val p2 = Pair.create(dashboardToolbar as View?, "toolbar")
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, p1, p2)
                    startActivity(Intent(context, WallpapersActivity::class.java), options.toBundle())
                }
                val overlayController = packageManager.overlayController(OverlayController.Categories.ACCENT_CATEGORY)
                systemItems.addAll(overlayController.AccentColors().getAccentColorsV2())
                val monetTab = themeSelector.getTabAt(0)
                val systemTab = themeSelector.getTabAt(1)
                themeSelector.selectTab(if (monetManager.isEnabled()) monetTab else systemTab)
                updateRecycler(themeSelector.selectedTabPosition)
            }
        }
    }

    fun updatePreviews() {
        WallpaperPreviewSystem(this,
            binding.previewContainerLauncher,
            binding.previewSurfaceLockscreen,
            binding.previewImageLockscreen)
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is FragmentThemeSettings && fragment.view != null) {
                fragment.updateSummaries()
            }
        }
    }

    private fun isWallpapersSupported(): Boolean {
        return WallpaperManager.getInstance(this).isSetWallpaperAllowed && WallpaperManager.getInstance(this).isWallpaperSupported
    }

    private fun updateDecorations() {
        while (binding.themeAccentPicker.itemDecorationCount > 0) {
            binding.themeAccentPicker.removeItemDecorationAt(0)
        }
        binding.themeAccentPicker.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacer),
                0,
                false
            )
        )
        binding.themeAccentPicker.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacerBig),
                binding.themeAccentPicker.adapter!!.itemCount - 1,
                true
            )
        )
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        monetManager.enableMonet(tab.position == 0)
        updateRecycler(tab.position)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun updateRecycler(position: Int) {
        when (position) {
            0 -> {
                binding.systemColorsSettings.visibility = View.GONE
                binding.wallpaperColorsSettings.visibility = View.VISIBLE
            }
            1 -> {
                binding.systemColorsSettings.visibility = View.VISIBLE
                binding.wallpaperColorsSettings.visibility = View.GONE
                binding.themeAccentPicker.adapter = AccentPickerV2Adapter(systemItems)
                binding.themeAccentPicker.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                updateDecorations()
            }
        }
    }

}