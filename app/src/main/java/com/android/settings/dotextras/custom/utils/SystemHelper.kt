/*
 * Copyright (C) 2020 The dotOS Project
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

import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.provider.OpenableColumns

fun Context.internetAvailable(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
    return networkInfo?.isConnected == true
}

fun Context.getFileName(uri: Uri): String {
    var result = ""
    if (uri.scheme == "content") {
        val cursor: Cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    if (result == "") {
        result = uri.path
        val cut = result.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun String.removeExtension(): String {
    val separator = System.getProperty("file.separator")
    val filename: String
    val lastSeparatorIndex = lastIndexOf(separator)
    filename = if (lastSeparatorIndex == -1) {
        this
    } else {
        this.substring(lastSeparatorIndex + 1)
    }
    val extensionIndex = filename.lastIndexOf(".")
    return if (extensionIndex == -1) filename else filename.substring(0, extensionIndex)
}