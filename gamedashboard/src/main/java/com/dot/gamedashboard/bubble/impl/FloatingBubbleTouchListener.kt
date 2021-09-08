package com.dot.gamedashboard.bubble.impl

/**
 * Floating bubble remove listener
 * Created by bijoysingh on 2/19/17.
 */
interface FloatingBubbleTouchListener {
    fun onDown(x: Float, y: Float)
    fun onTap(expanded: Boolean)
    fun onRemove()
    fun onMove(x: Float, y: Float)
    fun onUp(x: Float, y: Float)
}