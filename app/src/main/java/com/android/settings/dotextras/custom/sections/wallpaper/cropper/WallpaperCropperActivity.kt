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
package com.android.settings.dotextras.custom.sections.wallpaper.cropper

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImageOptions
import com.android.settings.dotextras.databinding.ActivityCropBinding
import java.io.File
import java.io.IOException

class WallpaperCropperActivity : AppCompatActivity(), CropImageView.OnCropImageCompleteListener,
    CropImageView.OnSetImageUriCompleteListener {

    private lateinit var binding: ActivityCropBinding
    private lateinit var imageUri: Uri
    private lateinit var mOptions: CropImageOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                appTitle.setNavigationIcon(R.drawable.ic_arrow_back)
                appTitle.setNavigationOnClickListener { onBackPressed() }
                val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
                imageUri = bundle!!.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)!!
                mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)!!
                cropImageView.setImageUriAsync(imageUri)
                aspectRatio.setOnClickListener {
                    when (cropImageView.aspectRatio) {
                        Pair.create(9, 18) -> {
                            cropImageView.setAspectRatio(9, 16)
                        }
                        Pair.create(9, 16) -> {
                            cropImageView.setAspectRatio(3, 4)
                        }
                        Pair.create(3, 4) -> {
                            cropImageView.setAspectRatio(1, 1)
                        }
                        Pair.create(1, 1) -> {
                            cropImageView.setFixedAspectRatio(false)
                        }
                        else -> {
                            cropImageView.setAspectRatio(9, 18)
                        }
                    }
                }
                reflect.setOnClickListener {
                    if (!cropImageView.isFlippedVertically && cropImageView.isFlippedHorizontally) {
                        cropImageView.flipImageHorizontally()
                    } else if (cropImageView.isFlippedVertically && !cropImageView.isFlippedHorizontally) {
                        cropImageView.flipImageVertically()
                    } else if (!cropImageView.isFlippedVertically && !cropImageView.isFlippedHorizontally) {
                        cropImageView.flipImageVertically()
                    }
                }
                rotate.setOnClickListener {
                    rotateImage(mOptions.rotationDegrees)
                }
                cardApplyCrop.setOnClickListener { cropImage() }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.cropImageView.setOnSetImageUriCompleteListener(this)
        binding.cropImageView.setOnCropImageCompleteListener(this)
    }

    override fun onStop() {
        super.onStop()
        binding.cropImageView.setOnSetImageUriCompleteListener(null)
        binding.cropImageView.setOnCropImageCompleteListener(null)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResultCancel()
    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult) {
        setResult(result.uri, result.error, result.sampleSize)
    }

    override fun onSetImageUriComplete(view: CropImageView?, uri: Uri?, error: Exception?) {
        if (error == null) {
            if (mOptions.initialCropWindowRectangle != null) {
                binding.cropImageView.cropRect = (mOptions.initialCropWindowRectangle)
            }
            if (mOptions.initialRotation > -1) {
                binding.cropImageView.rotatedDegrees = (mOptions.initialRotation)
            }
        } else {
            setResult(null, error, 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                binding.cropImageView.setImageUriAsync(imageUri)
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
            binding.cropImageView.saveCroppedImageAsync(
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
        binding.cropImageView.rotateImage(degrees)
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
            binding.cropImageView.imageUri,
            uri,
            error,
            binding.cropImageView.cropPoints,
            binding.cropImageView.cropRect,
            binding.cropImageView.rotatedDegrees,
            binding.cropImageView.wholeImageRect,
            sampleSize
        )
        val intent = Intent()
        intent.putExtras(getIntent())
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        return intent
    }

}