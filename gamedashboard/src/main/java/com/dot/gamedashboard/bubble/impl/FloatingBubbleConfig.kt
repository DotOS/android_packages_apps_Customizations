package com.dot.gamedashboard.bubble.impl

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.dot.gamedashboard.R

/**
 * Floating configurations
 * Created by bijoysingh on 2/19/17.
 */
class FloatingBubbleConfig private constructor(builder: Builder) {
    val bubbleIcon: Drawable?
    val removeBubbleIcon: Drawable?
    val expandableView: View?
    val bubbleIconDp: Int
    val removeBubbleIconDp: Int
    val removeBubbleAlpha: Float
    val expandableColor: Int
    val triangleColor: Int
    val gravity: Int
    val paddingDp: Int
    val borderRadiusDp: Int
    val isPhysicsEnabled: Boolean

    class Builder {
        var bubbleIcon: Drawable? = null
        var removeBubbleIcon: Drawable? = null
        var expandableView: View? = null
        var bubbleIconDp = 64
        var removeBubbleIconDp = 64
        var expandableColor = Color.WHITE
        var triangleColor = Color.WHITE
        var gravity = Gravity.END
        var paddingDp = 4
        var borderRadiusDp = 4
        var removeBubbleAlpha = 1.0f
        var physicsEnabled = true
        fun bubbleIcon(`val`: Drawable?): Builder {
            bubbleIcon = `val`
            return this
        }

        fun removeBubbleIcon(`val`: Drawable?): Builder {
            removeBubbleIcon = `val`
            return this
        }

        fun expandableView(`val`: View?): Builder {
            expandableView = `val`
            return this
        }

        fun bubbleIconDp(`val`: Int): Builder {
            bubbleIconDp = `val`
            return this
        }

        fun removeBubbleIconDp(`val`: Int): Builder {
            removeBubbleIconDp = `val`
            return this
        }

        fun triangleColor(`val`: Int): Builder {
            triangleColor = `val`
            return this
        }

        fun expandableColor(`val`: Int): Builder {
            expandableColor = `val`
            return this
        }

        fun build(): FloatingBubbleConfig {
            return FloatingBubbleConfig(this)
        }

        fun gravity(`val`: Int): Builder {
            gravity = `val`
            if (gravity == Gravity.CENTER || gravity == Gravity.CENTER_VERTICAL || gravity == Gravity.CENTER_HORIZONTAL) {
                gravity = Gravity.CENTER_HORIZONTAL
            } else if (gravity == Gravity.TOP ||
                gravity == Gravity.BOTTOM
            ) {
                gravity = Gravity.END
            }
            return this
        }

        fun paddingDp(`val`: Int): Builder {
            paddingDp = `val`
            return this
        }

        fun borderRadiusDp(`val`: Int): Builder {
            borderRadiusDp = `val`
            return this
        }

        fun physicsEnabled(`val`: Boolean): Builder {
            physicsEnabled = `val`
            return this
        }

        fun removeBubbleAlpha(`val`: Float): Builder {
            removeBubbleAlpha = `val`
            return this
        }
    }

    companion object {
        fun getDefaultBuilder(context: Context?): Builder {
            return Builder()
                .bubbleIcon(ContextCompat.getDrawable(context!!, R.drawable.ic_games))
                .removeBubbleIcon(ContextCompat.getDrawable(context, R.drawable.ic_close))
                .bubbleIconDp(64)
                .removeBubbleIconDp(64)
                .paddingDp(4)
                .removeBubbleAlpha(1.0f)
                .physicsEnabled(true)
                .expandableColor(Color.WHITE)
                .triangleColor(Color.WHITE)
                .gravity(Gravity.END)
        }

        fun getDefault(context: Context?): FloatingBubbleConfig {
            return getDefaultBuilder(context).build()
        }
    }

    init {
        bubbleIcon = builder.bubbleIcon
        removeBubbleIcon = builder.removeBubbleIcon
        expandableView = builder.expandableView
        bubbleIconDp = builder.bubbleIconDp
        removeBubbleIconDp = builder.removeBubbleIconDp
        expandableColor = builder.expandableColor
        triangleColor = builder.triangleColor
        gravity = builder.gravity
        paddingDp = builder.paddingDp
        borderRadiusDp = builder.borderRadiusDp
        isPhysicsEnabled = builder.physicsEnabled
        removeBubbleAlpha = builder.removeBubbleAlpha
    }
}