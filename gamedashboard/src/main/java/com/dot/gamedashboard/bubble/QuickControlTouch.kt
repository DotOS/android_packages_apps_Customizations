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
package com.dot.gamedashboard.bubble

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.text.method.Touch
import android.view.MotionEvent
import android.view.View
import com.dot.gamedashboard.R
import com.dot.ui.utils.ObjectToolsAnimator

class QuickControlTouch private constructor(builder: Builder): View.OnTouchListener {

    private var sizeX: Int
    private var sizeY: Int
    private var bubbleView: View
    private var bubblePill: View
    private var touchStartTime: Long = 0
    private var touchStopTime: Long = 0
    private var callback: TouchCallback
    private var isHidden = false

    init {
        sizeX = builder.sizeX
        sizeY = builder.sizeY
        callback = builder.callback!!
        bubbleView = builder.bubbleView!!
        bubblePill = bubbleView.findViewById(R.id.qc_drag)
    }

    fun forceHide() {
        ObjectAnimator.ofFloat(bubbleView, "translationX", 165f).apply {
            duration = 100
            start()
        }
        ObjectToolsAnimator.animate(bubblePill, "alpha", 0f, 1f, 100)
        ObjectToolsAnimator.animate(bubbleView.findViewById(R.id.qc_card), "alpha", 1f, 0.2f, 100)
        callback.onHide()
        isHidden = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        touchStartTime = 0
        touchStopTime = 0
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartTime = System.currentTimeMillis()
                isHidden = if (!isHidden) {
                    ObjectAnimator.ofFloat(bubbleView, "translationX", 165f).apply {
                        duration = 700
                        start()
                    }
                    ObjectToolsAnimator.animate(bubblePill, "alpha", 0f, 1f, 700)
                    ObjectToolsAnimator.animate(bubbleView.findViewById(R.id.qc_card), "alpha", 1f, 0.2f, 700)
                    callback.onHide()
                    true
                } else {
                    ObjectAnimator.ofFloat(bubbleView, "translationX", 0f).apply {
                        duration = 700
                        start()
                    }
                    ObjectToolsAnimator.animate(bubblePill, "alpha", 1f, 0f, 700)
                    ObjectToolsAnimator.animate(bubbleView.findViewById(R.id.qc_card), "alpha", 0.2f, 1f, 700)
                    callback.onShow()
                    false
                }
            }
            MotionEvent.ACTION_UP -> {
                touchStopTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchStopTime - touchStartTime >= TOUCH_CLICK_TIME) {
                    isHidden = if (!isHidden) {
                        ObjectToolsAnimator.animate(bubbleView, "translationX", bubbleView.x, (bubbleView.x / 4))
                        ObjectToolsAnimator.animate(bubbleView, "alpha", 1f, 0.2f)
                        true
                    } else {
                        ObjectToolsAnimator.animate(bubbleView, "translationX", bubbleView.x, (bubbleView.x * 4))
                        ObjectToolsAnimator.animate(bubbleView, "alpha", 0.2f, 1f)
                        false
                    }
                }
            }
        }
        return true
    }

    class Builder {
        var sizeX = 0
        var sizeY = 0
        var bubbleView: View? = null
        var callback: TouchCallback? = null

        fun sizeX(`val`: Int): Builder {
            sizeX = `val`
            return this
        }

        fun sizeY(`val`: Int): Builder {
            sizeY = `val`
            return this
        }

        fun callback(`val`: TouchCallback): Builder {
            callback = `val`
            return this
        }

        fun bubbleView(`val`: View?): Builder {
            bubbleView = `val`
            return this
        }

        fun build(): QuickControlTouch {
            return QuickControlTouch(this)
        }
    }

    companion object {
        const val TOUCH_CLICK_TIME = 350
    }

    interface TouchCallback {

        fun onShow()
        fun onHide()

    }
}