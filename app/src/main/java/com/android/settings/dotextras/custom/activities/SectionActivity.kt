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
package com.android.settings.dotextras.custom.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.custom.sections.maintainers.MaintainersActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.DashboardAdapter
import com.android.settings.dotextras.custom.DashboardItem
import com.android.settings.dotextras.custom.displays.*
import com.android.settings.dotextras.custom.sections.*
import com.dot.applock.AppLockActivity
import kotlinx.android.synthetic.main.activity_sections.*

class SectionActivity : AppCompatActivity() {

    private val content_titles = ArrayList<String>()
    private val content_displayfragments = ArrayList<Fragment>()
    private val content_sectionfragments = ArrayList<Fragment>()
    private val header_titles = ArrayList<String>()
    private val header_displayfragments = ArrayList<Fragment>()
    private val header_sectionfragments = ArrayList<Fragment>()
    private val standaloneFragments = ArrayList<Fragment>()
    private val securedFragments = ArrayList<Fragment>()
    private val startActivityFragments = HashMap<Fragment, AppCompatActivity>()
    private var dashadapter: DashboardAdapter? = null
    private var items: ArrayList<DashboardItem> = ArrayList()

    private var header_items: ArrayList<DashboardItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sections)
        sectionToolbar.canGoBack(this)
        clean()
        addSections()
        dashadapter = DashboardAdapter(items, supportFragmentManager, this)
        dashboard_Recycler.adapter = dashadapter
        dashboard_Recycler.layoutManager = LinearLayoutManager(this)
    }


    private fun addHeaders() {
        buildHeader(R.string.section_aod_title, AODDisplay(), AODLockscreenSection(), false)
        for (i in header_titles.indices) {
            val header = DashboardItem(
                header_titles[i],
                header_sectionfragments[i],
                header_displayfragments[i]
            )
            header.standalone = standaloneFragments.contains(header_sectionfragments[i])
            header.longCard = true
            header_items.add(header)
        }
    }

    private fun buildHeader(
        resID: Int,
        display: Fragment,
        section: GenericSection,
        standalone: Boolean
    ) {
        if (section.isAvailable(this)) {
            header_titles.add(getString(resID))
            header_displayfragments.add(display)
            header_sectionfragments.add(section)
            if (standalone) standaloneFragments.add(section)
        }
    }

    private fun addSections() {
        buildSection(R.string.section_aod_title, AODDisplay(), AODLockscreenSection())
        buildSection(R.string.section_statusbar_title, StatusbarDisplay(), StatusbarSection())
        buildSection(R.string.section_qs_title, QSDisplay(), QSSection())
        buildSection(R.string.applock_title, ApplockDisplay(), AppLockActivity(), true)
        buildSection(R.string.section_fod, FODDisplay(), FODSection())
        buildSection(R.string.section_hwkeys, HardwareKeysDisplay(), HardwareKeysSection())
        buildSection(R.string.section_system_title, SystemDisplay(), SystemSection())
        buildSection(R.string.maintainers, MaintainersDisplay(), MaintainersActivity(), false)
        for (i in content_titles.indices) {
            val item = DashboardItem(
                content_titles[i],
                content_sectionfragments[i],
                content_displayfragments[i]
            )
            item.standalone = standaloneFragments.contains(content_sectionfragments[i])
            item.secured = securedFragments.contains(content_displayfragments[i])
            if (startActivityFragments.containsKey(content_displayfragments[i])) {
                item.startActivity = startActivityFragments[content_displayfragments[i]]
            }
            items.add(item)
        }
    }

    private fun buildSection(resID: Int, display: Fragment, section: GenericSection) {
        if (section.isAvailable(this)) {
            content_titles.add(getString(resID))
            content_displayfragments.add(display)
            content_sectionfragments.add(section)
        }
    }

    private fun buildSection(
        resID: Int,
        display: Fragment,
        startActivity: AppCompatActivity,
        secured: Boolean
    ) {
        content_titles.add(getString(resID))
        content_displayfragments.add(display)
        content_sectionfragments.add(display)
        startActivityFragments[display] = startActivity
        if (secured) securedFragments.add(display)
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