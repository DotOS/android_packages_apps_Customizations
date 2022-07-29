/*
 * Copyright (C) 2021-2022 AOSP-Krypton Project
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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settingslib.collapsingtoolbar.databinding.CollapsingToolbarBaseLayoutBinding
import com.dot.customizations.R
import com.dot.customizations.databinding.FragmentApplockPickerBinding
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.AppbarFragment
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppLockPickFragment : AppbarFragment(), PreferencesAdapter.OnScreenChangeListener,
    MenuItem.OnActionExpandListener {

    private val viewModel: AppLockViewModel by viewModels()
    private val preferencesAdapter get() = viewModel.pickerPreferenceAdapter
    private var mSectionNavigationController:
            CustomizationSectionController.CustomizationSectionNavigationController? = null

    private var _rootbinding: CollapsingToolbarBaseLayoutBinding? = null
    private val rootbinding get() = _rootbinding!!
    private var _binding: FragmentApplockPickerBinding? = null
    private val binding get() = _binding!!

    private var callback: PickerCallback? = null

    private var searchText = ""
    private var needsToHideProgressBar = false
    private var displayCategory: Int = CATEGORY_USER_ONLY
    private val packageList = mutableListOf<PackageInfo>()
    private var packageFilter: ((PackageInfo) -> Boolean) = { true }
    private var packageComparator: ((PackageInfo, PackageInfo) -> Int) = { a, b ->
        getLabel(a).compareTo(getLabel(b))
    }
    private lateinit var packageManager: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        packageManager = requireContext().packageManager
        packageList.addAll(packageManager.getInstalledPackages(0))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _rootbinding = CollapsingToolbarBaseLayoutBinding.inflate(inflater)
        retainInstance = true
        if (mSectionNavigationController != null)
            viewModel.navigationController = mSectionNavigationController
        viewModel.callback = callback
        val parent =
            rootbinding.root.findViewById<ViewGroup>(com.android.settingslib.collapsingtoolbar.R.id.content_frame)
        parent?.removeAllViews()
        _binding = FragmentApplockPickerBinding.inflate(
            LayoutInflater.from(rootbinding.root.context),
            parent,
            true
        )
        binding.root.setOnApplyWindowInsetsListener { v: View, windowInsets: WindowInsets ->
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets.consumeSystemWindowInsets()
        }
        setUpToolbar(rootbinding.root, true)
        setUpToolbarMenu(R.menu.app_list_menu)
        return rootbinding.root
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.let {
            if (it.itemId == R.id.search) {
                it.setOnActionExpandListener(this@AppLockPickFragment)
                val searchView = it.actionView as SearchView
                searchView.queryHint = getString(R.string.search_apps)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String) = false

                    override fun onQueryTextChange(newText: String): Boolean {
                        lifecycleScope.launch {
                            searchText = newText
                            refreshList()
                        }
                        return true
                    }
                })
            }
        }
        return super.onMenuItemClick(item)
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
            setDisplayCategory(CATEGORY_BOTH)
            needsToHideProgressBar = true
            refreshList()
            binding.applockPickRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = preferencesAdapter
                layoutAnimation = AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.preference_layout_fall_down
                )
            }
            preferencesAdapter.restoreAndObserveScrollPosition(binding.applockPickRecycler)
            onScreenChanged(
                preferencesAdapter.currentScreen,
                preferencesAdapter.isInSubScreen()
            )
            preferencesAdapter.onScreenChangeListener = this@AppLockPickFragment
        }
    }

    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        // To prevent a large space on tool bar.
        rootbinding.appBar.setExpanded(false /*expanded*/)
        // To prevent user expanding the collapsing tool bar view.
        ViewCompat.setNestedScrollingEnabled(binding.applockPickRecycler, false)
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        // We keep the collapsed status after user cancel the search function.
        rootbinding.appBar.setExpanded(true /*expanded*/, true)
        // Allow user to expand the tool bar view.
        ViewCompat.setNestedScrollingEnabled(binding.applockPickRecycler, true)
        return true
    }

    /**
     * Set the type of apps that should be displayed in the list.
     * Defaults to [CATEGORY_USER_ONLY].
     *
     * @param category one of [CATEGORY_SYSTEM_ONLY],
     * [CATEGORY_USER_ONLY], [CATEGORY_BOTH]
     */
    private fun setDisplayCategory(category: Int) {
        lifecycleScope.launch {
            displayCategory = category
        }
    }

    /**
     * Set a custom filter to filter out items from the list.
     *
     * @param customFilter a function that takes a [PackageInfo] and
     * returns a [Boolean] indicating whether to show the item or not.
     */
    fun setCustomFilter(customFilter: ((packageInfo: PackageInfo) -> Boolean)) {
        lifecycleScope.launch {
            packageFilter = customFilter
        }
    }

    private fun refreshList() {
        lifecycleScope.launchWhenCreated {
            val list = withContext(Dispatchers.Default) {
                packageList.filter {
                    when (displayCategory) {
                        CATEGORY_SYSTEM_ONLY -> it.applicationInfo.isSystemApp()
                        CATEGORY_USER_ONLY -> !it.applicationInfo.isSystemApp()
                        else -> true
                    } && getLabel(it).contains(searchText, true) && packageFilter(it)
                }.sortedWith(packageComparator).map { appInfofromPackage(it) }
            }

            if (viewModel.appList == null ||
                // Compares the old list with the new one to make sure we're updating the adapter only when is necessary
                (viewModel.appList != null && list.map { it.packageName } != viewModel.appList!!.map { it.packageName })) {
                viewModel.updateApps(binding.applockPickRecycler, list)
            }
            if (needsToHideProgressBar) {
                binding.appLoading.visibility = View.GONE
                needsToHideProgressBar = false
            }
        }
    }

    private fun appInfofromPackage(packageInfo: PackageInfo): AppInfo =
        AppInfo(
            packageInfo.packageName,
            getLabel(packageInfo),
            packageInfo.applicationInfo.loadIcon(packageManager),
            packageInfo.applicationInfo.category
        )

    private fun getLabel(packageInfo: PackageInfo) =
        packageInfo.applicationInfo.loadLabel(packageManager).toString()

    override fun getToolbarId(): Int {
        return com.android.settingslib.collapsingtoolbar.R.id.action_bar
    }

    override fun onDestroy() {
        preferencesAdapter.onScreenChangeListener = null
        binding.applockPickRecycler.adapter = null
        super.onDestroy()
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        binding.applockPickRecycler.scheduleLayoutAnimation()
        setHasOptionsMenu(subScreen)
    }

    override fun setTitle(title: CharSequence?) {
        rootbinding.collapsingToolbar.title = title
        super.setTitle(title)
    }

    data class AppInfo(
        val packageName: String,
        val label: String,
        val icon: Drawable,
        val category: Int,
    )

    companion object {

        const val CATEGORY_SYSTEM_ONLY = 0
        const val CATEGORY_USER_ONLY = 1
        const val CATEGORY_BOTH = 2

        fun newInstance(
            title: CharSequence?,
            mSectionNavigationController: CustomizationSectionController.CustomizationSectionNavigationController,
            callback: PickerCallback,
            customFilter: ((packageInfo: PackageInfo) -> Boolean)
        ): AppLockPickFragment {
            val fragment = AppLockPickFragment()
            fragment.arguments = createArguments(title)
            fragment.mSectionNavigationController = mSectionNavigationController
            fragment.callback = callback
            fragment.setCustomFilter(customFilter)
            return fragment
        }
    }

    interface PickerCallback {
        /**
         * A list which is used to compare target object's
         * existence to determine it's current state
         */
        val sourceComparator: List<String>

        /**
         * Callback method usued to apply custom unit for each check change
         * @param appInfo - Used to get Application's packageInfo and/or label
         * @param isChecked - Apply the callback on a per-state basis
         */
        fun onAppChanged(appInfo: AppInfo, isChecked: Boolean)
    }
}