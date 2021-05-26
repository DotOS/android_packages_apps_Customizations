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

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.CropImageView
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage
import com.android.settings.dotextras.custom.sections.wallpaper.cropper.utils.CropImage.getActivityResult
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.ApplyForDialogFragment
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.CurrentWallpaperAdapter
import com.android.settings.dotextras.custom.utils.getFileName
import com.android.settings.dotextras.custom.utils.removeExtension
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ApplyStandaloneFragment(val wallpaper: Wallpaper) : Fragment() {

    private lateinit var wallpaperManager: WallpaperManager

    private lateinit var currentPager: ViewPager2
    private lateinit var applyButton: MaterialButton
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.layout_wallpaper_apply_standalone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyButton = view.findViewById(R.id.wp_apply_button)
        title = view.findViewById(R.id.wp_title)
        val chipRotate: Chip = view.findViewById(R.id.chipCrop)
        val uriB = Uri.parse(wallpaper.uri)
        chipRotate.setOnClickListener {
            CropImage.activity(uriB)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowCounterRotation(true)
                .setAllowRotation(true)
                .setAspectRatio(9, 18)
                .setOutputUri(Uri.EMPTY)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .start(requireContext(), this)
        }
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        currentPager = view.findViewById(R.id.standalonePager)
        val pagerLeft: ImageButton = view.findViewById(R.id.wallLeft)
        val pagerRight: ImageButton = view.findViewById(R.id.wallRight)
        if (wallpaper.uri != null) {
            title.text = requireContext().getFileName(Uri.parse(wallpaper.uri!!)).removeExtension()
        }
        pagerLeft.setOnClickListener {
            currentPager.setCurrentItem(currentPager.currentItem - 1, true)
        }
        pagerRight.setOnClickListener {
            currentPager.setCurrentItem(currentPager.currentItem + 1, true)
        }
        currentPager.adapter = CurrentWallpaperAdapter(requireActivity(), wallpaper)
        applyButton.setOnClickListener {
            ApplyForDialogFragment(wallpaper).show(parentFragmentManager, tag)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = getActivityResult(data)
            wallpaper.uri = result!!.uri.toString()
            currentPager.adapter = CurrentWallpaperAdapter(requireActivity(), wallpaper)
            currentPager.adapter!!.notifyDataSetChanged()
        }
    }
}