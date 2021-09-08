package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.content.DialogInterface
import android.content.om.IOverlayManager
import android.os.Bundle
import android.os.ServiceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.sections.grid.*
import com.android.settings.dotextras.custom.sections.grid.onHandleCallback
import com.dot.ui.system.themes.FontPackAdapter
import com.dot.ui.system.themes.IconPackAdapter
import com.dot.ui.system.themes.ShapeAdapter
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.dot.ui.system.OverlayController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_sheet_theme_settings.*

class ThemeSettingsSheet : BottomSheetDialogFragment() {

    /**
     * 0 - clock face
     * 1 - grid
     * 2 - shape
     * 3 - font
     * 4 - icon pack
     */
    private var type = -1

    private var shouldUpdatePreviews = false

    companion object {
        fun newInstance(type: Int): ThemeSettingsSheet {
            val fragment = ThemeSettingsSheet()
            val bundle = Bundle()
            bundle.putInt("settings_type", type)
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
    ): View? {
        if (arguments == null) dismiss()
        type = requireArguments().getInt("settings_type", -1)
        return inflater.inflate(R.layout.fragment_sheet_theme_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (type) {
            0 -> {
                settingsTitle.text = getString(R.string.clockface)
                ClockfaceSettings().setup()
            }
            1 -> {
                settingsTitle.text = getString(R.string.grid_section)
                GridSettings().fetchOptions(false)
            }
            2 -> {
                settingsTitle.text = getString(R.string.icon_shapes)
                ShapeSettings().setup()
            }
            3 -> {
                settingsTitle.text = getString(R.string.fonts)
                FontSettings().setup()
            }
            4 -> {
                settingsTitle.text = getString(R.string.icon_pack)
                IconPackSettings().setup()
            }
            else -> dismiss()
        }
    }

    fun addRecyclerDecoration() {
        while (settingsRecycler.itemDecorationCount > 0) {
            settingsRecycler.removeItemDecorationAt(0)
        }
        settingsRecycler.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacerBigger),
                null,
                false
            )
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (shouldUpdatePreviews)
            (requireActivity() as BaseActivity).updatePreviews()
    }

    private open inner class ClockfaceSettings {
        private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"
        private lateinit var mClockManager: BaseClockManager

        fun setup() {
            val contentProviderClockProvider = ContentProviderClockProvider(requireActivity())
            mClockManager = object : BaseClockManager(ContentProviderClockProvider(requireActivity())) {
                override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                    callback?.invoke(true)
                }

                override fun lookUpCurrentClock(): String =
                    requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
            }
            val callback = object : ClockfaceRecyclerAdapter.ClockfaceCallback {
                override fun onApply(clockfaceCompat: ClockfaceCompat) {
                    shouldUpdatePreviews = true
                }
            }
            mClockManager.fetchOptions({ options ->
                run {
                    if (options != null) {
                        val cm = ClockManager(
                            requireContext().contentResolver,
                            contentProviderClockProvider
                        )
                        val optionsCompat = ArrayList<ClockfaceCompat>()
                        for (option in options) {
                            optionsCompat.add(ClockfaceCompat(option))
                        }
                        settingsRecycler.adapter = ClockfaceRecyclerAdapter(cm, callback, optionsCompat)
                        settingsRecycler.layoutManager =
                            GridLayoutManager(context, 3)
                        while (settingsRecycler.itemDecorationCount > 0) {
                            settingsRecycler.removeItemDecorationAt(0)
                        }
                        settingsRecycler.addItemDecoration(
                            GridSpacingItemDecoration(3, resources.getDimensionPixelSize(R.dimen.recyclerSpacer), true)
                        )
                        for (i in 0 until optionsCompat.size) {
                            if (optionsCompat[i].clockface.isActive(cm))
                                settingsRecycler.scrollToPosition(i)
                        }
                    }
                }
            }, false)
        }
    }

    private open inner class GridSettings {

        fun fetchOptions(reload: Boolean) {
            val mGridManager = GridOptionsManager(
                LauncherGridOptionsProvider(
                    requireContext(),
                    getString(R.string.grid_control_metadata_name)
                )
            )
            mGridManager.fetchOptions({ options ->
                run {
                    if (options != null) {
                        val optionsCompat: ArrayList<GridOptionCompat> = ArrayList()
                        for (option in options) {
                            val gridCompat = GridOptionCompat(option)
                            gridCompat.listener = {
                                run {
                                    fetchOptions(true)
                                    shouldUpdatePreviews = true
                                }
                            }
                            optionsCompat.add(gridCompat)
                        }
                        settingsRecycler.adapter = GridRecyclerAdapter(mGridManager, optionsCompat)
                        settingsRecycler.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        if (!reload) addRecyclerDecoration()
                    }
                }
            }, reload)
        }
    }

    private open inner class ShapeSettings {
        fun setup() {
            val overlayController = OverlayController(
                OverlayController.Categories.ICON_SHAPE_CATEGORY,
                requireContext().packageManager,
                IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
            )
            val shapes = overlayController.Shapes().getShapes(requireContext())
            val adapter = ShapeAdapter(overlayController, shapes)
            settingsRecycler.adapter = adapter
            while (settingsRecycler.itemDecorationCount > 0) {
                settingsRecycler.removeItemDecorationAt(0)
            }
            val lm = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            settingsRecycler.layoutManager = lm
            addRecyclerDecoration()
            val smoothScroller: RecyclerView.SmoothScroller =
                object : LinearSmoothScroller(requireContext()) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_END
                }
            for (i in 0 until shapes.size) {
                if (shapes[i].selected)
                    smoothScroller.targetPosition = i
            }
            lm.startSmoothScroll(smoothScroller)
        }
    }

    private open inner class FontSettings {
        fun setup() {
            val overlayController = OverlayController(
                OverlayController.Categories.FONT_CATEGORY,
                requireContext().packageManager,
                IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
            )
            val fonts = overlayController.FontPacks().getFontPacks(requireContext())
            val adapter = FontPackAdapter(overlayController, fonts)
            settingsRecycler.adapter = adapter
            while (settingsRecycler.itemDecorationCount > 0) {
                settingsRecycler.removeItemDecorationAt(0)
            }
            val lm = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            addRecyclerDecoration()
            settingsRecycler.layoutManager = lm
            val smoothScroller: RecyclerView.SmoothScroller =
                object : LinearSmoothScroller(requireContext()) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_END
                }
            for (i in 0 until fonts.size) {
                if (fonts[i].selected)
                    smoothScroller.targetPosition = i
            }
            lm.startSmoothScroll(smoothScroller)
        }
    }

    private open inner class IconPackSettings {
        fun setup() {
            val overlayController = OverlayController(
                OverlayController.Categories.ANDROID_ICON_PACK_CATEGORY,
                requireContext().packageManager,
                IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
            )
            val iconPacks = overlayController.IconPacks().getIconPacks(requireContext())
            val adapter = IconPackAdapter(overlayController, iconPacks)
            settingsRecycler.adapter = adapter
            while (settingsRecycler.itemDecorationCount > 0) {
                settingsRecycler.removeItemDecorationAt(0)
            }
            val lm = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            addRecyclerDecoration()
            settingsRecycler.layoutManager = lm
            val smoothScroller: RecyclerView.SmoothScroller =
                object : LinearSmoothScroller(requireContext()) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_END
                }
            for (i in 0 until iconPacks.size) {
                if (iconPacks[i].selected)
                    smoothScroller.targetPosition = i
            }
            lm.startSmoothScroll(smoothScroller)
        }
    }
}