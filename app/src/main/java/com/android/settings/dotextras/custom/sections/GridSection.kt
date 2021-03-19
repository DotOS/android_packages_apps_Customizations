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
package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.grid.*
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer

class GridSection : GenericSection() {

    private lateinit var recycler: RecyclerView
    private lateinit var mGridManager: GridOptionsManager
    private lateinit var mGridOptionPreviewer: GridOptionPreviewer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mGridManager = GridOptionsManager(
            LauncherGridOptionsProvider(
                requireContext(),
                getString(R.string.grid_control_metadata_name)
            )
        )

        recycler = view.findViewById(R.id.gridRecycler)
        mGridOptionPreviewer = GridOptionPreviewer(
            mGridManager,
            view.findViewById(R.id.grid_preview_container)
        )
        fetchOptions(false)
    }

    private fun fetchOptions(reload: Boolean) {
        mGridManager.fetchOptions({ options ->
            run {
                if (options != null) {
                    val optionsCompat: ArrayList<GridOptionCompat> = ArrayList()
                    for (option in options) {
                        val gridCompat = GridOptionCompat(option)
                        gridCompat.listener = {
                            run {
                                mGridOptionPreviewer.setGridOption(it)
                                fetchOptions(true)
                            }
                        }
                        if (option.isActive()) mGridOptionPreviewer.setGridOption(option)
                        optionsCompat.add(gridCompat)
                    }
                    recycler.adapter = GridRecyclerAdapter(mGridManager, optionsCompat)
                    recycler.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    if (!reload) addRecyclerDecoration()
                }
            }
        }, reload)
    }

    private fun addRecyclerDecoration() {
        recycler.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacerBigger),
                null,
                false
            )
        )
    }

    override fun isAvailable(context: Context): Boolean {
        return GridOptionsManager(
            LauncherGridOptionsProvider(
                context,
                context.getString(R.string.grid_control_metadata_name)
            )
        ).isAvailable
    }
}