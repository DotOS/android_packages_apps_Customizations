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
package com.android.settings.dotextras

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.android.settings.dotextras.custom.SectionFragment
import com.google.android.material.appbar.AppBarLayout

class BaseActivity : AppCompatActivity() {
    private var appTitle: TextView? = null
    private var appBarLayout: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_layout)
        appTitle = findViewById(R.id.appTitle)
        appBarLayout = findViewById(R.id.appblayout)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameContent, SectionFragment(), "section_fragment")
                .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        resetUI()
    }

    fun setTitle(title: String?) = if (title == null) {
        appTitle!!.text = getString(R.string.app_name)
    } else {
        appTitle!!.text = title
    }

    private fun resetUI() {
        if (appBarLayout!!.layoutParams.height == 0)
            toggleAppBar(false)
        setTitle(null)
    }

    fun scrollTo(x: Int, y: Int) {
        findViewById<NestedScrollView>(R.id.nestedContainer).scrollTo(x, y)
    }

    fun toggleAppBar(hide: Boolean) {
        val lp = appBarLayout!!.layoutParams as AppBarLayout.LayoutParams
        val actionBarHeight = AppBarLayout.LayoutParams.WRAP_CONTENT
        lp.height = if (hide) 0 else actionBarHeight
        appBarLayout!!.layoutParams = lp
    }

}