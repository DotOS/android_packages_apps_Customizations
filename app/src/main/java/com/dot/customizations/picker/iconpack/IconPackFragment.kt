/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.dot.customizations.picker.iconpack

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.CustomizationManager.OptionsFetchedListener
import com.dot.customizations.model.CustomizationOption
import com.dot.customizations.model.iconpack.IconPackManager
import com.dot.customizations.model.iconpack.IconPackOption
import com.dot.customizations.model.theme.OverlayManagerCompat
import com.dot.customizations.picker.AppbarFragment
import com.dot.customizations.widget.BottomActionBar
import com.dot.customizations.widget.BottomActionBar.BottomAction
import com.dot.customizations.widget.OptionSelectorController
import com.dot.customizations.widget.OptionSelectorController.CheckmarkStyle
import com.dot.customizations.widget.OptionSelectorController.OptionSelectedListener

/**
 * Fragment that contains the UI for selecting and applying a IconPackOption.
 */
class IconPackFragment : AppbarFragment() {
    private var mOptionsContainer: RecyclerView? = null
    private lateinit var mOptionsController: OptionSelectorController<IconPackOption>
    private lateinit var mIconPackManager: IconPackManager
    private lateinit var mSelectedOption: IconPackOption
    private var mLoading: ContentLoadingProgressBar? = null
    private var mContent: ViewGroup? = null
    private var mError: View? = null
    private var mBottomActionBar: BottomActionBar? = null
    private val mApplyIconPackCallback: CustomizationManager.Callback =
        object : CustomizationManager.Callback {
            override fun onSuccess() {}
            override fun onError(throwable: Throwable?) {
                // Since we disabled it when clicked apply button.
                mBottomActionBar!!.enableActions()
                mBottomActionBar!!.hide()
                //TODO(chihhangchuang): handle
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_icon_pack_picker, container,  /* attachToRoot */false
        )
        setUpToolbar(view)
        mContent = view.findViewById(R.id.content_section)
        mOptionsContainer = view.findViewById(R.id.options_container)
        mLoading = view.findViewById(R.id.loading_indicator)
        mError = view.findViewById(R.id.error_section)

        // For nav bar edge-to-edge effect.
        view.setOnApplyWindowInsetsListener { v: View, windowInsets: WindowInsets ->
            v.setPadding(
                v.paddingLeft,
                windowInsets.systemWindowInsetTop,
                v.paddingRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets.consumeSystemWindowInsets()
        }
        mIconPackManager = IconPackManager.getInstance(
            context!!, OverlayManagerCompat(
                context
            )
        )
        setUpOptions(savedInstanceState)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mBottomActionBar != null) {
            outState.putBoolean(KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE, mBottomActionBar!!.isVisible)
        }
    }

    override fun onBottomActionBarReady(bottomActionBar: BottomActionBar) {
        super.onBottomActionBarReady(bottomActionBar)
        mBottomActionBar = bottomActionBar
        mBottomActionBar!!.showActionsOnly(BottomAction.APPLY_TEXT)
        mBottomActionBar!!.setActionClickListener(BottomAction.APPLY_TEXT) {
            applyIconPackOption(
                mSelectedOption
            )
        }
    }

    private fun applyIconPackOption(iconPackOption: IconPackOption) {
        mBottomActionBar!!.disableActions()
        mIconPackManager.apply(iconPackOption, mApplyIconPackCallback)
    }

    private fun setUpOptions(savedInstanceState: Bundle?) {
        hideError()
        mLoading!!.show()
        mIconPackManager.fetchOptions(object : OptionsFetchedListener<IconPackOption> {
            override fun onOptionsLoaded(options: List<IconPackOption>) {
                mLoading!!.hide()
                mOptionsController = OptionSelectorController(
                    mOptionsContainer, options,false, CheckmarkStyle.CORNER
                )
                mOptionsController.initOptions(mIconPackManager)
                mSelectedOption = getActiveOption(options)
                mOptionsController.setSelectedOption(mSelectedOption)
                onOptionSelected(mSelectedOption)
                restoreBottomActionBarVisibility(savedInstanceState)
                mOptionsController.addListener(OptionSelectedListener { selectedOption: CustomizationOption<*>? ->
                    onOptionSelected(selectedOption)
                    mBottomActionBar!!.show()
                })
            }

            override fun onError(throwable: Throwable?) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading iconpack options", throwable)
                }
                showError()
            }
        },  /*reload= */true)
    }

    private fun getActiveOption(options: List<IconPackOption>): IconPackOption {
        return options.stream()
            .filter { option: IconPackOption -> option.isActive(mIconPackManager) }
            .findAny() // For development only, as there should always be an iconpack set.
            .orElse(options[0])
    }

    private fun hideError() {
        mContent!!.visibility = View.VISIBLE
        mError!!.visibility = View.GONE
    }

    private fun showError() {
        mLoading!!.hide()
        mContent!!.visibility = View.GONE
        mError!!.visibility = View.VISIBLE
    }

    private fun onOptionSelected(selectedOption: CustomizationOption<*>?) {
        mSelectedOption = selectedOption as IconPackOption
        refreshPreview()
    }

    private fun refreshPreview() {
        mSelectedOption.bindPreview(mContent!!)
    }

    private fun restoreBottomActionBarVisibility(savedInstanceState: Bundle?) {
        val isBottomActionBarVisible = (savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE))
        if (mBottomActionBar == null) return
        if (isBottomActionBarVisible) {
            mBottomActionBar!!.show()
        } else {
            mBottomActionBar!!.hide()
        }
    }

    companion object {
        private const val TAG = "IconPackFragment"
        private const val KEY_STATE_SELECTED_OPTION = "IconPackFragment.selectedOption"
        private const val KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE =
            "IconPackFragment.bottomActionBarVisible"

        fun newInstance(title: CharSequence?): IconPackFragment {
            val fragment = IconPackFragment()
            fragment.arguments = createArguments(title)
            return fragment
        }
    }
}