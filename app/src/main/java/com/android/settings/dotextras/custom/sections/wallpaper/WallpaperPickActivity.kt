package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.WallpaperInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPickActivity.Types.TYPE_EXCLUSIVES
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPickActivity.Types.TYPE_INCLUDED
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPickActivity.Types.TYPE_LIVE
import com.android.settings.dotextras.custom.sections.wallpaper.filters.WallpaperFilter
import com.android.settings.dotextras.custom.sections.wallpaper.filters.WallpaperFilterAdapter
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.custom.utils.ResourceHelper
import kotlinx.android.synthetic.main.activity_wallpaper_pick.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.json.JSONObject
import java.net.URL

class WallpaperPickActivity : AppCompatActivity(R.layout.activity_wallpaper_pick) {

    private var wallpapers = ArrayList<Wallpaper>()
    private var exlist = ArrayList<Wallpaper>()
    private var permlist = ArrayList<Wallpaper>()
    private var filters = ArrayList<WallpaperFilter>()
    private var filteredWalls = ArrayList<Wallpaper>()

    private var adapter: WallpaperPreviewAdapter? = null

    private val ROWS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.extras != null) {
            val type = intent.getIntExtra("TYPE", TYPE_INCLUDED)
            when (type) {
                TYPE_INCLUDED -> {
                    parseBuiltInWallpapers()
                }
                TYPE_LIVE -> {
                    parseLiveWallpapers()
                    monetWarning.visibility = View.VISIBLE
                }
                TYPE_EXCLUSIVES -> {
                    categoriesRecycler.visibility = View.VISIBLE
                    adapter = WallpaperPreviewAdapter(ArrayList(), this)
                    buildCategories()
                    parseExclusives()
                }
            }
        }
    }

    private fun parseLiveWallpapers() {
        val liveWalls = packageManager.queryIntentServices(
            Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA) as ArrayList<ResolveInfo>
        liveWalls.removeIf {
            val wallInfo = WallpaperInfo(this, it)
            //Excluded from Live Wallpapers
            wallInfo.packageName == "com.android.wallpaper"
        }
        wallRecycler.adapter = LiveWallpaperAdapter(liveWalls)
        wallRecycler.layoutManager = GridLayoutManager(this, ROWS)
    }

    private fun parseBuiltInWallpapers() {
        wallpapers.clear()
        collapsing_toolbar.title = getString(R.string.built_in_wallpapers)
        val wallpaperPreview = Wallpaper()
        wallpaperPreview.uri = getString(R.string.default_wallpaper_uri)
        wallpapers.add(wallpaperPreview)
        if (ResourceHelper.getDotWallsSupport(this)) {
            val walls = ResourceHelper.getDotWalls(this)
            val uris = ResourceHelper.getDotWallsUri(this)
            for (i in walls.indices) {
                val wallpaperExtra = Wallpaper()
                wallpaperExtra.uri = uris[i].toString()
                wallpapers.add(wallpaperExtra)
            }
        }
        wallRecycler.adapter = WallpaperPreviewAdapter(wallpapers, this)
        wallRecycler.layoutManager = GridLayoutManager(this, ROWS)
    }

    private fun buildCategories() {
        task {
            val filterList: ArrayList<WallpaperFilter> = ArrayList()
            val jsonObject = JSONObject(URL(getString(R.string.papers_url)).readText())
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
            runOnUiThread {
                parseExclusives()
                categoriesRecycler.adapter = WallpaperFilterAdapter(filters)
                categoriesRecycler.addItemDecoration(
                    ItemRecyclerSpacer(resources.getDimension(R.dimen.recyclerSpacerSmall), null, false)
                )
                categoriesRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun parseExclusives() {
        permlist.clear()
        task {
            val jsonObject = JSONObject(URL(getString(R.string.papers_url)).readText())
            val jsonArray = jsonObject.optJSONArray("Wallpapers")
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val url = jsonObject.optString("url")
                    val category = jsonObject.getString("category")
                    val wallpaper = Wallpaper()
                    wallpaper.url = url
                    wallpaper.category = category
                    permlist.add(wallpaper)
                }
            }
            val linkedHashSet = LinkedHashSet<Wallpaper>(permlist)
            permlist.clear()
            permlist.addAll(linkedHashSet)
            filteredWalls = permlist
        } successUi {
            if (filteredWalls.isNotEmpty() && filters.isNotEmpty()) {
                exlist = filteredWalls
                val tempList = ArrayList<Wallpaper>()
                tempList.addAll(exlist)
                tempList.removeIf { it.category != filters[0].category }
                runOnUiThread {
                    adapter!!.updateList(tempList)
                    adapter!!.notifyDataSetChanged()
                    wallRecycler.adapter = adapter
                    wallRecycler.layoutManager = GridLayoutManager(this, ROWS)
                }
            }
        } fail {
            throw it
        }
    }

    private fun reloadExclusives(filter: String?) {
        exlist.addAll(permlist)
        val tempList = ArrayList<Wallpaper>()
        tempList.addAll(exlist)
        if (filter != null) {
            tempList.removeIf { it.category != filter }
        }
        val linkedHashSet = LinkedHashSet<Wallpaper>(tempList)
        tempList.clear()
        tempList.addAll(linkedHashSet)
        (wallRecycler.adapter as WallpaperPreviewAdapter).updateList(tempList)
        (wallRecycler.adapter as WallpaperPreviewAdapter).notifyDataSetChanged()
        wallRecycler.adapter = (wallRecycler.adapter as WallpaperPreviewAdapter)
        wallRecycler.layoutManager = GridLayoutManager(this, ROWS)
    }

    object Types {
        //val TYPE_GALLERY = 0
        val TYPE_INCLUDED = 1
        val TYPE_LIVE = 2
        val TYPE_EXCLUSIVES = 3
    }
}