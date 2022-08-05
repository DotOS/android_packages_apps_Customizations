/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.dot.customizations.model.iconpack

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.CustomizationManager.OptionsFetchedListener
import com.dot.customizations.model.ResourceConstants
import com.dot.customizations.model.theme.OverlayManagerCompat
import org.json.JSONException
import org.json.JSONObject

class IconPackManager internal constructor(
    private val mContext: Context,
    val overlayManager: OverlayManagerCompat,
    private val mProvider: IconPackOptionProvider
) : CustomizationManager<IconPackOption> {
    private var mActiveOption: IconPackOption? = null
    override fun isAvailable(): Boolean {
        return overlayManager.isAvailable
    }

    override fun apply(option: IconPackOption, callback: CustomizationManager.Callback?) {
        if (!persistOverlay(option)) {
            val failed = Toast.makeText(
                mContext,
                "Failed to apply icon pack, reboot to try again.",
                Toast.LENGTH_SHORT
            )
            failed.show()
            callback?.onError(null)
            return
        }
        if (option.title == "Default") {
            if (mActiveOption!!.title == "Default") return
            mActiveOption!!.overlayPackages.forEach { (category: String?, overlay: String?) ->
                overlayManager.disableOverlay(
                    overlay,
                    UserHandle.myUserId()
                )
            }
        } else {
            option.overlayPackages.forEach { (category: String?, overlay: String?) ->
                overlayManager.setEnabledExclusiveInCategory(
                    overlay,
                    UserHandle.myUserId()
                )
            }
        }
        callback?.onSuccess()
        mActiveOption = option
    }

    override fun fetchOptions(callback: OptionsFetchedListener<IconPackOption>, reload: Boolean) {
        val options = mProvider.options
        for (option in options) {
            if (option.isActive(this)) {
                mActiveOption = option
                break
            }
        }
        callback.onOptionsLoaded(options)
    }

    private fun persistOverlay(toPersist: IconPackOption): Boolean {
        val value: String = Settings.Secure.getStringForUser(
            mContext.contentResolver,
            Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES, UserHandle.USER_CURRENT
        )
        val json: JSONObject = try {
            JSONObject(value)
        } catch (e: JSONException) {
            Log.e(TAG, "Error adding new settings value: ${e.message}".trimIndent())
            return false
        }
        // removing all currently enabled overlays from the json
        for (categoryName in mCurrentCategories) {
            json.remove(categoryName)
        }
        // adding the new ones
        for (categoryName in mCurrentCategories) {
            try {
                json.put(categoryName, toPersist.overlayPackages[categoryName])
            } catch (e: JSONException) {
                Log.e(TAG, "Error adding new settings value: ${e.message}".trimIndent())
                return false
            }
        }
        // updating the setting
        Settings.Secure.putStringForUser(
            mContext.contentResolver,
            Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
            json.toString(), UserHandle.USER_CURRENT
        )
        return true
    }

    companion object {
        private var sIconPackOptionManager: IconPackManager? = null
        private const val TAG = "IconPackManager"
        private const val KEY_STATE_CURRENT_SELECTION = "IconPackManager.currentSelection"
        private val mCurrentCategories = arrayOf(
            ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID,
            ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS,
            ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI
        )

        fun getInstance(context: Context, overlayManager: OverlayManagerCompat): IconPackManager {
            if (sIconPackOptionManager == null) {
                val applicationContext = context.applicationContext
                sIconPackOptionManager = IconPackManager(
                    context,
                    overlayManager,
                    IconPackOptionProvider(applicationContext, overlayManager)
                )
            }
            return sIconPackOptionManager!!
        }
    }
}