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
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.LiveWallpaperAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPreviewAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.filters.WallpaperFilter
import com.android.settings.dotextras.custom.sections.wallpaper.filters.WallpaperFilterAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.CurrentWallpaperAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.onDismiss
import com.android.settings.dotextras.custom.sections.wallpaper.provider.StandalonePreviewActivity
import com.android.settings.dotextras.custom.utils.DepthPageTransformer
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.internetAvailable
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.json.JSONObject
import java.net.URL

@Suppress("NAME_SHADOWING")
class WallpaperSection() : GenericSection() {

    constructor(standalone: Boolean) : this() {
        this.standalone = standalone
    }

    private var standalone = false

    private var wallpaperManager: WallpaperManager? = null
    private var mClipboardManager: ClipboardManager? = null
    private var mBuiltInWallpapers = ArrayList<WallpaperBase>()
    private var exlist = ArrayList<WallpaperBase>()
    private var permlist = ArrayList<WallpaperBase>()
    private lateinit var sectionTitle: TextView
    private lateinit var currentPager: ViewPager2
    private lateinit var filters: ArrayList<WallpaperFilter>
    private lateinit var filteredWalls: ArrayList<WallpaperBase>
    private lateinit var adapter: WallpaperPreviewAdapter
    private val onDismissListener: onDismiss = {
        run {
            sectionTitle.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        return inflater.inflate(R.layout.section_wallpaper_header, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        useInitUI = false
        super.onViewCreated(view, savedInstanceState)
        if (!standalone) {
            (requireActivity() as BaseActivity).toggleAppBar(true)
            (requireActivity() as BaseActivity).enableSettingsLauncher(false)
        } else
            (requireActivity() as StandalonePreviewActivity).toggleAppBar(true)
        sectionTitle = view.findViewById(R.id.section_wp_title)
        currentPager = view.findViewById(R.id.wallPager)
        currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
        currentPager.setPageTransformer(DepthPageTransformer())
        adapter = WallpaperPreviewAdapter(
            exlist,
            wallpaperManager!!,
            this,
            currentPager,
            onDismissListener
        )
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

        parseBuiltInWallpapers()
        if (requireContext().internetAvailable()) {
            view.findViewById<LinearLayout>(R.id.dotextitle).visibility = View.VISIBLE
            buildCategories()
        } else {
            view.findViewById<LinearLayout>(R.id.dotextitle).visibility = View.GONE
        }
    }

    override fun isAvailable(context: Context): Boolean =
        WallpaperManager.getInstance(context).isSetWallpaperAllowed && WallpaperManager.getInstance(
            context).isWallpaperSupported

    private fun buildCategories() {
        val categoriesRecycler: RecyclerView =
            requireView().findViewById(R.id.dotCategoriesRecycler)
        task {
            val filterList: ArrayList<WallpaperFilter> = ArrayList()
            val jsonObject = JSONObject(URL(getString(R.string.papaers_url)).readText())
            val jsonArray = jsonObject.optJSONArray("Categories")
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val category = jsonArray.getJSONObject(i).getString("name")
                    val filter = WallpaperFilter(category)
                    filter.selected = category == jsonArray.getJSONObject(0).getString("name")
                    filter.listener = {
                        run {
                            reloadExclusives(filter.category)
                        }
                    }
                    filterList.add(filter)
                }
            }
            filters = filterList
        } successUi {
            requireActivity().runOnUiThread {
                parseExclusives()

                categoriesRecycler.adapter = WallpaperFilterAdapter(filters)
                categoriesRecycler.addItemDecoration(
                    ItemRecyclerSpacer(
                        requireContext().resources.getDimension(
                            R.dimen.recyclerSpacerSmall
                        ), 0, false
                    )
                )
                categoriesRecycler.addItemDecoration(
                    ItemRecyclerSpacer(
                        requireContext().resources.getDimension(
                            R.dimen.recyclerSpacerSmall
                        ), null, false
                    )
                )
                categoriesRecycler.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }
    }

    private fun parseLiveWallpapers() {
        val liveRecycler: RecyclerView = requireView().findViewById(R.id.liveRecycler)
        liveRecycler.adapter =
            LiveWallpaperAdapter(
                requireActivity().packageManager.queryIntentServices(
                    Intent(WallpaperService.SERVICE_INTERFACE),
                    PackageManager.GET_META_DATA
                ) as ArrayList<ResolveInfo>
            )
        liveRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun parseExclusives() {
        val exclusiveRecycler: RecyclerView = requireView().findViewById(R.id.dotRecycler)
        permlist.clear()
        task {
            val jsonObject = JSONObject(URL(getString(R.string.papaers_url)).readText())
            val jsonArray = jsonObject.optJSONArray("Wallpapers")
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val url = jsonObject.optString("url")
                    val category = jsonObject.getString("category")
                    val wallpaper = WallpaperBase(url)
                    wallpaper.category = category
                    wallpaper.type = wallpaper.WEB
                    wallpaper.listener = { _, _ ->
                        run {
                            currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
                        }
                    }
                    wallpaper.onPressed = {
                        run {
                            sectionTitle.visibility = View.GONE
                            if (standalone)
                                (requireActivity() as StandalonePreviewActivity).scrollTo(0, 0)
                            else
                                (requireActivity() as BaseActivity).scrollTo(0, 0)
                        }
                    }
                    permlist.add(wallpaper)
                }
            }
            filteredWalls = permlist
        } successUi {
            if (filteredWalls.isNotEmpty() && filters.isNotEmpty()) {
                exlist = filteredWalls
                val tempList = ArrayList<WallpaperBase>()
                tempList.addAll(exlist)
                tempList.removeIf { it.category != filters[0].category }
                requireActivity().runOnUiThread {
                    adapter.updateList(tempList)
                    adapter.notifyDataSetChanged()
                    exclusiveRecycler.adapter = adapter
                    exclusiveRecycler.layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }
            }
        } fail {
            throw it
        }

    }

