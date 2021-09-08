package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.WallpaperInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPickActivity.Types.TYPE_EXCLUSIVES
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPickActivity.Types.TYPE_INCLUDED
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPickActivity.Types.TYPE_LIVE
import com.android.settings.dotextras.custom.sections.wallpaper.filters.WallpaperFilter
import com.android.settings.dotextras.custom.sections.wallpaper.filters.WallpaperFilterAdapter
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.databinding.ActivityWallpaperPickBinding
import com.dot.ui.utils.ResourceHelper
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.json.JSONObject
import java.net.URL

class WallpaperPickActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWallpaperPickBinding

    private var wallpapers = ArrayList<Wallpaper>()
    private var exlist = ArrayList<Wallpaper>()
    private var permlist = ArrayList<Wallpaper>()
    private var filters = ArrayList<WallpaperFilter>()
    private var filteredWalls = ArrayList<Wallpaper>()

    private var adapter: WallpaperPreviewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperPickBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                val context = this@WallpaperPickActivity
                if (intent.extras != null) {
                    wallpaperToolbar.canGoBack(context)
                    val type = intent.getIntExtra("TYPE", TYPE_INCLUDED)
                    val lm = GridLayoutManager(context, 2)
                    wallRecycler.layoutManager = lm
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
                            adapter = WallpaperPreviewAdapter(ArrayList(), context)
                            buildCategories()
                            parseExclusives()
                        }
                    }
                }
            }
        }
    }

    private fun parseLiveWallpapers() {
        val liveWalls = packageManager.queryIntentServices(
            Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA
        ) as ArrayList<ResolveInfo>
        liveWalls.removeIf {
            val wallInfo = WallpaperInfo(this, it)
            //Excluded from Live Wallpapers
            wallInfo.packageName == "com.android.wallpaper"
        }
        binding.wallRecycler.adapter = LiveWallpaperAdapter(liveWalls)
    }

    private fun parseBuiltInWallpapers() {
        wallpapers.clear()
        binding.wallpaperToolbar.title = getString(R.string.built_in_wallpapers)
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
        binding.wallRecycler.adapter = WallpaperPreviewAdapter(wallpapers, this)
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
                binding.categoriesRecycler.adapter = WallpaperFilterAdapter(filters)
                binding.categoriesRecycler.addItemDecoration(
                    ItemRecyclerSpacer(
                        resources.getDimension(R.dimen.recyclerSpacerSmall),
                        null,
                        false
                    )
                )
                binding.categoriesRecycler.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
                    binding.wallRecycler.adapter = adapter
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
        (binding.wallRecycler.adapter as WallpaperPreviewAdapter).updateList(tempList)
        binding.wallRecycler.adapter = (binding.wallRecycler.adapter as WallpaperPreviewAdapter)
    }

    object Types {
        const val TYPE_INCLUDED = 1
        const val TYPE_LIVE = 2
        const val TYPE_EXCLUSIVES = 3
    }
}