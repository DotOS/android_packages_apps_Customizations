package com.android.settings.dotextras.custom.sections.clock

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class ClockfacePreviewRecyclerAdapter(
    private val clockManager: ClockManager,
    private val items: ArrayList<ClockfaceCompat>,
) :
    RecyclerView.Adapter<ClockfacePreviewRecyclerAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_clockface_preview,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clockfaceCompat: ClockfaceCompat = items[position]
        val shouldSelect = clockfaceCompat.clockface.isActive(clockManager)
        if (shouldSelect) clockfaceCompat.selected = true
        holder.accentButton.imageTintList = ColorStateList.valueOf(ResourceHelper.getAccent(holder.itemView.context))
        clockfaceCompat.clockface.bindPreviewTile(holder.itemView)
        holder.clockfaceChip.text = clockfaceCompat.clockface.getTitle()
        holder.clockfaceLayout.setOnClickListener {
            clockManager.apply(clockfaceCompat.clockface) {}
            select(position)
            updateSelection(clockfaceCompat, holder)
        }
        updateSelection(clockfaceCompat, holder)
    }

    private fun updateSelection(clockfaceCompat: ClockfaceCompat, holder: ViewHolder) {
        holder.clockfaceChip.isChecked = clockfaceCompat.selected
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i);
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val accentButton: AppCompatImageView = view.findViewById(R.id.clockface_button_accent)
        val clockfaceChip: Chip = view.findViewById(R.id.clockfaceChip)
        val clockfaceLayout: LinearLayout = view.findViewById(R.id.clockfaceLayout)
    }
}