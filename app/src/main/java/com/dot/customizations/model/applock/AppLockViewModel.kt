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

import android.app.AppLockManager
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.model.extras.fragmentPreference
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.*
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem

class AppLockViewModel(app: Application) : AndroidViewModel(app) {

    init {
        Preference.Config.dialogBuilderFactory = { context ->
            AlertDialog.Builder(
                context,
                com.android.settingslib.R.style.Theme_AlertDialog_SettingsLib
            )
        }
    }

    var callback: AppLockPickFragment.PickerCallback? = null
    var appList: List<AppLockPickFragment.AppInfo>? = null

    var navigationController:
            CustomizationSectionController.CustomizationSectionNavigationController? = null
        set(value) {
            field = value
            pickerPreferenceAdapter = PreferencesAdapter(createPickScreen(getApplication()))
        }

    private val appLockManager: AppLockManager = app.getSystemService(AppLockManager::class.java)

    var pickerPreferenceAdapter = PreferencesAdapter(createPickScreen(getApplication()))

    var settingsPreferencesAdapter = PreferencesAdapter(createSettingsScreen(getApplication()))

    fun updateApps(recyclerView: RecyclerView, newList: List<AppLockPickFragment.AppInfo>) {
        appList = newList
        if (recyclerView.adapter is PreferencesAdapter) {
            (recyclerView.adapter as PreferencesAdapter).setRootScreen(createPickScreen(getApplication()))
            pickerPreferenceAdapter = recyclerView.adapter as PreferencesAdapter
        } else {
            pickerPreferenceAdapter = PreferencesAdapter(createPickScreen(getApplication()))
            recyclerView.adapter = pickerPreferenceAdapter
        }
    }

    fun createSettingsScreen(context: Context) = screen(context) {
        title = context.getString(R.string.app_lock_title)
        navigationController?.let {
            val whiteListedPackages = context.resources.getStringArray(
                com.android.internal.R.array.config_appLockAllowedSystemApps
            )
            fragmentPreference(it,
                AppLockPickFragment.newInstance(
                    context.getString(R.string.app_lock_packages_title),
                    object : AppLockPickFragment.PickerCallback {
                        override val sourceComparator: List<String>
                            get() = appLockManager.packages

                        override fun onAppChanged(
                            appInfo: AppLockPickFragment.AppInfo,
                            isChecked: Boolean
                        ) {
                            if (isChecked) {
                                appLockManager.addPackage(appInfo.packageName)
                            } else {
                                appLockManager.removePackage(appInfo.packageName)
                            }
                        }

                    }) { app ->
                    !app.applicationInfo.isSystemApp() || whiteListedPackages.contains(app.packageName)
                }).apply {
                title = context.getString(R.string.app_lock_packages_title)
                summary = getAppLockSummary(context)
            }
            fragmentPreference(it,
                AppLockPickFragment.newInstance(
                    context.getString(R.string.app_lock_notifications_title),
                    object : AppLockPickFragment.PickerCallback {
                        override val sourceComparator: List<String>
                            get() = appLockManager.packagesWithSecureNotifications

                        override fun onAppChanged(
                            appInfo: AppLockPickFragment.AppInfo,
                            isChecked: Boolean
                        ) {
                            appLockManager.setSecureNotification(appInfo.packageName, isChecked)
                        }

                    }) { app ->
                    appLockManager.packages.contains(app.packageName)
                }).apply {
                title = context.getString(R.string.app_lock_notifications_title)
                summaryDisabledRes = R.string.app_lock_notifications_disabled_summary
                summaryRes = R.string.app_lock_notifications_summary
                enabled = appLockManager.packages.isNotEmpty()
            }
        }

        singleChoice("applocktimeout", arrayListOf(
            SelectionItem("250", context.getString(R.string.app_lock_timeout_instant)),
            SelectionItem("5000", context.getString(R.string.app_lock_timeout_5s)),
            SelectionItem("10000", context.getString(R.string.app_lock_timeout_10s)),
            SelectionItem("30000", context.getString(R.string.app_lock_timeout_30s)),
            SelectionItem("60000", context.getString(R.string.app_lock_timeout_1m)),
            SelectionItem("300000", context.getString(R.string.app_lock_timeout_5m)),
            SelectionItem("600000", context.getString(R.string.app_lock_timeout_10m)),
            SelectionItem("1800000", context.getString(R.string.app_lock_timeout_30m)),
        )) {
            title = context.getString(R.string.app_lock_timeout_title)
            initialSelection = appLockManager.timeout.toString()
            onSelectionChange {
                appLockManager.timeout = it.toLong()
                true
            }
        }

        switch("applockbiometrics") {
            titleRes = R.string.app_lock_biometrics_allowed_title
            defaultValue = appLockManager.isBiometricsAllowed
            onCheckedChange {
                appLockManager.isBiometricsAllowed = it
                true
            }
        }

        pref("applockfooter") {
            summaryRes = R.string.app_lock_footer
            enabled = false
        }
    }


