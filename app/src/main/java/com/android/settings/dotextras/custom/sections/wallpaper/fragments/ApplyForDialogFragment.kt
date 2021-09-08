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
package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import android.app.WallpaperManager
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.dot.ui.utils.setWallpaper
import com.dot.ui.utils.uriToDrawable
import com.dot.ui.utils.urlToDrawable
import kotlinx.android.synthetic.main.item_wallpaper_for.*

class ApplyForDialogFragment : DialogFragment() {

    var wallpaper: Wallpaper? = null

    private lateinit var wallpaperManager: WallpaperManager

    companion object {
        fun newInstance(wallpaper: Wallpaper): ApplyForDialogFragment {
            val fragment = ApplyForDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable("wallpaper", wallpaper)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireDialog().window!!.setBackgroundDrawableResource(android.R.color.transparent)
        requireDialog().window!!.setGravity(Gravity.BOTTOM)
        wallpaper = arguments?.getSerializable("wallpaper") as Wallpaper
        return inflater.inflate(R.layout.item_wallpaper_for, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wallpaperManager = WallpaperManager.getInstance(requireContext())
        if (wallpaper == null) return
        val isSystem = wallpaper!!.uri != null
        val drawable: Drawable = if (isSystem) {
            requireContext().uriToDrawable(Uri.parse(wallpaper!!.uri))
        } else {
            requireContext().urlToDrawable(wallpaper!!.url!!)
        }
        val onSuccess: () -> Unit = {
            requireView().postDelayed({
                dismiss()
            }, 500)
        }
        val successToast =
            Toast.makeText(requireContext(), "Wallpaper applied.", Toast.LENGTH_SHORT)
        wp_apply_home.setOnClickListener {
            wallpaperManager.setWallpaper(resources, drawable, WallpaperManager.FLAG_SYSTEM, onSuccess)
            successToast.show()
            dismiss()
        }
        wp_apply_lockscreen.setOnClickListener {
            wallpaperManager.setWallpaper(resources, drawable, WallpaperManager.FLAG_LOCK, onSuccess)
            successToast.show()
            dismiss()
        }
        wp_apply_both.setOnClickListener {
            wallpaperManager.setWallpaper(resources, drawable, onSuccess)
            successToast.show()
            dismiss()
        }

        requireDialog().window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().finish()
    }

}