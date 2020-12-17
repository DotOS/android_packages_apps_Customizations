package com.android.settings.dotextras.custom.sections

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.*
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.CurrentWallpaperAdapter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class WallpaperSection : GenericSection() {

    private var wallpaperManager: WallpaperManager? = null
    private var mClipboardManager: ClipboardManager? = null
    private var mBuiltInWallpapers = ArrayList<WallpaperBase>()
    private lateinit var currentPager: ViewPager2

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
        (requireActivity() as BaseActivity).toggleAppBar(true)
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
        parseExclusives()
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
        val exclusiveRecycler: RecyclerView = requireView().findViewById(R.id.builtInRecycler)
        val exlist: ArrayList<WallpaperBase> = ArrayList()
        val jsonObject = JSONObject(URL(getString(R.string.papaers_url)).readText())
        val jsonArray = jsonObject.optJSONArray("Wallpapers")
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val url = jsonObject.optString("url")
            val name = jsonObject.optString("name")
            val wallpaper = WallpaperBase(url)
            wallpaper.title = name
            wallpaper.type = wallpaper.WEB
            exlist.add(wallpaper)
        }
        val adapter = WallpaperPreviewAdapter(
            mBuiltInWallpapers,
            wallpaperManager!!,
            this,
            currentPager
        )
        exclusiveRecycler.adapter = adapter
        exclusiveRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

    }

    private fun setWallpaper(bitmap: Bitmap) {
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager!!.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager!!.desiredMinimumWidth
        val height = wallpaperManager!!.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(bitmap, width, height, true)
        try {
            wallpaperManager!!.bitmap = wallpaper
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseBuiltInWallpapers() {
        val builtInRecycler: RecyclerView = requireView().findViewById(R.id.builtInRecycler)
        val wallpaperPreview = WallpaperBase(wallpaperManager!!.builtInDrawable)
        wallpaperPreview.type = wallpaperPreview.SYSTEM
        wallpaperPreview.listener = { drawable, type ->
            run {
                currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
            }
        }
        mBuiltInWallpapers.add(wallpaperPreview)
        val wallpaperGallery = WallpaperBase(requireContext().getDrawable(R.drawable.ic_add))
        wallpaperGallery.type = wallpaperGallery.GALLERY
        wallpaperGallery.listener = { drawable, type ->
            run {
                currentPager.adapter = CurrentWallpaperAdapter(requireActivity())
            }
        }
        mBuiltInWallpapers.add(wallpaperGallery)
        val adapter = WallpaperPreviewAdapter(
            mBuiltInWallpapers,
            wallpaperManager!!,
            this,
            currentPager
        )
        builtInRecycler.adapter = adapter
        builtInRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun setColor(view: View, color: Color) {
        view.backgroundTintList = ColorStateList.valueOf(
            Color.rgb(
                color.red(),
                color.green(),
                color.blue()
            )
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setTextColor(view: TextView, color: Color) {
        view.text = "#${
            Integer.toHexString(Color.rgb(color.red(), color.green(), color.blue())).substring(2)
                .toUpperCase(Locale.getDefault())
        }"
    }

    private fun addClipboardLogic(view: TextView) {
        view.setOnLongClickListener {
            mClipboardManager!!.primaryClip = ClipData.newPlainText("text", view.text.toString())
            Toast.makeText(
                requireActivity().applicationContext, "Color copied",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }
}