    private fun createPickScreen(context: Context) = screen(context) {
        title = context.getString(R.string.app_lock_packages_title)
        appList?.let {
            appCategory(ApplicationInfo.CATEGORY_GAME, context.getString(R.string.app_category_games))
            appCategory(ApplicationInfo.CATEGORY_SOCIAL, context.getString(R.string.app_category_social))
            appCategory(ApplicationInfo.CATEGORY_PRODUCTIVITY, context.getString(R.string.app_category_prod))
            appCategory(ApplicationInfo.CATEGORY_MAPS, context.getString(R.string.app_category_maps))
            var addHeader = true
            for (app in it) {
                if (app.category != ApplicationInfo.CATEGORY_GAME &&
                    app.category != ApplicationInfo.CATEGORY_SOCIAL &&
                    app.category != ApplicationInfo.CATEGORY_PRODUCTIVITY &&
                    app.category != ApplicationInfo.CATEGORY_MAPS) {
                    if (addHeader) {
                        categoryHeader("others") {
                            title = context.getString(R.string.app_category_others)
                        }
                        // Add only once, only if there's any app that fits the description
                        addHeader = false
                    }
                    switch(app.packageName + (if (callback!!.sourceComparator == appLockManager.packages) "_apps" else "_notification")) {
                        title = app.label
                        summary = app.packageName
                        icon = app.icon
                        defaultValue = callback!!.sourceComparator.map { packageName -> packageName }.contains(app.packageName)
                        onCheckedChange { checked ->
                            callback?.onAppChanged(app, checked)
                            true
                        }
                    }
                }
            }
        }
    }

    private fun getAppLockSummary(context: Context): String {
        return if (appLockManager.packages.isNotEmpty()) {
            if (appLockManager.packages.size == 1)
                context.getString(R.string.app_lock_summary_singular)
            else
                context.getString(R.string.app_lock_summary_plural, appLockManager.packages.size)
        } else context.getString(R.string.app_lock_summary)
    }

    private fun PreferenceScreen.Builder.appCategory(category: Int, mTitle: CharSequence) {
        appList!!.let {
            for (app in it) {
                if (app.category == category) {
                    categoryHeader(mTitle.toString()) {
                        title = mTitle
                    }
                    for (appcat in it) {
                        if (appcat.category == category) {
                            switch(appcat.packageName + (if (callback!!.sourceComparator == appLockManager.packages) "_apps" else "_notification")) {
                                title = appcat.label
                                summary = appcat.packageName
                                icon = appcat.icon
                                defaultValue = callback!!.sourceComparator.map { packageName -> packageName }.contains(appcat.packageName)
                                onCheckedChange { checked ->
                                    callback?.onAppChanged(appcat, checked)
                                    true
                                }
                            }
                        }
                    }
                    break
                }
            }
        }
    }

}