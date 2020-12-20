package com.android.settings.dotextras.custom.sections

import android.app.WallpaperManager
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
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
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.custom.utils.internetAvailable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class WallpaperSection() : GenericSection() {

    constructor(standalone: Boolean) : this() {
        this.standalone = standalone
    }

    private var standalone = false

    private var wallpaperManager: WallpaperManager? = null
    private var mClipboardManager: ClipboardManager? = null
    private var mBuiltInWallpapers = ArrayList<WallpaperBase>()
    var exlist = ArrayList<WallpaperBase>()
    var permlist = ArrayList<WallpaperBase>()
    private lateinit var sectionTitle: TextView
    private lateinit var currentPager: ViewPager2
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
        super.onViewCreated(view, savedInstanceState)
        if (!standalone)
            (requireActivity() as BaseActivity).toggleAppBar(true)
        else
            (requireActivity() as StandalonePreviewActivity).toggleAppBar(true)
        sectionTitle = view.findViewById(R.id.section_wp_title)
        currentPager = view.findViewById(R.id.wallPager)
        currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
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

    val filterList: ArrayList<WallpaperFilter> = ArrayList()
    private fun buildCategories() {
        val categoriesRecycler: RecyclerView =
            requireView().findViewById(R.id.dotCategoriesRecycler)
        parseExclusives()
        doAsync {
            val jsonObject = JSONObject(URL(getString(R.string.papaers_url)).readText())
            val jsonArray = jsonObject.optJSONArray("Categories")
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
            uiThread {
                categoriesRecycler.adapter = WallpaperFilterAdapter(filterList)
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
        val adapter = WallpaperPreviewAdapter(
            exlist,
            wallpaperManager!!,
            this,
            currentPager,
            onDismissListener
        )
        doAsync {
            val jsonObject = JSONObject(URL(getString(R.string.papaers_url)).readText())
            val jsonArray = jsonObject.optJSONArray("Wallpapers")
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
            if (permlist.size == jsonArray.length() && permlist.isNotEmpty()) {
                uiThread {
                    exlist = permlist
                    val tempList = ArrayList<WallpaperBase>()
                    tempList.addAll(exlist)
                    tempList.removeIf { it.category != filterList[0].category }
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
        val builtInRecycler: RecyclerView = requireView().findViewById(R.id.builtInRecycler)
        val wallpaperPreview = WallpaperBase(wallpaperManager!!.builtInDrawable)
        wallpaperPreview.type = wallpaperPreview.SYSTEM
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
        mBuiltInWallpapers.add(wallpaperPreview)
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