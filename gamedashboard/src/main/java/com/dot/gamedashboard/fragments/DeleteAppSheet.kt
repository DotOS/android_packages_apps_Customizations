package com.dot.gamedashboard.fragments

import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.transform.CircleCropTransformation
import com.dot.gamedashboard.Launcher
import com.dot.gamedashboard.R
import com.dot.gamedashboard.databinding.SheetDeleteAppBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.Serializable

class DeleteAppSheet: BottomSheetDialogFragment() {

    private var _binding: SheetDeleteAppBinding? = null
    private val binding get() = _binding!!

    private var pkg: Launcher.Package? = null
    private var callback: Callback? = null

    companion object {
        fun newInstance(type: Launcher.Package, callback: Callback): DeleteAppSheet {
            val fragment = DeleteAppSheet()
            val bundle = Bundle()
            bundle.putSerializable("pkg", type)
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
        pkg = requireArguments().getSerializable("pkg") as Launcher.Package
        _binding = SheetDeleteAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val packageManager = requireActivity().packageManager
        pkg?.let {
            val info: PackageInfo =
                packageManager.getPackageInfo(it.name!!, PackageManager.GET_META_DATA)
            binding.gAppIcon.load(info.applicationInfo.loadIcon(packageManager)) {
                transformations(CircleCropTransformation())
                crossfade(100)
            }
            binding.gAppName.text = info.applicationInfo.loadLabel(packageManager)
            binding.gCancel.setOnClickListener { dismiss() }
            binding.gDelete.setOnClickListener { _ ->
                callback?.onRemove(it)
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback?.onDismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface Callback: Serializable {
        fun onRemove(pkg: Launcher.Package)
        fun onDismiss()
    }
}