    private fun reloadExclusives(filter: String?) {
        val exclusiveRecycler: RecyclerView = requireView().findViewById(R.id.dotRecycler)
        exlist.addAll(permlist)
        val tempList = ArrayList<WallpaperBase>()
        tempList.addAll(exlist)
        if (filter != null) {
            tempList.removeIf { it.category != filter }
        }
        (exclusiveRecycler.adapter as WallpaperPreviewAdapter).updateList(tempList)
        (exclusiveRecycler.adapter as WallpaperPreviewAdapter).notifyDataSetChanged()
        exclusiveRecycler.adapter = (exclusiveRecycler.adapter as WallpaperPreviewAdapter)
        exclusiveRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun parseBuiltInWallpapers() {
        mBuiltInWallpapers.clear()
        val builtInRecycler: RecyclerView = requireView().findViewById(R.id.builtInRecycler)
        val wallpaperPreview = WallpaperBase(wallpaperManager!!.builtInDrawable)
        wallpaperPreview.type = wallpaperPreview.SYSTEM
        wallpaperPreview.uri = Uri.parse("android.resource://android/drawable/default_wallpaper")
        wallpaperPreview.listener = { _, _ ->
            run {
                currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
            }
        }
        wallpaperPreview.onPressed = {
            run {
                sectionTitle.visibility = View.GONE
                if (standalone)
                    (requireActivity() as StandalonePreviewActivity).scrollTo(0, 0)
                else
                    (requireActivity() as BaseActivity).scrollTo(0, 0)
            }
        }
        val wallpaperGallery =
            WallpaperBase(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
        wallpaperGallery.type = wallpaperGallery.GALLERY
        wallpaperGallery.listener = { _, _ ->
            run {
                currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
            }
        }
        wallpaperGallery.onPressed = {
            run {
                sectionTitle.visibility = View.GONE
                if (standalone)
                    (requireActivity() as StandalonePreviewActivity).scrollTo(0, 0)
                else
                    (requireActivity() as BaseActivity).scrollTo(0, 0)
            }
        }
        mBuiltInWallpapers.add(wallpaperGallery)
        mBuiltInWallpapers.add(wallpaperPreview)
        if (ResourceHelper.getDotWallsSupport(requireContext())) {
            val walls = ResourceHelper.getDotWalls(requireContext())
            val uris = ResourceHelper.getDotWallsUri(requireContext())
            for (i in walls.indices) {
                val wallpaperExtra = WallpaperBase(walls[i])
                wallpaperExtra.type = wallpaperPreview.SYSTEM
                wallpaperExtra.uri = uris[i]
                wallpaperExtra.listener = { _, _ ->
                    run {
                        currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
                    }
                }
                wallpaperExtra.onPressed = {
                    run {
                        sectionTitle.visibility = View.GONE
                        if (standalone)
                            (requireActivity() as StandalonePreviewActivity).scrollTo(0, 0)
                        else
                            (requireActivity() as BaseActivity).scrollTo(0, 0)
                    }
                }
                mBuiltInWallpapers.add(wallpaperExtra)
            }
        }
        val adapter = WallpaperPreviewAdapter(
            mBuiltInWallpapers,
            wallpaperManager!!,
            this,
            currentPager,
            onDismissListener
        )
        builtInRecycler.adapter = adapter
        builtInRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }
}