package com.android.settings.dotextras.custom.sections.wallpaper.colors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.android.settings.dotextras.databinding.FragmentSheetWallpaperColorsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.monetcompat.extensions.toArgb
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

class WallpaperColorFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentSheetWallpaperColorsBinding? = null
    private val binding get() = _binding!!
    private var _wallpaper: Wallpaper? = null
    private val wallpaper get() = _wallpaper!!

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        fun newInstance(wallpaper: Wallpaper): WallpaperColorFragment {
            val fragment = WallpaperColorFragment()
            val bundle = Bundle()
            bundle.putSerializable("wallpaper", wallpaper)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _wallpaper = arguments?.getSerializable("wallpaper") as Wallpaper
        _binding = FragmentSheetWallpaperColorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                task {
                    val wallpaperColors = ArrayList<WallpaperColor>()
                    val monetColors = MonetColors(requireContext(), wallpaper)
                    val colors = monetColors.wallpaperColors
                    monetColors.colors.primaryColor.toArgb().let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Wallpaper Primary Color"))
                    }
                    monetColors.colors.secondaryColor?.let { color ->
                        wallpaperColors.add(
                            WallpaperColor(
                                color.toArgb(),
                                "Wallpaper Secondary Color"
                            )
                        )
                    }
                    monetColors.colors.tertiaryColor?.let { color ->
                        wallpaperColors.add(
                            WallpaperColor(
                                color.toArgb(),
                                "Wallpaper Tertiary Color"
                            )
                        )
                    }
                    val accent = colors.accent1[100]?.toArgb()
                    accent?.let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Accent Dark"))
                    }
                    val accentLight = colors.accent1[500]?.toArgb()
                    accentLight?.let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Accent Light"))
                    }
                    val background = colors.neutral1[900]?.toArgb()
                    background?.let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Background Dark"))
                    }
                    val backgroundLight = colors.neutral1[50]?.toArgb()
                    backgroundLight?.let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Background Light"))
                    }
                    val backgroundSecondary = colors.neutral1[700]?.toArgb()
                    backgroundSecondary?.let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Background Secondary Dark"))
                    }
                    val backgroundSecondaryLight = colors.neutral1[100]?.toArgb()
                    backgroundSecondaryLight?.let { color ->
                        wallpaperColors.add(WallpaperColor(color, "Background Secondary Light"))
                    }
                    wallpaperColors
                } successUi {
                    requireActivity().runOnUiThread {
                        if (it.isNotEmpty()) {
                            colorsRecycler.adapter = WallpaperColorAdapter(it, requireActivity())
                            colorsRecycler.layoutManager = LinearLayoutManager(context)
                        }
                    }
                }
            }

        }
    }

}