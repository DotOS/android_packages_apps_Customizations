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
package com.dot.customizations.picker.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.databinding.FragmentExtrasBinding
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.AppbarFragment
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class ExtrasFragment : AppbarFragment(), PreferencesAdapter.OnScreenChangeListener {

    private val viewModel: ExtrasViewModel by viewModels()
    private val preferencesAdapter get() = viewModel.preferencesAdapter
    private lateinit var preferencesView: RecyclerView
    private lateinit var mSectionNavigationController:
            CustomizationSectionController.CustomizationSectionNavigationController

    private var _binding: FragmentExtrasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtrasBinding.inflate(inflater)
        // For nav bar edge-to-edge effect.
        viewModel.navigationController = mSectionNavigationController
        binding.root.setOnApplyWindowInsetsListener { v: View, windowInsets: WindowInsets ->
            v.setPadding(
                v.paddingLeft,
                windowInsets.systemWindowInsetTop,
                v.paddingRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets.consumeSystemWindowInsets()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                setUpToolbar(root)
                preferencesView = extrasRecycler.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = preferencesAdapter
                    layoutAnimation = AnimationUtils.loadLayoutAnimation(
                        requireContext(),
                        R.anim.preference_layout_fall_down
                    )
                }
                preferencesAdapter.restoreAndObserveScrollPosition(preferencesView)
                onScreenChanged(
                    preferencesAdapter.currentScreen,
                    preferencesAdapter.isInSubScreen()
                )
                preferencesAdapter.onScreenChangeListener = this@ExtrasFragment
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return preferencesAdapter.goBack()
    }

    override fun onDestroy() {
        preferencesAdapter.onScreenChangeListener = null
        preferencesView.adapter = null
        super.onDestroy()
    }

    companion object {
        fun newInstance(
            title: CharSequence?,
            mSectionNavigationController: CustomizationSectionController.CustomizationSectionNavigationController
        ): ExtrasFragment {
            val fragment = ExtrasFragment()
            fragment.arguments = createArguments(title)
            fragment.mSectionNavigationController = mSectionNavigationController
            return fragment
        }
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        preferencesView.scheduleLayoutAnimation()
    }

}