/*
 * Copyright (C) 2021 The dotOS Project
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
package com.android.settings.dotextras.custom.sections.wallpaper

import android.Manifest
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.colors.MonetColors
import com.android.settings.dotextras.custom.sections.wallpaper.colors.WallpaperColorFragment
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.CropImageView
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.ApplyForDialogFragment
import com.dot.ui.utils.ResourceHelper
import com.android.settings.dotextras.custom.views.WallpaperPreview
import com.android.settings.dotextras.databinding.ActivityWallpaperApplyBinding
import com.dot.ui.utils.ObjectToolsAnimator
import com.dot.ui.utils.toBitmap
import com.dot.ui.utils.uriToDrawable
import com.google.android.material.tabs.TabLayout
import java.io.File

class StandalonePreviewActivity : AppCompatActivity(),
    TabLayout.OnTabSelectedListener {

    private lateinit var binding: ActivityWallpaperApplyBinding
    private lateinit var wallpaper: Wallpaper
    private lateinit var wallpaperManager: WallpaperManager
    private lateinit var targetWall: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            val imageUri = intent.data
            if (imageUri != null) {
                val isReadPermissionGrantedForImageUri = isReadPermissionGrantedForImageUri(imageUri)
                if (!isReadPermissionGrantedForImageUri && !isReadExternalStoragePermissionGrantedForApp) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                    )
                } else {
                    loadUI()
                }
            }
        }
    }

    private fun loadUI() {
        with(binding) {
            val context = this@StandalonePreviewActivity
            wallpaper = Wallpaper()
            wallpaper.uri = intent.data.toString()
            val monetColors = MonetColors(context, wallpaper)
            with(monetColors) {
                applyContainer.backgroundTintList = ColorStateList.valueOf(backgroundColor)
                apApply.backgroundTintList = ColorStateList.valueOf(backgroundSecondaryColor)
                apApply.rippleColor = ColorStateList.valueOf(backgroundSecondaryColor)
                apApply.setTextColor(accentColor)
                wallTabs.setSelectedTabIndicatorColor(accentColor)
                wallTabs.backgroundTintList = ColorStateList.valueOf(backgroundSecondaryColor)
                wallTabsToolbar.backgroundTintList = ColorStateList.valueOf(backgroundColor)
                previewLockscreen.setCardBackgroundColor(backgroundSecondaryColor)
                previewLauncher.setCardBackgroundColor(backgroundSecondaryColor)
                context.window.statusBarColor = backgroundColor
                context.window.navigationBarColor = backgroundColor
            }
            wallpaperManager = WallpaperManager.getInstance(context)
            targetWall = uriToDrawable(Uri.parse(wallpaper.uri!!))
            val uriB = Uri.parse(wallpaper.uri!!)
            apCrop.setOnClickListener {
                CropImage.activity(uriB)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAllowFlipping(true)
                    .setAllowCounterRotation(true)
                    .setAllowRotation(true)
                    .setAspectRatio(ResourceHelper.getAspectRatio(context).a2, ResourceHelper.getAspectRatio(context).a1)
                    .setOutputUri(Uri.EMPTY)
                    .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                    .start(context)
            }
            previewImageLockscreen.load(Uri.parse(wallpaper.uri!!) ) {
                crossfade(true)
            }
            previewImageLauncher.load(Uri.parse(wallpaper.uri!!)) {
                crossfade(true)
            }
            apApply.setOnClickListener {
                if (wallpaper.uri != null)
                    ApplyForDialogFragment.newInstance(wallpaper)
                        .show(supportFragmentManager, "applyWallpaper")
            }
            apDownload.isVisible = targetWall.toBitmap() != null
            targetWall.toBitmap()?.let { bitmap ->
                apDownload.setOnClickListener {
                    ResourceHelper.saveMediaToStorage(context, bitmap) {
                        Toast.makeText(context, "Wallpaper saved.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            apColors.setOnClickListener {
                WallpaperColorFragment.newInstance(wallpaper).show(supportFragmentManager, "wallpaperColors")
            }
            wallTabs.addOnTabSelectedListener(context)
            val showLockscreen = wallTabs.getTabAt(0)!!.isSelected
            if (showLockscreen) {
                ObjectToolsAnimator.show(binding.previewLockscreen, 500)
                ObjectToolsAnimator.hide(binding.previewLauncher, 500)
            } else {
                ObjectToolsAnimator.hide(binding.previewLockscreen, 500)
                ObjectToolsAnimator.show(binding.previewLauncher, 500)
            }

            WallpaperPreview(context, previewContainerLauncher, previewSurfaceLockscreen, wallpaper)
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            1 -> {
                ObjectToolsAnimator.hide(binding.previewLockscreen, 500)
                ObjectToolsAnimator.show(binding.previewLauncher, 500)
            }
            0 -> {
                ObjectToolsAnimator.show(binding.previewLockscreen, 500)
                ObjectToolsAnimator.hide(binding.previewLauncher, 500)
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val resultUri: Uri? = result!!.uri
            val drawable = Drawable.createFromStream(
                contentResolver.openInputStream(resultUri!!),
                resultUri.toString()
            )
            wallpaper.uri = resultUri.toString()
            targetWall = drawable
            binding.previewImageLockscreen.load(Uri.parse(wallpaper.uri!!) ) {
                crossfade(true)
            }
            binding.previewImageLauncher.load(Uri.parse(wallpaper.uri!!)) {
                crossfade(true)
            }
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            val isGranted =
                permissions.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                loadUI()
            } else {
                finish()
            }
        }
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
        private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
    }
}