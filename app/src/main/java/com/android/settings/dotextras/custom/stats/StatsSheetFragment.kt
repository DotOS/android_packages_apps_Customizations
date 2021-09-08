package com.android.settings.dotextras.custom.stats

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.android.settings.dotextras.R
import com.android.settings.dotextras.databinding.FragmentSheetStatsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_sheet_settings.*

class StatsSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSheetStatsBinding? = null
    private val binding get() = _binding!!
    private var isChecked = false

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSheetStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                val sharedprefStats =
                    requireActivity().getSharedPreferences("dotStatsPrefs", Context.MODE_PRIVATE)
                pref_stats.setChecked(sharedprefStats.getBoolean(Constants.ALLOW_STATS, true))
                pref_stats.setOnCheckListener { _, isChecked ->
                    run {
                        val editor: SharedPreferences.Editor = sharedprefStats.edit()
                        editor.putBoolean(Constants.ALLOW_STATS, isChecked)
                        editor.apply()
                        this@StatsSheetFragment.isChecked = isChecked
                    }
                }
                prefApply.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isChecked)
            StatsBuilder(
                requireActivity().getSharedPreferences(
                    "dotStatsPrefs",
                    Context.MODE_PRIVATE
                )
            ).push(requireActivity())
    }
}