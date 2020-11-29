package com.android.settings.dotextras

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.android.settings.dotextras.custom.SectionFragment
import com.google.android.material.appbar.AppBarLayout

class BaseActivity : AppCompatActivity() {

    private var appTitle: TextView? = null
    private var appBar: AppBarLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_layout)
        appTitle = findViewById(R.id.appTitle)
        appBar = findViewById(R.id.dashboardAppBar)
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

    fun hideAppBar(hide: Boolean) {
        appBar?.visibility = if (hide) View.GONE else View.VISIBLE
    }

    fun setTitle(title: String?) = if (title == null) {
        appTitle!!.text = getString(R.string.app_name)
    } else {
        appTitle!!.text = title
    }

    fun resetUI() {
        hideAppBar(false)
        setTitle(null)
    }

}