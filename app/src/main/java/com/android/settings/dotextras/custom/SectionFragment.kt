package com.android.settings.dotextras.custom

import android.app.WallpaperManager
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
import com.android.settings.dotextras.custom.utils.ResourceHelper

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
        val wallpaperManager = WallpaperManager.getInstance(requireContext())
        if (wallpaperManager.isSetWallpaperAllowed && wallpaperManager.isWallpaperSupported) {
            header_titles.add(getString(R.string.section_wallpaper_title))
            header_sectionfragments.add(WallpaperSection())
            header_displayfragments.add(WallpaperDisplay())
        }
        //Titles
        header_titles.add(getString(R.string.section_aod_title))
        //Sections
        header_sectionfragments.add(AODSection())
        //Displays
        header_displayfragments.add(AODDisplay())
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

    private fun addSections() {
        //Titles
        content_titles.add(getString(R.string.section_statusbar_title))
        content_titles.add(getString(R.string.section_qs_title))
        if (ResourceHelper.hasFodSupport(requireContext()))
            content_titles.add(getString(R.string.section_fod_title))
        content_titles.add(getString(R.string.section_themes_title))
        //content_titles.add(getString(R.string.section_icons_title))
        content_titles.add(getString(R.string.section_system_title))
        //Sections
        content_sectionfragments.add(StatusbarSection())
        content_sectionfragments.add(QSSection())
        if (ResourceHelper.hasFodSupport(requireContext()))
            content_sectionfragments.add(FODSection())
        content_sectionfragments.add(ThemeSection())
        //content_sectionfragments.add(IconsSection())
        content_sectionfragments.add(SystemSection())
        //Displays
        content_displayfragments.add(StatusbarDisplay())
        content_displayfragments.add(QSDisplay())
        if (ResourceHelper.hasFodSupport(requireContext()))
            content_displayfragments.add(FODDisplay())
        content_displayfragments.add(ThemeDisplay())
        //content_displayfragments.add(IconsDisplay())
        content_displayfragments.add(SystemDisplay())
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