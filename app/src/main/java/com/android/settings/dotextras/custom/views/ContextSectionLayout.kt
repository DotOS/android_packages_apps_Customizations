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
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R

class ContextSectionLayout(
    context: Context?,
    attrs: AttributeSet?,
) : LinearLayout(context, attrs) {

    private var title: TextView
    private var recylcer: RecyclerView
    var expandable: Boolean = true

    init {
        val layout = inflate(context, R.layout.layout_context_section, this)
        title = layout.findViewById(R.id.contextTitle)
        recylcer = layout.findViewById(R.id.contextRecycler)
        val expandableLayout: ExpandableLayout = layout.findViewById(R.id.contextExpandable)
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.ContextSectionLayout)
            setTitle(a.getString(R.styleable.ContextSectionLayout_sectionTitle))
            a.recycle()
        }
        if (expandable) {
            title.isClickable = true
            title.setOnClickListener { expandableLayout.toggle(true) }
        } else title.isClickable = false
    }

    fun hideTitle(hide: Boolean) {
        title.visibility = if (hide) View.GONE else View.VISIBLE
    }

    fun setTitle(string: String) {
        title.text = string
    }

    fun setTitle(resId: Int) {
        title.text = context.getString(resId)
    }

    fun addDecoration(decor: RecyclerView.ItemDecoration) {
        recylcer.addItemDecoration(decor)
    }

    fun addDecoration(decor: RecyclerView.ItemDecoration, index: Int) {
        recylcer.addItemDecoration(decor, index)
    }

    fun setupAdapter(adapter: RecyclerView.Adapter<*>) {
        recylcer.adapter = adapter
    }

    fun setLayoutManger(lm: RecyclerView.LayoutManager) {
        recylcer.layoutManager = lm
    }

    fun getRecycler() = recylcer
}