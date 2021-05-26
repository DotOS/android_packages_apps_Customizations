/*
 * Copyright (C) 2020 The dotOS Project
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
package com.dot.applock

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd

object ObjectToolsAnimator {
    fun animate(v: View, direction: String?, start: Long, end: Long, duration: Long) {
        val mover = ObjectAnimator.ofFloat(v, direction, start.toFloat(), end.toFloat())
        mover.duration = duration
        mover.start()
    }

    fun animate(v: View, direction: String?, start: Long, end: Long) {
        val mover = ObjectAnimator.ofFloat(v, direction, start.toFloat(), end.toFloat())
        mover.duration = 500
        mover.start()
    }

    fun rotate(v: View, from: Float, to: Float) {
        val mover = ObjectAnimator.ofFloat(v, "rotation", from, to)
        mover.duration = 500
        mover.start()
    }

    fun gone(v: View, duration: Long) {
        v.visibility = View.VISIBLE
        v.alpha = 1f
        val mover = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f)
        mover.duration = duration
        mover.start()
        mover.doOnEnd {
            v.visibility = View.GONE
        }
    }

    fun hide(v: View, duration: Long) {
        v.visibility = View.VISIBLE
        v.alpha = 1f
        val mover = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f)
        mover.duration = duration
        mover.start()
        mover.doOnEnd {
            v.visibility = View.INVISIBLE
        }
    }

    fun show(v: View, duration: Long) {
        v.visibility = View.VISIBLE
        v.alpha = 0f
        val mover = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
        mover.duration = duration
        mover.start()
    }

    fun rotate(v: View, from: Float, to: Float, duration: Long) {
        val mover = ObjectAnimator.ofFloat(v, "rotation", from, to)
        mover.duration = duration
        mover.start()
    }
}