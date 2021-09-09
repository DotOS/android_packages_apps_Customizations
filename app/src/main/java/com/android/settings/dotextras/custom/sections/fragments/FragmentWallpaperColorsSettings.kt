package com.android.settings.dotextras.custom.sections.fragments

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.custom.monet.MonetPack
import com.android.settings.dotextras.custom.monet.MonetPackAdapter
import com.android.settings.dotextras.custom.monet.PreferenceUtils
import com.android.settings.dotextras.custom.sections.GenericSection
import com.android.settings.dotextras.databinding.FragmentWallpaperColorsSettingsBinding
import com.dot.ui.DotMaterialPreference
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.core.WallpaperTypes
import kotlin.math.roundToInt

class FragmentWallpaperColorsSettings : GenericSection() {

    private var _binding: FragmentWallpaperColorsSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWallpaperColorsSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                monetChroma.seekBar!!.min = 50
                monetChroma.seekBar!!.max = 200
                monetChroma.seekBar!!.progress =
                    (Settings.Secure.getFloat(requireContext().contentResolver, "monet_chroma", 1.0f) * 100).roundToInt()
                monetChroma.countText!!.text =
                    Settings.Secure.getFloat(requireContext().contentResolver, "monet_chroma", 1.0f).toString()
                monetChroma.setOnProgressChangedPreference(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        monetChroma.countText!!.text = (progress.toFloat() / 100f).toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        featureManager.Secure().setFloat("monet_chroma", (seekBar!!.progress.toFloat() / 100f))
                        monetChroma.seekBar!!.progress =
                            (Settings.Secure.getFloat(requireContext().contentResolver, "monet_chroma", 1.0f) * 100).roundToInt()
                    }

                })
                monetLightness.seekBar(1, 1000, 25, "monet_lightness", 425f)
                val monet = getMonetCompat()
                val availableColors = monet.getAvailableWallpaperColors() ?: emptyList()
                val monetPacks = ArrayList<MonetPack>()
                val prefs = PreferenceUtils(requireContext())
                if (availableColors.isEmpty()) {
                    monetPackRecycler.visibility = View.GONE
                } else {
                    for (color in availableColors) {
                        monetPacks.add(MonetPack(requireContext(), color))
                    }
                    val adapter = MonetPackAdapter(requireContext(), monetPacks) { color ->
                        prefs.wallpaperColor = color
                    }
                    monetPackRecycler.adapter = adapter
                    monetPackRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                }
            }
        }
        lifecycleScope.launchWhenResumed {
            with(binding) {
                monetChroma.seekBar!!.progress =
                    (Settings.Secure.getFloat(requireContext().contentResolver, "monet_chroma", 1.0f) * 100).roundToInt()
                monetChroma.countText!!.text =
                    Settings.Secure.getFloat(requireContext().contentResolver, "monet_chroma", 1.0f).toString()
            }
        }
    }

    private fun DotMaterialPreference.seekBar(min: Int, max: Int, step: Int, setting: String, default: Float) {
        fun calcRaw(value: Int) = (value - min) / step
        fun calcValue(raw: Int) = raw * step
        var valueInternal = 0
        seekBar!!.min = min
        seekBar!!.max = calcRaw(max)
        seekBar!!.progress = calcRaw(
            (Settings.Secure.getFloat(requireContext().contentResolver, setting, default)).roundToInt())
        countText!!.text = Settings.Secure.getFloat(requireContext().contentResolver, setting, default).roundToInt().toString()

        setOnProgressChangedPreference(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val next = calcValue(progress)
                valueInternal = next
                countText!!.text = valueInternal.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                featureManager.Secure().setFloat(setting, valueInternal.toFloat())
            }
        })
    }

    private fun getMonetCompat(): MonetCompat {
        MonetCompat.setup(requireContext())
        MonetCompat.wallpaperSource = WallpaperTypes.WALLPAPER_SYSTEM
        val chroma = Settings.Secure.getFloat(requireContext().contentResolver, "monet_chroma", 1.0f).toDouble()
        val lightness = Settings.Secure.getFloat(requireContext().contentResolver, "monet_lightness", 425.0f).toDouble()
        return MonetCompat.getInstance(chroma, lightness)
    }
}