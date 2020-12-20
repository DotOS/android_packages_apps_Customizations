package com.android.settings.dotextras.custom.sections.wallpaper.cropper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImageOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.io.File
import java.io.IOException

class WallpaperCropperActivity : AppCompatActivity(), CropImageView.OnCropImageCompleteListener,
    CropImageView.OnSetImageUriCompleteListener {

    private lateinit var cropImageView: CropImageView
    private lateinit var sheetLayout: LinearLayout
    private lateinit var applyCrop: MaterialButton

    private lateinit var ratio189: Chip
    private lateinit var ratio169: Chip
    private lateinit var ratio43: Chip
    private lateinit var ratio11: Chip
    private lateinit var ratiocustom: Chip

    private lateinit var reflectHorizontal: Chip
    private lateinit var reflectVertical: Chip

    private lateinit var rotateLeft: Chip
    private lateinit var rotateRight: Chip

    private lateinit var imageUri: Uri
    private lateinit var mOptions: CropImageOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        cropImageView = findViewById(R.id.cropImageView)
        applyCrop = findViewById(R.id.cardApplyCrop)
        sheetLayout = findViewById(R.id.cropSheet)
        ratio189 = findViewById(R.id.ratio189)
        ratio169 = findViewById(R.id.ratio169)
        ratio43 = findViewById(R.id.ratio43)
        ratio11 = findViewById(R.id.ratio11)
        ratiocustom = findViewById(R.id.ratiocustom)
        reflectHorizontal = findViewById(R.id.reflectHorizontal)
        reflectVertical = findViewById(R.id.reflectVertical)
        rotateLeft = findViewById(R.id.rotateLeft)
        rotateRight = findViewById(R.id.rotateRight)
        val bottomSheetBehavior = BottomSheetBehavior.from(sheetLayout)
        val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
        imageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)
        mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)
        cropImageView.setImageUriAsync(imageUri)
        ratio189.setOnClickListener { cropImageView.setAspectRatio(9, 18) }
        ratio169.setOnClickListener { cropImageView.setAspectRatio(9, 16) }
        ratio43.setOnClickListener { cropImageView.setAspectRatio(3, 4) }
        ratio11.setOnClickListener { cropImageView.setAspectRatio(1, 1) }
        ratiocustom.setOnClickListener { cropImageView.setFixedAspectRatio(false) }
        reflectHorizontal.setOnClickListener { cropImageView.flipImageHorizontally() }
        reflectVertical.setOnClickListener { cropImageView.flipImageVertically() }
        rotateLeft.setOnClickListener { rotateImage(-mOptions.rotationDegrees) }
        rotateRight.setOnClickListener { rotateImage(mOptions.rotationDegrees) }
        applyCrop.setOnClickListener { cropImage() }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(@NonNull view: View, newState: Int) {
            }

            override fun onSlide(@NonNull view: View, v: Float) {
            }
        })
    }

    override fun onStart() {
        super.onStart()
        cropImageView.setOnSetImageUriCompleteListener(this)
        cropImageView.setOnCropImageCompleteListener(this)
    }

    override fun onStop() {
        super.onStop()
        cropImageView.setOnSetImageUriCompleteListener(null)
        cropImageView.setOnCropImageCompleteListener(null)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResultCancel()
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                setResultCancel()
            }
            if (resultCode == RESULT_OK) {
                imageUri = CropImage.getPickImageResultUri(this, data)!!

                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                    )
                } else {
                    cropImageView.setImageUriAsync(imageUri)
                }
            }
        }
    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult) {
        setResult(result.uri, result.error, result.sampleSize)
    }

    override fun onSetImageUriComplete(view: CropImageView?, uri: Uri?, error: Exception?) {
        if (error == null) {
            if (mOptions.initialCropWindowRectangle != null) {
                cropImageView.cropRect = (mOptions.initialCropWindowRectangle)
            }
            if (mOptions.initialRotation > -1) {
                cropImageView.rotatedDegrees = (mOptions.initialRotation)
            }
        } else {
            setResult(null, error, 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                cropImageView.setImageUriAsync(imageUri)
            } else {
                Toast.makeText(this, R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG)
                    .show()
                setResultCancel()
            }
        }
    }

    /**
     * Execute crop image and save the result tou output uri.
     */
    private fun cropImage() {
        if (mOptions.noOutputImage) {
            setResult(null, null, 1)
        } else {
            val outputUri = outputUri
            cropImageView.saveCroppedImageAsync(
                outputUri,
                mOptions.outputCompressFormat,
                mOptions.outputCompressQuality,
                mOptions.outputRequestWidth,
                mOptions.outputRequestHeight,
                mOptions.outputRequestSizeOptions
            )
        }
    }

    /**
     * Rotate the image in the crop image view.
     */
    private fun rotateImage(degrees: Int) {
        cropImageView.rotateImage(degrees)
    }

    /**
     * Get Android uri to save the cropped image into.<br></br>
     * Use the given in options or create a temp file.
     */
    private val outputUri: Uri
        get() {
            var outputUri = mOptions.outputUri
            if (outputUri == Uri.EMPTY) {
                outputUri = try {
                    val ext =
                        if (mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG) ".jpg" else if (mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG) ".png" else ".webp"
                    Uri.fromFile(File.createTempFile("cropped", ext, cacheDir))
                } catch (e: IOException) {
                    throw RuntimeException("Failed to create temp file for output image", e)
                }
            }
            return outputUri
        }

    /**
     * Result with cropped image data or error if failed.
     */
    private fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        val resultCode =
            if (error == null) RESULT_OK else CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE
        setResult(resultCode, getResultIntent(uri, error, sampleSize))
        finish()
    }

    /**
     * Cancel of cropping activity.
     */
    private fun setResultCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    private fun getResultIntent(uri: Uri?, error: Exception?, sampleSize: Int): Intent {
        val result = CropImage.ActivityResult(
            cropImageView.imageUri,
            uri,
            error,
            cropImageView.cropPoints,
            cropImageView.cropRect,
            cropImageView.rotatedDegrees,
            cropImageView.wholeImageRect,
            sampleSize
        )
        val intent = Intent()
        intent.putExtras(getIntent())
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        return intent
    }

}