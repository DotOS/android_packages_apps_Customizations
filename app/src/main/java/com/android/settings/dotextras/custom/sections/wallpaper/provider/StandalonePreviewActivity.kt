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
package com.android.settings.dotextras.custom.sections.wallpaper.provider

import android.Manifest
import android.animation.LayoutTransition
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.WallpaperSection
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.google.android.material.appbar.AppBarLayout
import java.io.File

class StandalonePreviewActivity : AppCompatActivity() {

    private var standalone = false
    lateinit var toolbar: Toolbar
    lateinit var appBar: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature)
        toolbar = findViewById(R.id.appTitle)
        appBar = findViewById(R.id.dashboardAppBar)
        toolbar.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        val cropAndSetWallpaperIntent = intent
        val imageUri = cropAndSetWallpaperIntent.data
        if (imageUri == null) {
            Log.e(TAG, "No URI passed in intent; moving to StandaloneSection")
            standalone = true
        } else {
            val isReadPermissionGrantedForImageUri = isReadPermissionGrantedForImageUri(imageUri)
            if (!isReadPermissionGrantedForImageUri && !isReadExternalStoragePermissionGrantedForApp) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
        val lp2 = toolbar.layoutParams as AppBarLayout.LayoutParams
        lp2.scrollFlags = 0
        setTitle(getString(R.string.section_wallpaper_title))
    }

    fun setTitle(title: String?) {
        toolbar.title = title ?: getString(R.string.app_name)
    }


    override fun onDestroy() {
        val dir = applicationContext.cacheDir
        val children: Array<String>? = dir.list()
        if (children != null) {
            for (child in children) {
                if (child.endsWith(".jpeg") || child.endsWith(".jpg")) File(dir, child).delete()
            }
        }
        super.onDestroy()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.featureCoordinator)
        if (fragment == null) {
            loadPreviewFragment()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            val isGranted =
                permissions.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                finish()
            }
            loadPreviewFragment()
        }
    }

    private fun loadPreviewFragment() {
        if (!standalone) {
            val uri: Uri? = intent.data
            if (uri != null) {
                val wallpaperBase = Wallpaper()
                wallpaperBase.uri = uri.toString()
                supportFragmentManager.beginTransaction()
                    .add(R.id.featureCoordinator, ApplyStandaloneFragment(wallpaperBase))
                    .commit()
            }
        } else supportFragmentManager.beginTransaction()
            .add(R.id.featureCoordinator, WallpaperSection(standalone))
            .commit()
    }

    private val isReadExternalStoragePermissionGrantedForApp: Boolean
        get() = packageManager.checkPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            packageName
        ) == PackageManager.PERMISSION_GRANTED

    private fun isReadPermissionGrantedForImageUri(imageUri: Uri): Boolean {
        return checkUriPermission(
            imageUri,
            Binder.getCallingPid(),
            Binder.getCallingUid(),
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "StandalonePreview"
        private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
    }
}