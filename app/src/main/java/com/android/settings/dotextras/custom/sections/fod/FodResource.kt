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
package com.android.settings.dotextras.custom.sections.fod

import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import java.io.Serializable

typealias onFodResourceApplied = ((drawable: Drawable?) -> Unit)?
typealias onFodAnimApplied = ((anim: AnimationDrawable?) -> Unit)?

class FodResource(val resource: String, val id: Int) : Serializable {
    var selected = false
    var listener: onFodResourceApplied = null
    var listenerAnim: onFodAnimApplied = null
    var animation: String? = null
}