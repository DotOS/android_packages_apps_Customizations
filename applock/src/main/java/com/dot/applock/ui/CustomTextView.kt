package com.dot.applock.ui

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CustomTextView(context: Context?, attrs: AttributeSet?) : AppCompatTextView(context!!, attrs) {

    private var watcherArray = ArrayList<TextWatcher>()

    override fun addTextChangedListener(watcher: TextWatcher?) {
        super.addTextChangedListener(watcher)
        if (watcher != null) {
            watcherArray.add(watcher)
        };
    }

    override fun removeTextChangedListener(watcher: TextWatcher?) {
        super.removeTextChangedListener(watcher)
        if (watcher != null && watcherArray.isNotEmpty()) {
            val i: Int = watcherArray.indexOf(watcher)
            if (i >= 0) {
                watcherArray.removeAt(i)
            }
        };
    }

    fun removeTextChangedListeners() {
        for (watcher in watcherArray) {
            removeTextChangedListener(watcher)
        }
    }
}