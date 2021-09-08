package com.dot.gamedashboard.fragments

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.dot.gamedashboard.AppsAdapter
import com.dot.gamedashboard.Launcher
import com.dot.gamedashboard.R
import com.dot.gamedashboard.databinding.SheetAddAppBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import java.io.Serializable

class AddAppSheet: BottomSheetDialogFragment() {

    private var mGamingPackages: MutableMap<String, Launcher.Package>? = null
    private var mGamingPackagesArray = ArrayList<Launcher.Package>()
    private var mGamingPackageList: String? = null

    private var _binding: SheetAddAppBinding? = null
    private val binding get() = _binding!!

    private var callback: Callback? = null

    companion object {
        fun newInstance(callback: Callback): AddAppSheet {
            val fragment = AddAppSheet()
            val bundle = Bundle()
            bundle.putSerializable("callback", callback)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (arguments == null) dismiss()
        callback = requireArguments().getSerializable("callback") as Callback
        _binding = SheetAddAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        task {
            val items = ArrayList<AppsAdapter.AppModel>()
            try {
                val intent = Intent(Intent.ACTION_MAIN, null)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val resolveInfoList: List<ResolveInfo> =
                    requireActivity().packageManager.queryIntentActivities(intent,
                        0)
                parsePackageList()
                for (resolveInfo in resolveInfoList) {
                    items.add(AppsAdapter.AppModel(resolveInfo, requireActivity().packageManager))
                    items.removeIf { filter ->
                        mGamingPackageList!!.contains(filter.mPackageName!!)
                    }
                }
                items.sortBy { it.mLabel }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            items
        } successUi  {
            requireActivity().runOnUiThread {
                val adapter = AppsAdapter(it)
                binding.loadingBar.visibility = View.GONE
                binding.gRecycler.adapter = adapter
                binding.gRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.gCancel.setOnClickListener { dismiss() }
                binding.gAdd.setOnClickListener {
                    callback?.onAdd(adapter.getApps())
                    dismiss()
                }
            }
        }
    }

    private fun parsePackageList(): Boolean {
        var parsed = false
        var gamingModeString: String? =
            Settings.System.getString(requireActivity().contentResolver, "gaming_mode_values")
        if (gamingModeString == null) gamingModeString = ""
        if (!TextUtils.equals(mGamingPackageList, gamingModeString)) {
            mGamingPackageList = gamingModeString
            mGamingPackages?.clear()
            mGamingPackages?.let { parseAndAddToMap(gamingModeString, it) }
            parsed = true
        }
        return parsed
    }

    private fun parseAndAddToMap(baseString: String?, map: MutableMap<String, Launcher.Package>) {
        if (baseString == null) {
            return
        }
        mGamingPackagesArray.clear()
        val array = TextUtils.split(baseString, "\\|")
        for (item in array) {
            if (TextUtils.isEmpty(item)) {
                continue
            }
            val pkg: Launcher.Package? = Launcher.Package.fromString(item)
            if (pkg != null) {
                map[pkg.name!!] = pkg
                mGamingPackagesArray.add(pkg)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface Callback: Serializable {
        fun onAdd(items: ArrayList<Launcher.Package>)
        fun onDismiss()
    }
}