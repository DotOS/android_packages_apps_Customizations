package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.settings.dotextras.R
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
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_wallpaper_apply.*
import kotlinx.android.synthetic.main.item_wallpaper_preview_card_big.*
import nl.komponents.kovenant.task
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class WallpaperApplyActivity : AppCompatActivity(R.layout.activity_wallpaper_apply) {

    var wallpaper: Wallpaper? = null

    private lateinit var wallpaperManager: WallpaperManager
    private lateinit var targetWall: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallpaper = intent.getSerializableExtra("wallpaperObject") as Wallpaper
        wallpaperManager = WallpaperManager.getInstance(this)
        homescreenOverlay.visibility = View.VISIBLE
        val isSystem = wallpaper!!.uri != null
        targetWall =
            if (!isSystem) urlToDrawable(wallpaper!!.url!!)!! else uriToDrawable(Uri.parse(wallpaper!!.uri!!))
        val uriB = if (!isSystem) {
            val bitmap =
                BitmapFactory.decodeStream(URL(wallpaper!!.url!!).openConnection().getInputStream())
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
        } else Uri.parse(wallpaper!!.uri!!)
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

        val glideListener = object: RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                imgLoading.visibility = View.GONE
                Toast.makeText(this@WallpaperApplyActivity, "Error loading wallpaper", Toast.LENGTH_SHORT).show()
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

        if (isSystem) {
            Glide.with(this)
                .load(Uri.parse(wallpaper!!.uri!!))
                .listener(glideListener)
                .into(wallpaperPreviewImage)
        } else {
            Glide.with(this)
                .load(Uri.parse(wallpaper!!.url))
                .listener(glideListener)
                .into(wallpaperPreviewImage)
        }
        ap_apply.setOnClickListener {
            if (wallpaperPreviewImage.drawable != null)
                ApplyForDialogFragment(wallpaper!!).show(supportFragmentManager, "applyWallpaper")
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
            wallpaper!!.uri = resultUri.toString()
            wallpaper!!.url = null
            targetWall = drawable
            Glide.with(this)
                .load(Uri.parse(wallpaper!!.uri!!))
                .thumbnail(0.1f)
                .into(wallpaperPreviewImage)
            val monetColor = MonetManager(this).getSwatchFromTarget(targetWall)
            monetColorCard.imageTintList = ColorStateList.valueOf(monetColor.rgb)
        }
    }

    private fun urlToDrawable(urlString: String): Drawable? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        return try {
            BitmapDrawable(
                Resources.getSystem(),
                BitmapFactory.decodeStream(URL(urlString).content as InputStream)
            )
        } catch (e: IOException) {
            null
        }
    }

}