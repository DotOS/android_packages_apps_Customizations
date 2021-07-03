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
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.WallpaperSection
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.CropImageView
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.ApplyForDialogFragment
import com.android.settings.dotextras.custom.utils.ObjectToolsAnimator
import com.android.settings.dotextras.system.MonetManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_wallpaper_apply.*
import kotlinx.android.synthetic.main.item_wallpaper_preview_card_big.*
import java.io.File

class StandalonePreviewActivity : AppCompatActivity(R.layout.activity_wallpaper_apply) {

    private lateinit var wallpaper: Wallpaper
    private lateinit var wallpaperManager: WallpaperManager
    private lateinit var targetWall: Drawable
    private val glideListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            imgLoading.visibility = View.GONE
            Toast.makeText(
                this@StandalonePreviewActivity,
                "Error loading wallpaper",
                Toast.LENGTH_SHORT
            ).show()
            if (e != null) Log.e("GlideException", e.stackTrace.toString())
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            imgLoading.visibility = View.GONE
            return false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        } else loadStandalone()
    }

    private fun loadStandalone() {
        applyContainer.removeAllViews()
        supportFragmentManager.beginTransaction()
            .replace(R.id.applyContainer, WallpaperSection(true))
            .commit()
    }

    private fun loadUI() {
        wallpaper = Wallpaper()
        wallpaper.uri = intent.data.toString()
        wallpaperManager = WallpaperManager.getInstance(this)
        homescreenOverlay.visibility = View.VISIBLE
        targetWall = uriToDrawable(Uri.parse(wallpaper.uri!!))
        val uriB = Uri.parse(wallpaper.uri!!)
        ap_crop.setOnClickListener {
            CropImage.activity(uriB)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowCounterRotation(true)
                .setAllowRotation(true)
                .setAspectRatio(9, 18)
                .setOutputUri(Uri.EMPTY)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .start(this)
        }
        imgLoading.visibility = View.VISIBLE

        Glide.with(this)
            .load(Uri.parse(wallpaper.uri!!))
            .listener(glideListener)
            .into(wallpaperPreviewImage)
        ap_apply.setOnClickListener {
            if (wallpaperPreviewImage.drawable != null)
                ApplyForDialogFragment.newInstance(wallpaper).show(supportFragmentManager, "applyWallpaper")
        }

        wallTabs {
            orientation = LinearLayout.HORIZONTAL
            initialCheckedIndex = 0
            initWithItems {
                listOf(getString(R.string.homescreen), getString(R.string.lockscreen))
            }
            onSegmentChecked {
                when (it.text) {
                    getString(R.string.homescreen) -> {
                        ObjectToolsAnimator.hide(lockscreenOverlay, 500)
                        ObjectToolsAnimator.show(homescreenOverlay, 500)
                    }
                    getString(R.string.lockscreen) -> {
                        ObjectToolsAnimator.show(lockscreenOverlay, 500)
                        ObjectToolsAnimator.hide(homescreenOverlay, 500)
                    }
                }
            }
        }
        val monetColor = MonetManager(this).getSwatchFromTarget(targetWall)
        monetColorCard.imageTintList = ColorStateList.valueOf(monetColor.rgb)
    }


    private fun uriToDrawable(uri: Uri): Drawable {
        return Drawable.createFromStream(contentResolver.openInputStream(uri), uri.toString())
    }

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
            Glide.with(this)
                .load(Uri.parse(wallpaper.uri!!))
                .listener(glideListener)
                .thumbnail(0.1f)
                .into(wallpaperPreviewImage)
            val monetColor = MonetManager(this).getSwatchFromTarget(targetWall)
            monetColorCard.imageTintList = ColorStateList.valueOf(monetColor.rgb)
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