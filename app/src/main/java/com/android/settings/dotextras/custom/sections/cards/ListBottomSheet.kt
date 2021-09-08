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
package com.android.settings.dotextras.custom.sections.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.dot.ui.system.FeatureManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class ListBottomSheet : BottomSheetDialogFragment() {

    private var title: String = ""
    private var callback: Callback? = null
    private var feature: String? = null
    private var featureType: Int? = null
    private var default: Int? = null
    private var entries: ArrayList<String> = ArrayList()
    private var entryValues: ArrayList<Int> = ArrayList()

    var fm: FeatureManager? = null
    var adapter: ListBottomSheetAdapter? = null
    private lateinit var recycler: RecyclerView
    private lateinit var titleView: TextView
    private lateinit var applyButton: MaterialButton

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.list_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fm = FeatureManager(requireContext().contentResolver)
        recycler = requireView().requireViewById(R.id.list_recycler)
        titleView = requireView().requireViewById(R.id.list_title)
        applyButton = requireView().requireViewById(R.id.list_apply)
        titleView.text = title
        if (featureType == null && feature == null && default == null) dismiss()
        recycler.adapter = adapter
        applyButton.setOnClickListener {
            val option = adapter!!.getSelected()
            if (option != null) {
                callback!!.onApply(option.entry)
                dismiss()
            }
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val bottomSheet: View = dialog!!.findViewById(R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setupDialog(
        title: String,
        feature: String,
        featureType: Int,
        default: Int,
        entries: ArrayList<String>,
        entryValues: ArrayList<Int>,
        callback: Callback
    ): ListBottomSheet {
        this.title = title
        this.feature = feature
        this.featureType = featureType
        this.default = default
        this.entries = entries
        this.entryValues = entryValues
        this.callback = callback
        val options = ArrayList<ListBottomSheetAdapter.Option>()
        for (i in entries.indices) {
            options.add(ListBottomSheetAdapter.Option(entries[i], entryValues[i]))
        }
        adapter = ListBottomSheetAdapter(featureType, feature, default, options, callback)
        return this
    }

    interface Callback {
        fun onApply(entry: String)
    }
}