/*
 * Copyright (C) 2022 The DotOS Project
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
package com.dot.customizations.model.applock

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.AppbarFragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class AppLockFragment : AppbarFragment(), PreferencesAdapter.OnScreenChangeListener,
    CustomizationSectionController.CustomizationSectionNavigationController {

    private val viewModel: AppLockViewModel by viewModels()
    private lateinit var preferencesView: RecyclerView
    private var collapsingToolbar: CollapsingToolbarLayout? = null
    private var mSectionNavigationController:
            CustomizationSectionController.CustomizationSectionNavigationController = this

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
        retainInstance = true
        viewModel.navigationController = mSectionNavigationController
        viewModel.settingsPreferencesAdapter = PreferencesAdapter(viewModel.createSettingsScreen(requireContext()))
        collapsingToolbar = view.findViewById(com.android.settingslib.R.id.collapsing_toolbar)
        val parent = view.findViewById<ViewGroup>(com.android.settingslib.R.id.content_frame)
        parent?.removeAllViews()
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_applock, parent, true)

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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        viewModel.navigationController = mSectionNavigationController
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            preferencesView = view.findViewById(R.id.applockRecycler)
            preferencesView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = viewModel.settingsPreferencesAdapter
                layoutAnimation = AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.preference_layout_fall_down
                )
            }
            viewModel.settingsPreferencesAdapter.restoreAndObserveScrollPosition(preferencesView)
            onScreenChanged(
                viewModel.settingsPreferencesAdapter.currentScreen,
                viewModel.settingsPreferencesAdapter.isInSubScreen()
            )
            viewModel.settingsPreferencesAdapter.onScreenChangeListener = this@AppLockFragment
        }
    }

    override fun getToolbarId(): Int {
        return com.android.settingslib.R.id.action_bar
    }

    override fun onBackPressed(): Boolean {
        return viewModel.settingsPreferencesAdapter.goBack()
    }

    override fun onDestroy() {
        viewModel.settingsPreferencesAdapter.onScreenChangeListener = null
        preferencesView.adapter = null
        super.onDestroy()
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        preferencesView.scheduleLayoutAnimation()
    }

    override fun setTitle(title: CharSequence?) {
        collapsingToolbar?.title = title
        super.setTitle(title)
    }

    companion object {
        fun newInstance(
            title: CharSequence?
        ): AppLockFragment {
            val fragment = AppLockFragment()
            fragment.arguments = createArguments(title)
            return fragment
        }
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