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
package com.android.settings.dotextras.custom.displays

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper

class GridDisplay : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.display_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preview: AppCompatImageView = view.findViewById(R.id.gridDisplay)
        /*val path = Resources.getSystem().getIdentifier(OverlayController.Constants.CONFIG_ICON_MASK, "string", OverlayController.Constants.ANDROID_PACKAGE)
        val grid = GridTileDrawable(4, 4, resources.getString(path))
        grid.setColorFilter(ResourceHelper.getAccent(requireContext()), PorterDuff.Mode.ADD)
        preview.setImageDrawable(grid)*/
        preview.imageTintList = ColorStateList.valueOf(ResourceHelper.getAccent(requireContext()))
    }

}