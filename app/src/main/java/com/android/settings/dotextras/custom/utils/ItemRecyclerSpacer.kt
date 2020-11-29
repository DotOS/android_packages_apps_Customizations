package com.android.settings.dotextras.custom.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class ItemRecyclerSpacer(private val left: Float, private val pos: Int, private val right: Boolean) : RecyclerView.ItemDecoration() {
    private var position: Int = pos

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == this.position) {

            if (right) outRect.right = left.roundToInt()
            else outRect.left = left.roundToInt()
        }
    }
}