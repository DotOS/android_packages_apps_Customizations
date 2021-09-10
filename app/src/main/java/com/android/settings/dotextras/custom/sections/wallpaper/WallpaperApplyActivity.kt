package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.android.settings.dotextras.custom.sections.wallpaper.colors.WallpaperColor
import com.android.settings.dotextras.custom.sections.wallpaper.colors.WallpaperColorFragment
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.CropImageView
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.ApplyForDialogFragment
import com.android.settings.dotextras.custom.utils.*
import com.android.settings.dotextras.custom.views.WallpaperPreview
import com.android.settings.dotextras.databinding.ActivityWallpaperApplyBinding
import com.dot.ui.utils.*
import com.google.android.material.tabs.TabLayout
import nl.komponents.kovenant.task
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class WallpaperApplyActivity : AppCompatActivity(),
    TabLayout.OnTabSelectedListener {

    private lateinit var wallpaper: Wallpaper
    private lateinit var binding: ActivityWallpaperApplyBinding
    private lateinit var wallpaperManager: WallpaperManager
    private lateinit var targetWall: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                val context = this@WallpaperApplyActivity
                wallpaper = intent.getSerializableExtra("wallpaperObject") as Wallpaper
                wallpaperManager = WallpaperManager.getInstance(context)
                val isSystem = wallpaper.uri != null
                targetWall = if (!isSystem)
                    urlToDrawable(wallpaper.url!!)
                else
                    uriToDrawable(Uri.parse(wallpaper.uri!!))
                val uriB = if (!isSystem) {
                    val bitmap =
                        BitmapFactory.decodeStream(URL(wallpaper.url!!).openConnection().getInputStream())
                    val imageFileName = "temp.jpg"
                    val storageDir = File(cacheDir.toString())
                    val imageFile = File(storageDir, imageFileName)
                    try {
                        val fOut = FileOutputStream(imageFile)
                        task {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                            fOut.close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Uri.fromFile(imageFile)
                } else Uri.parse(wallpaper.uri!!)
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
                previewImageLockscreen.load(targetWall) { crossfade(true) }

                apApply.setOnClickListener {
                    if (previewImageLockscreen.drawable != null)
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
                WallpaperPreview(context, previewContainerLauncher, previewSurfaceLockscreen, wallpaper)
            }
        }
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
            wallpaper.url = null
            targetWall = drawable
            binding.previewImageLockscreen.load(resultUri) { crossfade(true) }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            0 -> {
                ObjectToolsAnimator.show(binding.previewLockscreen, 500)
                ObjectToolsAnimator.hide(binding.previewLauncher, 200)
            }
            1 -> {
                ObjectToolsAnimator.hide(binding.previewLockscreen, 200)
                ObjectToolsAnimator.show(binding.previewLauncher, 500)
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}

}