/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.settings.dotextras.custom.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils

/** Util class for wallpaper preview.  */
class PreviewUtils(private val mContext: Context, authorityMetadataKey: String?) {
    private var mProviderAuthority: String? = null
    private val mProviderInfo: ProviderInfo?

    /** Render preview under the current grid option.  */
    fun renderPreview(bundle: Bundle?): Bundle {
        return mContext.contentResolver.call(getUri(PREVIEW), METHOD_GET_PREVIEW, null,
            bundle)
    }

    /** Easy way to generate a Uri with the provider info from this class.  */
    fun getUri(path: String?): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(mProviderInfo!!.authority)
            .appendPath(path)
            .build()
    }

    /** Return whether preview is supported.  */
    fun supportsPreview(): Boolean {
        return mProviderInfo != null
    }

    companion object {
        private const val PREVIEW = "preview"
        private const val METHOD_GET_PREVIEW = "get_preview"
    }

    init {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val info = mContext.packageManager.resolveActivity(homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.GET_META_DATA)
        mProviderAuthority =
            if (info?.activityInfo != null && info.activityInfo.metaData != null) {
                info.activityInfo.metaData.getString(authorityMetadataKey)
            } else {
                null
            }
        // TODO: check permissions if needed
        mProviderInfo =
            if (TextUtils.isEmpty(mProviderAuthority)) null else mContext.packageManager.resolveContentProvider(
                mProviderAuthority,
                0)
    }
}