/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.settings.dotextras.custom.sections.clock

/**
 * Interface for a class that can retrieve Themes from the system.
 */

interface ClockProvider {
    /**
     * Returns whether clockfaces are available in the current setup.
     */
    val isAvailable: Boolean

    /**
     * Retrieve the available clockfaces.
     * @param callback called when the clockfaces have been retrieved (or immediately if cached)
     * @param reload whether to reload clockfaces if they're cached.
     */
    fun fetch(callback: OptionsFetchedListener, reload: Boolean)
}