package com.dot.customizations.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.google.android.material.appbar.CollapsingToolbarLayout

open class CollapsingToolbarFragment : AppbarFragment(),
    CustomizationSectionController.CustomizationSectionNavigationController {

    protected var collapsingToolbar: CollapsingToolbarLayout? = null
    protected open var layoutRes: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            com.android.settingslib.R.layout.collapsing_toolbar_base_layout,
            container,
            false
        )
        collapsingToolbar = view.findViewById(com.android.settingslib.R.id.collapsing_toolbar)
        val parent = view.findViewById<ViewGroup>(com.android.settingslib.R.id.content_frame)
        layoutRes?.let {
            parent?.removeAllViews()
            inflater.inflate(it, parent, true)
        }
        parent.setOnApplyWindowInsetsListener { v: View, windowInsets: WindowInsets ->
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets.consumeSystemWindowInsets()
        }
        setUpToolbar(view, true)
        return view
    }

    override fun getToolbarId(): Int {
        return com.android.settingslib.R.id.action_bar
    }

    override fun setTitle(title: CharSequence?) {
        collapsingToolbar?.title = title
        super.setTitle(title)
    }

    override fun navigateTo(fragment: Fragment?) {
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment!!)
            .addToBackStack(null)
            .commit()
        fragmentManager.executePendingTransactions()
    }

    override fun getRootFragment(): FragmentActivity = requireActivity()

}