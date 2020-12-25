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

abstract class BaseClockManager(private val mClockProvider: ClockProvider?) {

    val isAvailable: Boolean
        get() = mClockProvider!!.isAvailable

    fun apply(option: Clockface, callback: onHandleCallback) {
        handleApply(option, callback)
    }

    fun fetchOptions(
        callback: OptionsFetchedListener,
        reload: Boolean
    ) {
        mClockProvider!!.fetch(callback, false)
    }

    /** Returns the ID of the current clock face, which may be null for the default clock face.  */
    val currentClock: String
        get() = lookUpCurrentClock()

    /**
     * Implement to apply the clock picked by the user for [BaseClockManager.apply].
     *
     * @param option Clock option, containing ID of the clock, that the user picked.
     * @param callback Report success and failure.
     */
    protected abstract fun handleApply(option: Clockface?, callback: onHandleCallback)

    /**
     * Implement to look up the current clock face for [BaseClockManager.getCurrentClock].
     *
     * @return ID of current clock. Can be null for the default clock face.
     */
    abstract fun lookUpCurrentClock(): String
}