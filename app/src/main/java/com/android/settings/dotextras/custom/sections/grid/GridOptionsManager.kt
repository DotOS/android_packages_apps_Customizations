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
package com.android.settings.dotextras.custom.sections.grid

import android.os.AsyncTask
import android.os.Bundle

/**
 * [CustomizationManager] for interfacing with the launcher to handle [GridOption]s.
 */
typealias onHandleCallback = ((success: Boolean) -> Unit)?
typealias OptionsFetchedListener = ((options: ArrayList<GridOption>?) -> Unit)?

class GridOptionsManager(private val mProvider: LauncherGridOptionsProvider) {
    val isAvailable: Boolean
        get() = mProvider.areGridsAvailable()

    fun apply(option: GridOption, callback: onHandleCallback) {
        val updated = mProvider.applyGrid(option.name)
        callback?.invoke(updated == 1)
    }

    fun fetchOptions(callback: OptionsFetchedListener, reload: Boolean) {
        FetchTask(mProvider, callback, reload).execute()
    }

    /** Call through content provider API to render preview  */
    fun renderPreview(bundle: Bundle, gridName: String?): Bundle {
        return mProvider.renderPreview(gridName, bundle)
    }

    private class FetchTask(
        provider: LauncherGridOptionsProvider,
        callback: OptionsFetchedListener, reload: Boolean,
    ) : AsyncTask<Void?, Void?, ArrayList<GridOption>?>() {
        private val mProvider: LauncherGridOptionsProvider = provider
        private val mCallback: OptionsFetchedListener = callback
        private val mReload: Boolean = reload
        override fun doInBackground(params: Array<Void?>): ArrayList<GridOption>? {
            return mProvider.fetch(mReload)
        }

        override fun onPostExecute(gridOptions: ArrayList<GridOption>?) {
            if (mCallback != null) {
                if (gridOptions != null && gridOptions.isNotEmpty()) {
                    mCallback.invoke(gridOptions)
                } else {
                    mCallback.invoke(null)
                }
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            mCallback?.invoke(null)
        }

    }
}