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
package com.android.settings.dotextras.custom.views

import android.content.Context
import android.content.om.IOverlayManager
import android.os.ServiceManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.themes.AccentAdapter
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.system.FeatureManager
import com.android.settings.dotextras.system.OverlayController

class AccentColorController(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private val featureManager: FeatureManager
    private val recycler: RecyclerView

    init {
        LayoutInflater.from(mContext).inflate(
            R.layout.item_accent_control, this, true
        )
        val overlayController = OverlayController(
            OverlayController.Categories.ACCENT_CATEGORY,
            mContext.packageManager,
            IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
        )
        recycler = findViewById(R.id.accentRecycler)
        val adapter = AccentAdapter(
            overlayController, overlayController.AccentColors().getAccentColors(context!!)
        )
        recycler.adapter = adapter
        recycler.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacerBigger),
                0,
                false
            )
        )
        recycler.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacerBig),
                adapter.itemCount - 1,
                true
            )
        )
        featureManager = FeatureManager(context.contentResolver)
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        visibility = if (overlayController.isAvailable()) VISIBLE else GONE
    }

    fun updateVisibility(configuration: Int) {
        recycler.visibility =
            if (featureManager.AccentManager().isUsingRGBAccent(configuration)) GONE else VISIBLE
    }

}