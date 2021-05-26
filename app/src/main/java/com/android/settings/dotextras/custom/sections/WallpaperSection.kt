/*
 * Copyright (C) 2020 The dotOS Project
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
package com.android.settings.dotextras.custom.sections

import android.app.WallpaperManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.*
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.CurrentWallpaperAdapter
import com.android.settings.dotextras.custom.utils.DepthPageTransformer
import com.android.settings.dotextras.custom.utils.internetAvailable
import kotlinx.android.synthetic.main.section_wallpaper.*

@Suppress("NAME_SHADOWING")
class WallpaperSection() : GenericSection() {

    constructor(standalone: Boolean) : this() {
        this.standalone = standalone
    }

    private var standalone = false

    private var wallpaperManager: WallpaperManager? = null
    private var mClipboardManager: ClipboardManager? = null
    private lateinit var sectionTitle: TextView
    private lateinit var currentPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        return inflater.inflate(R.layout.section_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        useInitUI = false
        super.onViewCreated(view, savedInstanceState)
        sectionTitle = view.findViewById(R.id.section_wp_title)
        currentPager = view.findViewById(R.id.wallPager)
        currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
        currentPager.setPageTransformer(DepthPageTransformer())
        val pagerLeft: ImageButton = view.findViewById(R.id.wallLeft)
        val pagerRight: ImageButton = view.findViewById(R.id.wallRight)
        pagerLeft.setOnClickListener {
            currentPager.setCurrentItem(currentPager.currentItem - 1, true)
        }
        pagerRight.setOnClickListener {
            currentPager.setCurrentItem(currentPager.currentItem + 1, true)
        }
        mClipboardManager =
            requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        wp_gallery.setOnClickListener {
            getContent.launch("image/*")
        }
        wp_builtin.setOnClickListener {
            val intent = Intent(requireActivity(), WallpaperPickActivity::class.java)
            intent.putExtra("TYPE", WallpaperPickActivity.Types.TYPE_INCLUDED)
            startActivity(intent)
        }
        wp_live.setOnClickListener {
            val intent = Intent(requireActivity(), WallpaperPickActivity::class.java)
            intent.putExtra("TYPE", WallpaperPickActivity.Types.TYPE_LIVE)
            startActivity(intent)
        }
        wp_dot.setOnClickListener {
            val intent = Intent(requireActivity(), WallpaperPickActivity::class.java)
            intent.putExtra("TYPE", WallpaperPickActivity.Types.TYPE_EXCLUSIVES)
            if (requireContext().internetAvailable())
                startActivity(intent)
            else
                Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val wallpaperGallery = Wallpaper()
                wallpaperGallery.uri = uri.toString()
                val intent = Intent(activity, WallpaperApplyActivity::class.java)
                intent.putExtra("wallpaperObject", wallpaperGallery)
                requireActivity().startActivity(intent)
            }
        }

    override fun isAvailable(context: Context): Boolean =
        WallpaperManager.getInstance(context).isSetWallpaperAllowed && WallpaperManager.getInstance(
            context
        ).isWallpaperSupported

}