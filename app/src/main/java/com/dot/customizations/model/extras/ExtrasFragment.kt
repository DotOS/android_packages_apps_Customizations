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
package com.dot.customizations.model.extras

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.picker.CollapsingToolbarFragment
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class ExtrasFragment : CollapsingToolbarFragment(), PreferencesAdapter.OnScreenChangeListener {

    override var layoutRes: Int? = R.layout.fragment_extras

    private val viewModel: ExtrasViewModel by viewModels()
    private val preferencesAdapter by lazy { viewModel.preferencesAdapter as PreferencesAdapter }

    private var preferenceRecycler: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.preferencesAdapter = PreferencesAdapter(viewModel.createScreen(requireContext()))
        viewModel.navigationController = this
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            preferenceRecycler = view.findViewById(R.id.extrasRecycler)
            preferenceRecycler!!.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = preferencesAdapter
                layoutAnimation = AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.preference_layout_fall_down
                )
            }
            preferencesAdapter.restoreAndObserveScrollPosition(preferenceRecycler!!)
            onScreenChanged(
                preferencesAdapter.currentScreen,
                preferencesAdapter.isInSubScreen()
            )
            preferencesAdapter.onScreenChangeListener = this@ExtrasFragment
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onBackPressed(): Boolean {
        return preferencesAdapter.goBack()
    }

    override fun onDestroy() {
        preferencesAdapter.onScreenChangeListener = null
        preferenceRecycler?.adapter = null
        super.onDestroy()
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        preferenceRecycler!!.scheduleLayoutAnimation()
    }

    companion object {
        fun newInstance(
            title: CharSequence?
        ): ExtrasFragment {
            val fragment = ExtrasFragment()
            fragment.arguments = createArguments(title)
            return fragment
        }
    }

}