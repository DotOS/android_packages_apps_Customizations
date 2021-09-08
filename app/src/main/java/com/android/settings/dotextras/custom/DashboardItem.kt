/*
 * Copyright (C) 2021 The dotOS Project
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
package com.android.settings.dotextras.custom

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class DashboardItem(
    val card_title: String,
    val target_fragment: Fragment,
    val display_fragment: Fragment,
) {
    var longCard: Boolean = false
    var standalone: Boolean = false
    var secured: Boolean = false
    var startActivity: AppCompatActivity? = null
}