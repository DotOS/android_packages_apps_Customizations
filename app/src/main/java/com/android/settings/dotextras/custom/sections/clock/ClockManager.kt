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
package com.android.settings.dotextras.custom.sections.clock

import android.content.ContentResolver
import android.text.TextUtils
import android.util.Log
import com.android.settings.dotextras.system.FeatureManager
import org.json.JSONException
import org.json.JSONObject

typealias onHandleCallback = ((success: Boolean) -> Unit)?

class ClockManager(mContentResolver: ContentResolver, provider: ClockProvider?) :
    BaseClockManager(provider) {

    private val featureManager = FeatureManager(mContentResolver)

    var callback: onHandleCallback = null

    override fun handleApply(option: Clockface?, callback: onHandleCallback) {
        val stored: Boolean = try {
            val json = JSONObject()
            json.put(CLOCK_FIELD, option!!.id)
            json.put(TIMESTAMP_FIELD, System.currentTimeMillis())
            Log.e("ClockManager", json.toString())
            featureManager.Secure()
                .setStringBool(featureManager.Secure().CLOCK_FACE_SETTING, json.toString())
        } catch (ex: JSONException) {
            false
        }
        if (stored) {
            this.callback?.invoke(true)
        } else {
            this.callback?.invoke(false)
        }
    }

    override fun lookUpCurrentClock(): String {
        val value = featureManager.Secure().getString(featureManager.Secure().CLOCK_FACE_SETTING)
        return if (TextUtils.isEmpty(value)) ({
            value
        }).toString() else try {
            val json = JSONObject(value!!)
            json.getString(CLOCK_FIELD)
        } catch (ex: JSONException) {
            value
        }!!
    }

    companion object {
        private const val CLOCK_FIELD = "clock"
        private const val TIMESTAMP_FIELD = "_applied_timestamp"
    }
}