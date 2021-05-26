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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.dot.extra.maintainers.MaintainersActivity
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.displays.*
import com.android.settings.dotextras.custom.sections.*
import com.dot.applock.AppLockActivity

class SectionFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var headerRecycler: RecyclerView
    private val content_titles = ArrayList<String>()
    private val content_displayfragments = ArrayList<Fragment>()
    private val content_sectionfragments = ArrayList<Fragment>()
    private val header_titles = ArrayList<String>()
    private val header_displayfragments = ArrayList<Fragment>()
    private val header_sectionfragments = ArrayList<Fragment>()
    private val standaloneFragments = ArrayList<Fragment>()
    private val startActivityFragments = HashMap<Fragment, AppCompatActivity>()
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
        recycler = view.findViewById(R.id.dashboard_Recycler)
        headerRecycler = view.findViewById(R.id.header_Recycler)
        recycler.isNestedScrollingEnabled = false
        headerRecycler.isNestedScrollingEnabled = false
        clean()
        addSections()
        addHeaders()
        dashadapter = DashboardAdapter(items, parentFragmentManager, parent!!)
        recycler.adapter = dashadapter
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        headeradapter = DashboardAdapter(header_items, parentFragmentManager, parent!!)
        headerRecycler.adapter = headeradapter
        headerRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun addHeaders() {
        buildHeader(R.string.section_wallpaper_title, WallpaperDisplay(), WallpaperSection(), true)
        buildHeader(R.string.section_aod_title, AODDisplay(), AODLockscreenSection(), false)
        for (i in header_titles.indices) {
            val header = DashboardItem(
                this,
                header_titles[i],
                header_sectionfragments[i],
                header_displayfragments[i]
            )
            header.standalone = standaloneFragments.contains(header_sectionfragments[i])
            header.longCard = true
            header_items.add(header)
        }
    }

    private fun buildHeader(resID: Int, display: Fragment, section: GenericSection, standalone: Boolean) {
        if (section.isAvailable(requireContext())) {
            header_titles.add(getString(resID))
            header_displayfragments.add(display)
            header_sectionfragments.add(section)
            if (standalone) standaloneFragments.add(section)
        }
    }

    private fun addSections() {
        buildSection(R.string.section_statusbar_title, StatusbarDisplay(), StatusbarSection())
        buildSection(R.string.section_qs_title, QSDisplay(), QSSection())
        buildSection(R.string.applock_title, ApplockDisplay(), AppLockActivity())
        buildSection(R.string.section_lab, LabDisplay(), LabSection())
        buildSection(R.string.section_clockface2, ClockfaceDisplay(), ClockfaceSection(), true)
        buildSection(R.string.section_fod_icon, FODIconDisplay(), FODIconSection(), true)
        buildSection(R.string.section_fod_anim, FODAnimDisplay(), FODAnimSection(), true)
        buildSection(R.string.section_fod_color_opt, FODOptDisplay(), FODOptSection())
        buildSection(R.string.section_hwkeys, HardwareKeysDisplay(), HardwareKeysSection())
        buildSection(R.string.section_themes_title, ThemeDisplay(), ThemeSection())
        buildSection(R.string.section_system_title, SystemDisplay(), SystemSection())
        buildSection(R.string.grid_section, GridDisplay(), GridSection(), true)
        buildSection(R.string.maintainers, MaintainersDisplay(), MaintainersActivity())
        for (i in content_titles.indices) {
            val item = DashboardItem(
                this,
                content_titles[i],
                content_sectionfragments[i],
                content_displayfragments[i]
            )
            item.standalone = standaloneFragments.contains(content_sectionfragments[i])
            if (startActivityFragments.containsKey(content_displayfragments[i])) {
                item.startActivity = startActivityFragments[content_displayfragments[i]]
            }
            items.add(item)
        }
    }

    private fun buildSection(resID: Int, display: Fragment, section: GenericSection) {
        if (section.isAvailable(requireContext())) {
            content_titles.add(getString(resID))
            content_displayfragments.add(display)
            content_sectionfragments.add(section)
        }
    }

    private fun buildSection(resID: Int, display: Fragment, startActivity: AppCompatActivity) {
        content_titles.add(getString(resID))
        content_displayfragments.add(display)
        content_sectionfragments.add(display)
        startActivityFragments[display] = startActivity
    }

    private fun buildSection(resID: Int, display: Fragment, section: GenericSection, standalone: Boolean) {
        if (section.isAvailable(requireContext())) {
            content_titles.add(getString(resID))
            content_displayfragments.add(display)
            content_sectionfragments.add(section)
            if (standalone) standaloneFragments.add(section)
        }
    }

    private fun clean() {
        content_titles.clear()
        content_sectionfragments.clear()
        content_displayfragments.clear()
        header_titles.clear()
        header_sectionfragments.clear()
        header_displayfragments.clear()
        standaloneFragments.clear()
        items.clear()
        header_items.clear()
    }
}