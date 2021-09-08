package com.dot.gamedashboard.bubble.impl

import android.util.Log

/**
 * Floating Bubble Logger
 * Created by bijoy on 1/6/17.
 */
class FloatingBubbleLogger {
    private var isDebugEnabled = false
    private var tag: String
    fun setTag(tag: String): FloatingBubbleLogger {
        this.tag = tag
        return this
    }

    fun setDebugEnabled(enabled: Boolean): FloatingBubbleLogger {
        isDebugEnabled = enabled
        return this
    }

    fun log(message: String?) {
        if (isDebugEnabled) {
            Log.d(tag, message!!)
        }
    }

    fun log(message: String?, throwable: Throwable?) {
        if (isDebugEnabled) {
            Log.e(tag, message, throwable)
        }
    }

    init {
        tag = FloatingBubbleLogger::class.java.simpleName
    }
}