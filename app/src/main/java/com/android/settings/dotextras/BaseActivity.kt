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

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.android.settings.dotextras.custom.SectionFragment
import com.android.settings.dotextras.custom.sections.SettingsSection
import com.android.settings.dotextras.custom.stats.StatsBuilder
import com.android.settings.dotextras.custom.utils.MaidService
import com.google.android.material.appbar.AppBarLayout


class BaseActivity : AppCompatActivity() {

    private var appTitle: TextView? = null
    private var appBarLayout: LinearLayout? = null
    private var launchSettings: ImageButton? = null
    lateinit var appBar: AppBarLayout

    private lateinit var statsBuilder: StatsBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_layout)
        statsBuilder = StatsBuilder(getSharedPreferences("dotStatsPrefs", Context.MODE_PRIVATE))
        startService(Intent(this, MaidService::class.java))
        appBar = findViewById(R.id.dashboardAppBar)
        appTitle = findViewById(R.id.appTitle)
        appBarLayout = findViewById(R.id.appblayout)
        launchSettings = findViewById(R.id.launchSettings)
        launchSettings!!.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                .replace(R.id.frameContent, SettingsSection(), "settings")
                .addToBackStack("Settings")
                .commit()
            setTitle("Settings")
        }
        appBarLayout!!.layoutTransition
            .enableTransitionType(LayoutTransition.CHANGING)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameContent, SectionFragment(), "section_fragment")
                .commit()
        }
        statsBuilder.push(this)
    }

    fun expandToolbar() {
        val params: CoordinatorLayout.LayoutParams = appBar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior?
        if (behavior != null) {
            val valueAnimator = ValueAnimator.ofInt()
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                behavior.topAndBottomOffset = (animation.animatedValue as Int)
                appBar.requestLayout()
            }
            valueAnimator.setIntValues(behavior.topAndBottomOffset, 0)
            valueAnimator.duration = 400
            valueAnimator.start()
        }
    }

    fun getNestedScroll() : NestedScrollView = findViewById(R.id.nestedContainer)

    fun enableSettingsLauncher(enable: Boolean) {
        launchSettings!!.visibility = if (enable) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        statsBuilder.clearComposite()
        super.onDestroy()
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
        enableSettingsLauncher(true)
        expandToolbar()
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