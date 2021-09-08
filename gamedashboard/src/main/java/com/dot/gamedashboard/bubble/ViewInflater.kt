package com.dot.gamedashboard.bubble

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.dot.gamedashboard.R

class ViewInflater(var context: Context) {

    init {
        context = ContextThemeWrapper(context, R.style.AppTheme)
    }

    fun inflate(resID: Int): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater.inflate(resID, null)
    }
}