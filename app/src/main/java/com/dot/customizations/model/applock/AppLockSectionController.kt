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
import android.content.Context
import android.view.LayoutInflater
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.applock.AppLockSectionView

class AppLockSectionController(
    private val mSectionNavigationController:
    CustomizationSectionController.CustomizationSectionNavigationController
) : CustomizationSectionController<AppLockSectionView> {

    @SuppressLint("InflateParams")
    override fun createView(context: Context?): AppLockSectionView {
        val sectionView = LayoutInflater.from(context)
            .inflate(R.layout.applock_section_view, null) as AppLockSectionView
        val targetFragment =
            AppLockFragment.newInstance(
                context!!.getString(R.string.app_lock_title)
            )
        sectionView.setOnClickListener {
            BiometricVerification(mSectionNavigationController.rootFragment).setCallback(object :
                BiometricVerification.Callback {
                override fun onSuccess() {
                    mSectionNavigationController.navigateTo(targetFragment)
                }

                override fun onError(errString: CharSequence) {
                }

                override fun onFail() {
                }
            }).setTitle(context.getString(R.string.app_lock_title))
              .setSubtitle(context.getString(R.string.app_lock_summary))
              .authenticate()
        }
        return sectionView
    }

    override fun isAvailable(context: Context?): Boolean {
        return context?.let { BiometricVerification.isSupported(it) } == true
    }
}