package com.dot.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

class DotToolbar(context: Context, attrs: AttributeSet?) : AppBarLayout(context, attrs) {

    var collapsingToolbar: CollapsingToolbarLayout
    var toolbar: Toolbar
    var addToAppbar = false

    var title: String
        get() {
            return collapsingToolbar.title.toString()
        }
        set(value) {
            collapsingToolbar.title = value
        }

    init {
        View.inflate(context, R.layout.dot_toolbar, this)
        setBackgroundResource(R.color.colorPrimary)
        backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.colorPrimary))
        elevation = 0f
        requestLayout()
        collapsingToolbar = requireViewById(R.id.dotCollapsingToolbar)
        toolbar = requireViewById(R.id.dotToolbar)
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.DotToolbar)
        addToAppbar = a.getBoolean(R.styleable.DotToolbar_addToAppbar, false)
        collapsingToolbar.title = a.getString(R.styleable.DotToolbar_android_title)
        a.recycle()
    }

    fun canGoBack(activity: Activity) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { activity.onBackPressed() }
    }

    override fun addView(child: View?) {
        if (child is CollapsingToolbarLayout || addToAppbar)
            super.addView(child)
        else
            collapsingToolbar.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        if (child is CollapsingToolbarLayout || addToAppbar)
            super.addView(child, index)
        else
            collapsingToolbar.addView(child, index)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        if (child is CollapsingToolbarLayout || addToAppbar)
            super.addView(child, width, height)
        else
            collapsingToolbar.addView(child, width, height)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        if (child is CollapsingToolbarLayout || addToAppbar)
            super.addView(child, params)
        else
            collapsingToolbar.addView(child, params)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child is CollapsingToolbarLayout || addToAppbar)
            super.addView(child, index, params)
        else
            collapsingToolbar.addView(child, index, params)
    }

}