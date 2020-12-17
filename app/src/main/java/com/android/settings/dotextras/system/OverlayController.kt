package com.android.settings.dotextras.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.om.IOverlayManager
import android.content.om.OverlayInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.os.AsyncTask
import android.os.RemoteException
import android.os.UserHandle.USER_SYSTEM
import android.text.TextUtils
import android.util.Log
import android.util.PathParser
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.themes.*
import com.android.settings.dotextras.custom.utils.ResourceHelper

class OverlayController(
    val mCategory: String,
    var packageManager: PackageManager,
    var overlayManager: IOverlayManager,
) {

    private val TAG = "OverlayController"
    private val PACKAGE_DEVICE_DEFAULT = "package_device_default"
    private val OVERLAY_TARGET_PACKAGE = "android"
    private val OVERLAY_INFO_COMPARATOR: Comparator<OverlayInfo> =
        Comparator.comparingInt { a -> a.priority }

    object Categories {
        const val NOTCH_CATEGORY = "com.android.internal.display_cutout_emulation"
        const val FONT_CATEGORY = "android.theme.customization.font"
        const val ICON_SHAPE_CATEGORY = "android.theme.customization.adaptive_icon_shape"
        const val NAVIGATION_BAR_CATEGORY = "com.android.internal.navigation_bar_mode"
        const val ANDROID_ICON_PACK_CATEGORY = "android.theme.customization.icon_pack.android"
        const val SETTINGS_ICON_PACK_CATEGORY = "android.theme.customization.icon_pack.settings"
        const val SYSUI_ICON_PACK_CATEGORY = "android.theme.customization.icon_pack.systemui"
        const val LAUNCHER_ICON_PACK_CATEGORY = "android.theme.customization.icon_pack.launcher"
        const val ACCENT_CATEGORY = "android.theme.customization.accent_color"
    }

    object Packages {
        const val NAVBAR_LONG_OVERLAY_PKG = "com.dot.overlay.systemui.gestural.long"
        const val NAVBAR_MEDIUM_OVERLAY_PKG = "com.dot.overlay.systemui.gestural.medium"
        const val HIDDEN_OVERLAY_PKG = "com.dot.overlay.systemui.gestural.hidden"
    }

    object Constants {
        const val CONFIG_ICON_MASK = "config_icon_mask"
        const val CONFIG_BODY_FONT_FAMILY = "config_bodyFontFamily"
        const val CONFIG_HEADLINE_FONT_FAMILY = "config_headlineFontFamily"
        val ICONS_FOR_PREVIEW = arrayListOf(
            "ic_wifi_signal_3",
            "ic_qs_bluetooth",
            "ic_qs_flashlight",
            "ic_battery_80_24dp"
        )
        const val PATH_SIZE = 100f
    }

    inner class Shapes {
        @SuppressLint("StaticFieldLeak")
        fun setOverlay(
            packageName: String,
            shape: Shape,
            holder: ShapeAdapter.ViewHolder
        ): Boolean {
            val currentPackageName = getOverlayInfos().stream()
                .filter { info: OverlayInfo -> info.isEnabled }
                .map { info: OverlayInfo -> info.packageName }
                .findFirst()
                .orElse(null)
            if (OVERLAY_TARGET_PACKAGE == packageName && TextUtils.isEmpty(currentPackageName)
                || TextUtils.equals(packageName, currentPackageName)
            ) return true
            object : AsyncTask<Void, Void, Boolean?>() {
                override fun doInBackground(vararg params: Void): Boolean? {
                    return try {
                        if (OVERLAY_TARGET_PACKAGE == packageName) {
                            overlayManager.setEnabled(currentPackageName, false, USER_SYSTEM)
                        } else {
                            overlayManager.setEnabledExclusiveInCategory(packageName, USER_SYSTEM)
                        }
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: RemoteException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    }
                }

                override fun onPostExecute(success: Boolean?) {
                    if (success!!) updateSelection(shape, holder)
                    else {
                        Toast.makeText(
                            holder.shapeLayout.context,
                            "Could not enable font ${shape.label}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.execute()
            return true
        }

        private fun updateSelection(shape: Shape, holder: ShapeAdapter.ViewHolder) {
            val accentColor: Int = ResourceHelper.getAccent(holder.shapeLayout.context)
            if (shape.selected) {
                holder.shapeLayout.setBackgroundColor(accentColor)
                holder.shapeLayout.invalidate()
            } else {
                holder.shapeLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.shapeLayout.context,
                        android.R.color.transparent
                    )
                )
                holder.shapeLayout.invalidate()
            }
        }

        fun getIconMask(packageName: String?): String? {
            val resources: Resources =
                if (OVERLAY_TARGET_PACKAGE == packageName) Resources.getSystem() else packageManager.getResourcesForApplication(
                    packageName
                )
            return resources.getString(
                resources.getIdentifier(
                    Constants.CONFIG_ICON_MASK,
                    "string",
                    packageName
                )
            )
        }

        fun createShapeDrawable(context: Context, path: Path): ShapeDrawable {
            val shape = PathShape(path, Constants.PATH_SIZE, Constants.PATH_SIZE)
            val shapeDrawable = ShapeDrawable(shape)
            val size = context.resources.getDimensionPixelSize(
                R.dimen.component_shape_thumb_size
            )
            shapeDrawable.intrinsicHeight = size
            shapeDrawable.intrinsicWidth = size
            return shapeDrawable
        }

        fun getShapes(context: Context): ArrayList<Shape> {
            val fontPacks = ArrayList<Shape>()

            val selectedPkg = OVERLAY_TARGET_PACKAGE
            val selectedLabel = context.getString(R.string.device_default)

            fontPacks.add(Shape(getShapePath(selectedPkg)!!, selectedPkg, selectedLabel))

            for (overlayInfo in getOverlayInfos()) {
                try {
                    val label = packageManager.getApplicationInfo(overlayInfo.packageName, 0)
                        .loadLabel(packageManager).toString()
                    fontPacks.add(
                        Shape(
                            getShapePath(overlayInfo.packageName)!!,
                            overlayInfo.packageName,
                            label
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    fontPacks.add(
                        Shape(
                            getShapePath(overlayInfo.packageName)!!,
                            overlayInfo.packageName,
                            overlayInfo.packageName
                        )
                    )
                }
                if (overlayInfo.isEnabled) {
                    fontPacks[fontPacks.size - 1].selected = true
                }
            }
            /**
             * Manually check for selected shapes
             * if there's no shape selected
             * then the default one should be selected
             */
            var checker = 0
            for (i in fontPacks.indices) {
                if (fontPacks[i].selected) checker = 1
            }
            if (checker == 0) fontPacks[0].selected = true
            return fontPacks
        }

        private fun getShapePath(overlayPackage: String): Path? {
            val resources: Resources =
                if (OVERLAY_TARGET_PACKAGE == overlayPackage) Resources.getSystem() else packageManager
                    .getResourcesForApplication(overlayPackage)
            val shape: String = resources.getString(
                resources.getIdentifier(
                    Constants.CONFIG_ICON_MASK, "string",
                    overlayPackage
                )
            )
            return if (!TextUtils.isEmpty(shape)) {
                PathParser.createPathFromPathData(shape)
            } else null
        }

    }

    inner class FontPacks {

        @SuppressLint("StaticFieldLeak")
        fun setOverlay(
            packageName: String,
            fontPack: FontPack,
            holder: FontPackAdapter.ViewHolder
        ): Boolean {
            val currentPackageName = getOverlayInfos().stream()
                .filter { info: OverlayInfo -> info.isEnabled }
                .map { info: OverlayInfo -> info.packageName }
                .findFirst()
                .orElse(null)
            if (OVERLAY_TARGET_PACKAGE == packageName && TextUtils.isEmpty(currentPackageName)
                || TextUtils.equals(packageName, currentPackageName)
            ) return true
            object : AsyncTask<Void, Void, Boolean?>() {
                override fun doInBackground(vararg params: Void): Boolean? {
                    return try {
                        if (OVERLAY_TARGET_PACKAGE == packageName) {
                            overlayManager.setEnabled(currentPackageName, false, USER_SYSTEM)
                        } else {
                            overlayManager.setEnabledExclusiveInCategory(packageName, USER_SYSTEM)
                        }
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: RemoteException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    }
                }

                override fun onPostExecute(success: Boolean?) {
                    if (success!!) updateSelection(fontPack, holder)
                    else {
                        Toast.makeText(
                            holder.fontLayout.context,
                            "Could not enable font ${fontPack.label}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.execute()
            return true
        }

        fun getFontPacks(context: Context): ArrayList<FontPack> {
            val fontPacks = ArrayList<FontPack>()

            val selectedPkg = OVERLAY_TARGET_PACKAGE
            val selectedLabel = context.getString(R.string.device_default_font)

            val headlineFont = Typeface.create(
                getFontFamily(selectedPkg, Constants.CONFIG_HEADLINE_FONT_FAMILY), Typeface.NORMAL
            )
            val bodyFont = Typeface.create(
                getFontFamily(selectedPkg, Constants.CONFIG_BODY_FONT_FAMILY), Typeface.NORMAL
            )
            fontPacks.add(FontPack(headlineFont, bodyFont, selectedPkg, selectedLabel))

            for (overlayInfo in getOverlayInfos()) {
                try {
                    var headlineFont: Typeface? = null
                    val headLineFamily = getFontFamily(
                        overlayInfo.packageName,
                        Constants.CONFIG_HEADLINE_FONT_FAMILY
                    )
                    if (headLineFamily != "null") {
                        headlineFont = Typeface.create(headlineFont, Typeface.NORMAL)
                    }
                    var bodyFont: Typeface? = null
                    val bodyFamily =
                        getFontFamily(overlayInfo.packageName, Constants.CONFIG_BODY_FONT_FAMILY)
                    if (bodyFamily != "null") {
                        bodyFont = Typeface.create(bodyFont, Typeface.NORMAL)
                    }
                    val label = packageManager.getApplicationInfo(overlayInfo.packageName, 0)
                        .loadLabel(packageManager).toString().replace(" /", "\n")
                    fontPacks.add(
                        FontPack(
                            headlineFont,
                            bodyFont,
                            overlayInfo.packageName,
                            label
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    fontPacks.add(
                        FontPack(
                            headlineFont,
                            bodyFont,
                            overlayInfo.packageName,
                            overlayInfo.packageName
                        )
                    )
                }
                if (overlayInfo.isEnabled) {
                    fontPacks[fontPacks.size - 1].selected = true
                }
            }
            /**
             * Manually check for selected fontPacks
             * if there's no fontPack selected
             * then the default one should be selected
             */
            var checker = 0
            for (i in fontPacks.indices) {
                if (fontPacks[i].selected) checker = 1
            }
            if (checker == 0) fontPacks[0].selected = true
            return fontPacks
        }

        private fun getFontFamily(
            overlayPackage: String,
            configName: String
        ): String? {
            val resources: Resources =
                if (OVERLAY_TARGET_PACKAGE == overlayPackage) Resources.getSystem() else packageManager
                    .getResourcesForApplication(overlayPackage)
            return try {
                resources.getString(resources.getIdentifier(configName, "string", overlayPackage))
            } catch (e: Resources.NotFoundException) {
                "null"
            }
        }

        private fun updateSelection(fontPack: FontPack, holder: FontPackAdapter.ViewHolder) {
            val typedValue = TypedValue()
            val contextThemeWrapper = ContextThemeWrapper(
                holder.fontLayout.context,
                android.R.style.Theme_DeviceDefault
            )
            contextThemeWrapper.theme.resolveAttribute(
                android.R.attr.colorAccent,
                typedValue, true
            )
            val accentManager =
                FeatureManager(holder.fontLayout.context.contentResolver).AccentManager()
            val accentColor: Int =
                if (accentManager.get() == "-1" || accentManager.get() == "") typedValue.data else Color.parseColor(
                    "#" + accentManager.get()
                )
            if (fontPack.selected) {
                holder.fontLayout.setBackgroundColor(accentColor)
                holder.fontLayout.invalidate()
            } else {
                holder.fontLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.fontLayout.context,
                        android.R.color.transparent
                    )
                )
                holder.fontLayout.invalidate()
            }
        }

    }

    inner class IconPacks {

        @SuppressLint("StaticFieldLeak")
        fun setOverlay(
            packageName: String,
            iconPack: IconPack,
            holder: IconPackAdapter.ViewHolder
        ): Boolean {
            val currentPackageName = getOverlayInfos().stream()
                .filter { info: OverlayInfo -> info.isEnabled }
                .map { info: OverlayInfo -> info.packageName }
                .findFirst()
                .orElse(null)
            if (OVERLAY_TARGET_PACKAGE == packageName && TextUtils.isEmpty(currentPackageName)
                || TextUtils.equals(packageName, currentPackageName)
            ) return true
            object : AsyncTask<Void, Void, Boolean?>() {
                override fun doInBackground(vararg params: Void): Boolean? {
                    return try {
                        if (OVERLAY_TARGET_PACKAGE == packageName) {
                            overlayManager.setEnabled(currentPackageName, false, USER_SYSTEM)
                        } else {
                            overlayManager.setEnabledExclusiveInCategory(packageName, USER_SYSTEM)
                        }
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: RemoteException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    }
                }

                override fun onPostExecute(success: Boolean?) {
                    if (success!!) updateSelection(iconPack, holder)
                    else {
                        Toast.makeText(
                            holder.iconPackLayout.context,
                            "Could not enable icon pack ${iconPack.label}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.execute()
            return true
        }

        private fun updateSelection(iconPack: IconPack, holder: IconPackAdapter.ViewHolder) {
            val accentColor: Int = ResourceHelper.getAccent(holder.iconPackLayout.context)
            if (iconPack.selected) {
                holder.iconPackLayout.setBackgroundColor(accentColor)
                holder.iconPackLayout.invalidate()
            } else {
                holder.iconPackLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.iconPackLayout.context,
                        android.R.color.transparent
                    )
                )
                holder.iconPackLayout.invalidate()
            }
        }

        fun getIconPacks(context: Context): ArrayList<IconPack> {
            val iconPacks = ArrayList<IconPack>()

            val selectedPkg = OVERLAY_TARGET_PACKAGE
            val selectedLabel = context.getString(R.string.device_default)
            iconPacks.add(IconPack(Constants.ICONS_FOR_PREVIEW, selectedPkg, selectedLabel))

            for (overlayInfo in getOverlayInfos()) {
                try {
                    val label = packageManager.getApplicationInfo(overlayInfo.packageName, 0)
                        .loadLabel(packageManager).toString()
                    iconPacks.add(
                        IconPack(
                            Constants.ICONS_FOR_PREVIEW,
                            overlayInfo.packageName,
                            label
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    iconPacks.add(
                        IconPack(
                            Constants.ICONS_FOR_PREVIEW,
                            overlayInfo.packageName,
                            overlayInfo.packageName
                        )
                    )
                }
                if (overlayInfo.isEnabled) {
                    iconPacks[iconPacks.size - 1].selected = true
                }
            }
            /**
             * Manually check for selected iconPacks
             * if there's no iconPack selected
             * then the default one should be selected
             */
            var checker = 0
            for (i in iconPacks.indices) {
                if (iconPacks[i].selected) checker = 1
            }
            if (checker == 0) iconPacks[0].selected = true
            return iconPacks
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun loadIconPreviewDrawable(
            drawableName: String,
            packageName: String
        ): Drawable {
            val resources: Resources =
                if (OVERLAY_TARGET_PACKAGE == packageName) Resources.getSystem() else packageManager
                    .getResourcesForApplication(packageName)
            return resources.getDrawable(
                resources.getIdentifier(drawableName, "drawable", packageName), null
            )
        }

    }

    inner class Analog(private var preference: ListPreference) {

        @SuppressLint("StaticFieldLeak")
        fun setOverlay(packageName: String): Boolean {
            val currentPackageName = getOverlayInfos().stream()
                .filter { info: OverlayInfo -> info.isEnabled }
                .map { info: OverlayInfo -> info.packageName }
                .findFirst()
                .orElse(null)
            if (PACKAGE_DEVICE_DEFAULT == packageName && TextUtils.isEmpty(currentPackageName)
                || TextUtils.equals(packageName, currentPackageName)
            ) return true
            object : AsyncTask<Void, Void, Boolean?>() {
                override fun doInBackground(vararg params: Void): Boolean? {
                    return try {
                        if (PACKAGE_DEVICE_DEFAULT == packageName) {
                            overlayManager.setEnabled(currentPackageName, false, USER_SYSTEM)
                        } else {
                            overlayManager.setEnabledExclusiveInCategory(packageName, USER_SYSTEM)
                        }
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    } catch (e: RemoteException) {
                        Log.w(TAG, "Error enabling overlay.", e)
                        false
                    }
                }

                override fun onPostExecute(success: Boolean?) {
                    updatePreferenceOverlays()
                    if (!success!!) {
                        Toast.makeText(
                            preference.context,
                            "Could not enable overlay",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.execute()
            return true
        }

        fun updatePreferenceOverlays() {
            val pkgs: MutableList<String> = ArrayList()
            val labels: MutableList<String> = ArrayList()

            var selectedPkg = PACKAGE_DEVICE_DEFAULT
            var selectedLabel = preference.context.getString(R.string.device_default)

            pkgs.add(selectedPkg)
            labels.add(selectedLabel)

            for (overlayInfo in getOverlayInfos()) {
                pkgs.add(overlayInfo.packageName)
                try {
                    labels.add(
                        packageManager.getApplicationInfo(overlayInfo.packageName, 0)
                            .loadLabel(packageManager).toString()
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    labels.add(overlayInfo.packageName)
                }
                if (overlayInfo.isEnabled) {
                    selectedPkg = pkgs[pkgs.size - 1]
                    selectedLabel = labels[labels.size - 1]
                }
            }

            preference.entries = labels.toTypedArray()
            preference.entryValues = pkgs.toTypedArray()
            preference.value = selectedPkg
            preference.summary = selectedLabel
        }

    }

    fun getOverlayInfos(): ArrayList<OverlayInfo> {
        val filteredInfos: ArrayList<OverlayInfo> = ArrayList()
        try {
            val overlayInfos: ArrayList<OverlayInfo> =
                overlayManager.getOverlayInfosForTarget(
                    OVERLAY_TARGET_PACKAGE,
                    USER_SYSTEM
                ) as ArrayList<OverlayInfo>
            for (overlayInfo in overlayInfos) {
                //Log.d(TAG, "Overlay Categories : ${overlayInfo.category}")
                if (mCategory!! == overlayInfo.category) {
                    filteredInfos.add(overlayInfo)
                }
            }
        } catch (re: RemoteException) {
            throw re.rethrowFromSystemServer()
        }
        filteredInfos.sortWith(OVERLAY_INFO_COMPARATOR)
        return filteredInfos
    }

    fun isAvailable(): Boolean = getOverlayInfos().isNotEmpty()
}