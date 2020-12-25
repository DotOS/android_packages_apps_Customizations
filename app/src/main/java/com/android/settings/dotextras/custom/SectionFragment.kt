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
package com.android.settings.dotextras.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.displays.*
import com.android.settings.dotextras.custom.sections.*
import com.android.settings.dotextras.custom.utils.AutoFitGridLayoutManager

class SectionFragment : Fragment() {

    private val content_titles = ArrayList<String>()
    private val content_displayfragments = ArrayList<Fragment>()
    private val content_sectionfragments = ArrayList<Fragment>()
    private val header_titles = ArrayList<String>()
    private val header_displayfragments = ArrayList<Fragment>()
    private val header_sectionfragments = ArrayList<Fragment>()
    private var dashadapter: DashboardAdapter? = null
    private var headeradapter: DashboardAdapter? = null
    private var items: ArrayList<DashboardItem> = ArrayList()
    private var header_items: ArrayList<DashboardItem> = ArrayList()
    private var parent: BaseActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = requireActivity() as BaseActivity
        val recycler: RecyclerView = view.findViewById(R.id.dashboard_Recycler)
        val headerRecycler: RecyclerView = view.findViewById(R.id.header_Recycler)
        clean()
        addSections()
        dashadapter = DashboardAdapter(items, parentFragmentManager, parent!!)
        recycler.adapter = dashadapter
        recycler.layoutManager = AutoFitGridLayoutManager(
            requireContext(),
            resources.getDimension(R.dimen.default_card_width)
        )
        addHeaders()
        headeradapter = DashboardAdapter(header_items, parentFragmentManager, parent!!)
        headerRecycler.adapter = headeradapter
        headerRecycler.layoutManager = AutoFitGridLayoutManager(
            requireContext(),
            resources.getDimension(R.dimen.default_card_width)
        )
    }

    private fun addHeaders() {
        buildHeader(R.string.section_wallpaper_title, WallpaperDisplay(), WallpaperSection())
        buildHeader(R.string.section_aod_title, AODDisplay(), AODLockscreenSection())
        for (i in header_titles.indices) {
            val header = DashboardItem(
                this,
                header_titles[i],
                header_sectionfragments[i],
                header_displayfragments[i]
            )
            header.longCard = true
            header_items.add(header)
        }
    }

    private fun buildHeader(resID: Int, display: Fragment, section: GenericSection) {
        if (section.isAvailable(requireContext())) {
            header_titles.add(getString(resID))
            header_displayfragments.add(display)
            header_sectionfragments.add(section)
        }
    }

    private fun addSections() {
        buildSection(R.string.section_statusbar_title, StatusbarDisplay(), StatusbarSection())
        buildSection(R.string.section_qs_title, QSDisplay(), QSSection())
        buildSection(R.string.section_fod_title, FODDisplay(), FODSection())
        buildSection(R.string.section_themes_title, ThemeDisplay(), ThemeSection())
        buildSection(R.string.section_system_title, SystemDisplay(), SystemSection())
        //build(R.string.section_icons_title, IconsDisplay(), IconsSection())
        for (i in content_titles.indices) {
            items.add(
                DashboardItem(
                    this,
                    content_titles[i],
                    content_sectionfragments[i],
                    content_displayfragments[i]
                )
            )
        }
    }

    private fun buildSection(resID: Int, display: Fragment, section: GenericSection) {
        if (section.isAvailable(requireContext())) {
            content_titles.add(getString(resID))
            content_displayfragments.add(display)
            content_sectionfragments.add(section)
        }
    }

    private fun clean() {
        content_titles.clear()
        content_sectionfragments.clear()
        content_displayfragments.clear()
        header_titles.clear()
        header_sectionfragments.clear()
        header_displayfragments.clear()
        items.clear()
        header_items.clear()
    }
}