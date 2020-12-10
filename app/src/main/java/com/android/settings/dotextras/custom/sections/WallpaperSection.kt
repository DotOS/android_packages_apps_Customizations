package com.android.settings.dotextras.custom.sections

import android.annotation.SuppressLint
import android.app.WallpaperColors
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.LiveWallpaperAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPreview
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperPreviewAdapter
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class WallpaperSection : Fragment() {

    private var wallpaperManager: WallpaperManager? = null

    private var primaryView: View? = null
    private var secondaryView: View? = null
    private var tertiaryView: View? = null
    private var primaryTextView: TextView? = null
    private var secondaryTextView: TextView? = null
    private var tertiaryTextView: TextView? = null
    private var secondaryLayout: LinearLayout? = null
    private var tertiaryLayout: LinearLayout? = null

    private var mClipboardManager: ClipboardManager? = null

    private var mBuiltInWallpapers = ArrayList<WallpaperPreview>()

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
        val wallpaper: ImageView = view.findViewById(R.id.myWallpaper)
        mClipboardManager =
            requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        primaryView = view.findViewById(R.id.primaryColor)
        secondaryView = view.findViewById(R.id.secondaryColor)
        tertiaryView = view.findViewById(R.id.tertiaryColor)
        primaryTextView = view.findViewById(R.id.primaryColorText)
        secondaryTextView = view.findViewById(R.id.secondaryColorText)
        tertiaryTextView = view.findViewById(R.id.tertiaryColorText)
        secondaryLayout = view.findViewById(R.id.secondaryLayout)
        tertiaryLayout = view.findViewById(R.id.tertiaryLayout)
        wallpaper.setImageDrawable(wallpaperManager!!.drawable)
        generateWallColors(wallpaperManager!!.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)!!)
        parseBuiltInWallpapers()
        parseLiveWallpapers()
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

    private fun setWallpaper(bitmap: Bitmap) {
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager!!.suggestDesiredDimensions(screenWidth, screenHeight);
        val width = wallpaperManager!!.desiredMinimumWidth
        val height = wallpaperManager!!.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(bitmap, width, height, true)
        try {
            wallpaperManager!!.setBitmap(wallpaper)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseBuiltInWallpapers() {
        val builtInRecycler: RecyclerView = requireView().findViewById(R.id.builtInRecycler)
        val wallpaperPreview = WallpaperPreview(wallpaperManager!!.builtInDrawable)
        wallpaperPreview.type = wallpaperPreview.SYSTEM
        mBuiltInWallpapers.add(wallpaperPreview)
        val adapter = WallpaperPreviewAdapter(mBuiltInWallpapers, wallpaperManager!!)
        builtInRecycler.adapter = adapter
        builtInRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun generateWallColors(wallpaperColors: WallpaperColors) {
        val primaryColor = wallpaperColors.primaryColor
        val secondaryColor = wallpaperColors.secondaryColor
        val tertiaryColor = wallpaperColors.tertiaryColor
        setColor(primaryView!!, primaryColor)
        setTextColor(primaryTextView!!, primaryColor)
        if (secondaryColor != null) {
            setColor(secondaryView!!, secondaryColor)
            setTextColor(secondaryTextView!!, secondaryColor)
        } else {
            secondaryLayout!!.visibility = View.GONE
        }
        if (tertiaryColor != null) {
            setColor(tertiaryView!!, tertiaryColor)
            setTextColor(tertiaryTextView!!, tertiaryColor)
        } else {
            tertiaryLayout!!.visibility = View.GONE
        }
        addClipboardLogic(primaryTextView!!)
        addClipboardLogic(secondaryTextView!!)
        addClipboardLogic(tertiaryTextView!!)
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
            mClipboardManager!!.setPrimaryClip(ClipData.newPlainText("text", view.text.toString()))
            Toast.makeText(
                requireActivity().applicationContext, "Color copied",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }
}