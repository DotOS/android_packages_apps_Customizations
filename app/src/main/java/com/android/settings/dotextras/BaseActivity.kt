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