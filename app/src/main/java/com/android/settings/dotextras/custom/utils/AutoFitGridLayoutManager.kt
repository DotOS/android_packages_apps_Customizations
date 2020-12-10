package com.android.settings.dotextras.custom.utils

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class AutoFitGridLayoutManager(
    context: Context,
    columnWidth: Float,
) : GridLayoutManager(context, 1) {
    private var mColumnWidth = 0
    private var mColumnWidthChanged = true

    init {
        setColumnWidth(checkedColumnWidth(context, columnWidth.toInt()))
    }

    private fun checkedColumnWidth(context: Context, columnWidth: Int): Int {
        var columnWidth = columnWidth
        if (columnWidth <= 0) {
            columnWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 48f,
                context.resources.displayMetrics
            ).toInt()
        }
        return columnWidth
    }

    fun setColumnWidth(newColumnWidth: Int) {
        if (newColumnWidth > 0 && newColumnWidth != mColumnWidth) {
            mColumnWidth = newColumnWidth
            mColumnWidthChanged = true
        }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        val width = width
        val height = height
        if (mColumnWidthChanged && mColumnWidth > 0 && width > 0 && height > 0) {
            val totalSpace: Int
            totalSpace = if (orientation == RecyclerView.VERTICAL) {
                width - paddingRight - paddingLeft
            } else {
                height - paddingTop - paddingBottom
            }
            val spanCount = Math.max(1, totalSpace / mColumnWidth)
            setSpanCount(spanCount)
            mColumnWidthChanged = false
        }
        super.onLayoutChildren(recycler, state)
    }
}