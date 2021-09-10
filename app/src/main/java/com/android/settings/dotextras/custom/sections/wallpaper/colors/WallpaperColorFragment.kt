package com.android.settings.dotextras.custom.sections.wallpaper.colors

import android.app.WallpaperColors
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.android.settings.dotextras.databinding.FragmentSheetWallpaperColorsBinding
import com.dot.ui.utils.uriToDrawable
import com.dot.ui.utils.urlToDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.monetcompat.extensions.toArgb
import dev.kdrag0n.monet.colors.CieLab
import dev.kdrag0n.monet.colors.Illuminants
import dev.kdrag0n.monet.colors.Srgb
import dev.kdrag0n.monet.colors.Zcam
import dev.kdrag0n.monet.theme.ZcamDynamicColorScheme
import dev.kdrag0n.monet.theme.ZcamMaterialYouTargets
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import kotlin.math.log10
import kotlin.math.pow

class WallpaperColorFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentSheetWallpaperColorsBinding? = null
    private val binding get() = _binding!!
    private var _wallpaper: Wallpaper? = null
    private val wallpaper get() = _wallpaper!!

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {

        private const val WHITE_LUMINANCE_MIN = 1.0
        private const val WHITE_LUMINANCE_MAX = 10000.0
        private const val WHITE_LUMINANCE_USER_MAX = 1000

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
                val drawable: Drawable = if (wallpaper.uri != null) {
                    requireContext().uriToDrawable(Uri.parse(wallpaper.uri))
                } else {
                    requireContext().urlToDrawable(wallpaper.url!!)
                }
                task {
                    val wallpaperColors = ArrayList<WallpaperColor>()
                    val colors = WallpaperColors.fromDrawable(drawable)
                    val chroma = Settings.Secure.getFloat(
                        requireContext().contentResolver,
                        "monet_chroma",
                        1.0f
                    ).toDouble()
                    val lightness = Settings.Secure.getInt(
                        requireContext().contentResolver,
                        "monet_lightness",
                        425
                    )
                    val cond = createZcamViewingConditions(parseWhiteLuminanceUser(lightness))
                    colors.let {
                        val primaryColor = it.primaryColor.toArgb()
                        val colors = ZcamDynamicColorScheme(
                            ZcamMaterialYouTargets(chroma, true, cond),
                            Srgb(primaryColor),
                            chroma,
                            cond
                        )
                        val accent = colors.accent1[100]?.toArgb()
                        primaryColor.let { color ->
                            wallpaperColors.add(WallpaperColor(color, "Wallpaper Primary Color"))
                        }
                        it.secondaryColor?.let { color ->
                            wallpaperColors.add(
                                WallpaperColor(
                                    color.toArgb(),
                                    "Wallpaper Secondary Color"
                                )
                            )
                        }
                        it.tertiaryColor?.let { color ->
                            wallpaperColors.add(
                                WallpaperColor(
                                    color.toArgb(),
                                    "Wallpaper Tertiary Color"
                                )
                            )
                        }
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

    private fun parseWhiteLuminanceUser(userValue: Int): Double {
        val userSrc = userValue.toDouble() / WHITE_LUMINANCE_USER_MAX
        val userInv = 1.0 - userSrc
        return (10.0).pow(userInv * log10(WHITE_LUMINANCE_MAX))
            .coerceAtLeast(WHITE_LUMINANCE_MIN)
    }

    private fun createZcamViewingConditions(whiteLuminance: Double) = Zcam.ViewingConditions(
        Zcam.ViewingConditions.SURROUND_AVERAGE,
        0.4 * whiteLuminance,
        CieLab(50.0, 0.0, 0.0).toCieXyz().y * whiteLuminance,
        Illuminants.D65 * whiteLuminance, whiteLuminance
    )

}