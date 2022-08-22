/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.ResourceConstants
import com.dot.customizations.model.ResourceConstants.OVERLAY_CATEGORY_COLOR
import com.dot.customizations.model.ResourceConstants.OVERLAY_CATEGORY_SYSTEM_PALETTE
import com.dot.customizations.model.theme.OverlayManagerCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors

/** The Color manager to manage Color bundle related operations.  */
class ColorCustomizationManager @VisibleForTesting internal constructor(
    provider: ColorOptionsProvider, contentResolver: ContentResolver,
    overlayManagerCompat: OverlayManagerCompat
) : CustomizationManager<ColorOption> {
    private val mProvider: ColorOptionsProvider
    private val mOverlayManagerCompat: OverlayManagerCompat
    private val mContentResolver: ContentResolver
    private val mObserver: ContentObserver
    private var mCurrentOverlays: Map<String, String>? = null

    @ColorOptionsProvider.ColorSource
    private var mCurrentSource: String? = null
    private var mCurrentStyle: String? = null
    private var mHomeWallpaperColors: WallpaperColors? = null
    private var mLockWallpaperColors: WallpaperColors? = null

    init {
        mProvider = provider
        mContentResolver = contentResolver
        mObserver = object : ContentObserver( /* handler= */null) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                // Resets current overlays when system's theme setting is changed.
                if (TextUtils.equals(uri!!.lastPathSegment, ResourceConstants.THEME_SETTING)) {
                    Log.i(
                        TAG, "Resetting " + mCurrentOverlays + ", " + mCurrentStyle + ", "
                                + mCurrentSource + " to null"
                    )
                    mCurrentOverlays = null
                    mCurrentStyle = null
                    mCurrentSource = null
                }
            }
        }
        mContentResolver.registerContentObserver(
            Settings.Secure.CONTENT_URI,  /* notifyForDescendants= */true, mObserver
        )
        mOverlayManagerCompat = overlayManagerCompat
    }

    override fun isAvailable(): Boolean = mOverlayManagerCompat.isAvailable && mProvider.isAvailable

    override fun apply(theme: ColorOption?, callback: CustomizationManager.Callback?) {
        applyOverlays(theme!!, callback!!)
    }

    private fun applyOverlays(colorOption: ColorOption, callback: CustomizationManager.Callback) {
        sExecutorService.submit {
            var currentStoredOverlays = storedOverlays
            if (TextUtils.isEmpty(currentStoredOverlays)) {
                currentStoredOverlays = "{}"
            }
            var overlaysJson: JSONObject? = null
            try {
                overlaysJson = JSONObject(currentStoredOverlays)
                val colorJson: JSONObject = colorOption.getJsonPackages(true)
                for (setting in COLOR_OVERLAY_SETTINGS) {
                    overlaysJson.remove(setting)
                }
                val it: Iterator<String> = colorJson.keys()
                while (it.hasNext()) {
                    val key = it.next()
                    overlaysJson.put(key, colorJson.get(key))
                }
                overlaysJson.put(ColorOptionsProvider.OVERLAY_COLOR_SOURCE, colorOption.source)
                overlaysJson.put(
                    ColorOptionsProvider.OVERLAY_COLOR_INDEX,
                    colorOption.index.toString()
                )
                overlaysJson.put(
                    ColorOptionsProvider.OVERLAY_THEME_STYLE,
                    java.lang.String.valueOf(colorOption.style.toString())
                )

                // OVERLAY_COLOR_BOTH is only for wallpaper color case, not preset.
                if (ColorOptionsProvider.COLOR_SOURCE_PRESET != colorOption.source) {
                    val isForBoth =
                        mLockWallpaperColors == null || mLockWallpaperColors == mHomeWallpaperColors
                    overlaysJson.put(
                        ColorOptionsProvider.OVERLAY_COLOR_BOTH,
                        if (isForBoth) "1" else "0"
                    )
                } else {
                    overlaysJson.remove(ColorOptionsProvider.OVERLAY_COLOR_BOTH)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val allApplied = overlaysJson != null && Settings.Secure.putString(
                mContentResolver, ResourceConstants.THEME_SETTING, overlaysJson.toString()
            )
            Handler(Looper.getMainLooper()).post {
                if (allApplied) {
                    callback.onSuccess()
                } else {
                    callback.onError(null)
                }
            }
        }
    }

    override fun fetchOptions(callback: CustomizationManager.OptionsFetchedListener<ColorOption?>, reload: Boolean) {
        var lockWallpaperColors = mLockWallpaperColors
        if (lockWallpaperColors != null && mLockWallpaperColors == mHomeWallpaperColors) {
            lockWallpaperColors = null
        }
        mProvider.fetch(callback, reload, mHomeWallpaperColors, lockWallpaperColors)
    }

    /**
     * Sets the current wallpaper colors to extract seeds from
     */
    fun setWallpaperColors(
        homeColors: WallpaperColors?,
        lockColors: WallpaperColors?
    ) {
        mHomeWallpaperColors = homeColors
        mLockWallpaperColors = lockColors
    }

    /**
     * Gets current overlays mapping
     * @return the [Map] of overlays
     */
    val currentOverlays: Map<String, String>?
        get() {
            if (mCurrentOverlays == null) {
                parseSettings(storedOverlays)
            }
            return mCurrentOverlays
        }

    /**
     * @return The source of the currently applied color. One of
     * [ColorOptionsProvider.COLOR_SOURCE_HOME],[ColorOptionsProvider.COLOR_SOURCE_LOCK]
     * or [ColorOptionsProvider.COLOR_SOURCE_PRESET].
     */
    @get:ColorOptionsProvider.ColorSource
    val currentColorSource: String?
        get() {
            if (mCurrentSource == null) {
                parseSettings(storedOverlays)
            }
            return mCurrentSource
        }

    /**
     * @return The style of the currently applied color. One of enum values in
     * [com.android.systemui.monet.Style].
     */
    val currentStyle: String?
        get() {
            if (mCurrentStyle == null) {
                parseSettings(storedOverlays)
            }
            return mCurrentStyle
        }
    val storedOverlays: String
        get() = Settings.Secure.getString(mContentResolver, ResourceConstants.THEME_SETTING)

    @VisibleForTesting
    fun parseSettings(serializedJson: String?) {
        val allSettings = parseColorSettings(serializedJson)
        mCurrentSource = allSettings.remove(ColorOptionsProvider.OVERLAY_COLOR_SOURCE)
        mCurrentStyle = allSettings.remove(ColorOptionsProvider.OVERLAY_THEME_STYLE)
        mCurrentOverlays = allSettings
    }

    private fun parseColorSettings(serializedJsonSettings: String?): MutableMap<String, String> {
        val overlayPackages: MutableMap<String, String> = HashMap()
        if (serializedJsonSettings != null) {
            try {
                val jsonPackages = JSONObject(serializedJsonSettings)
                val names: JSONArray? = jsonPackages.names()
                if (names != null) {
                    for (i in 0 until names.length()) {
                        val category: String = names.getString(i)
                        if (COLOR_OVERLAY_SETTINGS.contains(category)) {
                            try {
                                overlayPackages[category] = jsonPackages.getString(category)
                            } catch (e: JSONException) {
                                Log.e(TAG, "parseColorOverlays: " + e.localizedMessage, e)
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "parseColorOverlays: " + e.localizedMessage, e)
            }
        }
        return overlayPackages
    }

    companion object {
        private const val TAG = "ColorCustomizationManager"
        private val sExecutorService = Executors.newSingleThreadExecutor()
        private val COLOR_OVERLAY_SETTINGS: MutableSet<String> = HashSet()

        init {
            COLOR_OVERLAY_SETTINGS.add(OVERLAY_CATEGORY_SYSTEM_PALETTE)
            COLOR_OVERLAY_SETTINGS.add(OVERLAY_CATEGORY_COLOR)
            COLOR_OVERLAY_SETTINGS.add(ColorOptionsProvider.OVERLAY_COLOR_SOURCE)
            COLOR_OVERLAY_SETTINGS.add(ColorOptionsProvider.OVERLAY_THEME_STYLE)
        }

        private var sColorCustomizationManager: ColorCustomizationManager? = null

        /** Returns the [ColorCustomizationManager] instance.  */
        fun getInstance(
            context: Context,
            overlayManagerCompat: OverlayManagerCompat
        ): ColorCustomizationManager? {
            if (sColorCustomizationManager == null) {
                val appContext = context.applicationContext
                sColorCustomizationManager = ColorCustomizationManager(
                    ColorProvider(
                        appContext,
                        appContext.getString(R.string.themes_stub_package)
                    ),
                    appContext.contentResolver, overlayManagerCompat
                )
            }
            return sColorCustomizationManager
        }
    }
}