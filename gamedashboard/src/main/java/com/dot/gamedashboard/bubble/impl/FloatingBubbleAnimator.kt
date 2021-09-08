package com.dot.gamedashboard.bubble.impl

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.WindowManager
import java.lang.Exception

/**
 * Animator
 * Created by bijoysingh on 2/19/17.
 */
class FloatingBubbleAnimator private constructor(builder: Builder) {
    private val bubbleView: View?
    private val bubbleParams: WindowManager.LayoutParams?
    private val windowManager: WindowManager?
    private val sizeX: Int
    private val sizeY: Int
    fun animate(
        x: Float,
        y: Float
    ) {
        val startX = bubbleParams!!.x.toFloat()
        val startY = bubbleParams.y.toFloat()
        val animator = ValueAnimator.ofInt(0, 5)
            .setDuration(ANIMATION_TIME.toLong())
        animator.addUpdateListener { valueAnimator ->
            try {
                val currentX = startX + (x - startX) *
                        valueAnimator.animatedValue as Int / ANIMATION_STEPS
                val currentY = startY + (y - startY) *
                        valueAnimator.animatedValue as Int / ANIMATION_STEPS
                bubbleParams.x = currentX.toInt()
                bubbleParams.x = Math.max(bubbleParams.x, 0)
                bubbleParams.x = Math.min(bubbleParams.x, sizeX - bubbleView!!.width)
                bubbleParams.y = currentY.toInt()
                bubbleParams.y = Math.max(bubbleParams.y, 0)
                bubbleParams.y = Math.min(bubbleParams.y, sizeY - bubbleView.width)
                windowManager!!.updateViewLayout(bubbleView, bubbleParams)
            } catch (exception: Exception) {
                Log.e(FloatingBubbleAnimator::class.java.simpleName, exception.message!!)
            }
        }
        animator.start()
    }

    class Builder {
        var bubbleView: View? = null
        var bubbleParams: WindowManager.LayoutParams? = null
        var windowManager: WindowManager? = null
        var sizeX = 0
        var sizeY = 0
        fun bubbleView(`val`: View?): Builder {
            bubbleView = `val`
            return this
        }

        fun bubbleParams(`val`: WindowManager.LayoutParams?): Builder {
            bubbleParams = `val`
            return this
        }

        fun windowManager(`val`: WindowManager?): Builder {
            windowManager = `val`
            return this
        }

        fun sizeX(`val`: Int): Builder {
            sizeX = `val`
            return this
        }

        fun sizeY(`val`: Int): Builder {
            sizeY = `val`
            return this
        }

        fun build(): FloatingBubbleAnimator {
            return FloatingBubbleAnimator(this)
        }
    }

    companion object {
        private const val ANIMATION_TIME = 100
        private const val ANIMATION_STEPS = 5
    }

    init {
        bubbleView = builder.bubbleView
        bubbleParams = builder.bubbleParams
        windowManager = builder.windowManager
        sizeX = builder.sizeX
        sizeY = builder.sizeY
    }
}