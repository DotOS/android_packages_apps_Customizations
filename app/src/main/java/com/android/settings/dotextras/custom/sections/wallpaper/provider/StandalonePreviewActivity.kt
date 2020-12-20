package com.android.settings.dotextras.custom.sections.wallpaper.provider

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.WallpaperSection
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase
import com.google.android.material.appbar.AppBarLayout

class StandalonePreviewActivity : AppCompatActivity() {

    private var standalone = false
    private lateinit var appBarLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        appBarLayout = findViewById(R.id.appblayout)
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
    }

    fun scrollTo(x: Int, y: Int) {
        findViewById<NestedScrollView>(R.id.nestedContainer).scrollTo(x, y)
    }

    fun toggleAppBar(hide: Boolean) {
        val lp = appBarLayout.layoutParams as AppBarLayout.LayoutParams
        val actionBarHeight = AppBarLayout.LayoutParams.WRAP_CONTENT
        lp.height = if (hide) 0 else actionBarHeight
        appBarLayout.layoutParams = lp
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null) {
            loadPreviewFragment()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
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
            val uri: Uri = intent.data
            val drawable = Drawable.createFromStream(
                contentResolver.openInputStream(uri),
                uri.toString()
            )
            val display: DisplayMetrics = resources.displayMetrics
            val bitmapDrawable =
                scaleCropToFit(
                    drawableToBitmap(drawable)!!,
                    display.widthPixels,
                    display.heightPixels
                )
            val wallpaperBase = WallpaperBase(BitmapDrawable(resources, bitmapDrawable))
            wallpaperBase.type = wallpaperBase.GALLERY
            wallpaperBase.uri = uri
            wallpaperBase.listener = { _, _ ->
                run {
                    Toast.makeText(this, getString(R.string.wall_applied), Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ApplyStandaloneFragment(wallpaperBase))
                .commit();
        } else supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, WallpaperSection(standalone))
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

    private fun scaleCropToFit(original: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap? {
        val width = original.width
        val height = original.height
        val widthScale = targetWidth.toFloat() / width.toFloat()
        val heightScale = targetHeight.toFloat() / height.toFloat()
        val scaledWidth: Float
        val scaledHeight: Float
        var startY = 0
        var startX = 0
        if (widthScale > heightScale) {
            scaledWidth = targetWidth.toFloat()
            scaledHeight = height * widthScale
            startY = ((scaledHeight - targetHeight) / 2).toInt()
        } else {
            scaledHeight = targetHeight.toFloat()
            scaledWidth = width * heightScale
            startX = ((scaledWidth - targetWidth) / 2).toInt()
        }
        val scaledBitmap = Bitmap.createScaledBitmap(
            original,
            scaledWidth.toInt(), scaledHeight.toInt(), true
        )
        return Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val TAG = "StandalonePreview"
        private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
    }
}