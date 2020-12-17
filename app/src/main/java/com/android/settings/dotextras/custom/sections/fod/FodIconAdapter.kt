package com.android.settings.dotextras.custom.sections.fod

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.FeatureManager
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class FodIconAdapter(
    private val featureManager: FeatureManager,
    private val items: ArrayList<FodResource>
) :
    RecyclerView.Adapter<FodIconAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_fod_icon,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fodIcon: FodResource = items[position]
        val shouldSelect =
            featureManager.System().getInt(featureManager.System().FOD_ICON, 0) == fodIcon.id
        if (shouldSelect) fodIcon.selected = true
        Glide.with(holder.fodIcon)
            .load(ResourceHelper.getDrawable(holder.fodIcon.context, "com.android.systemui", fodIcon.resource))
            .into(holder.fodIcon)
        holder.fodLayout.setOnClickListener {
            select(position)
            updateSelection(fodIcon, holder, position)
            featureManager.System().setInt(featureManager.System().FOD_ICON, fodIcon.id)
        }
        holder.fodLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val scaleDownX = ObjectAnimator.ofFloat(
                        holder.fodCard,
                        "scaleX", 0.9f
                    )
                    val scaleDownY = ObjectAnimator.ofFloat(
                        holder.fodCard,
                        "scaleY", 0.9f
                    )
                    scaleDownX.duration = 200
                    scaleDownY.duration = 200
                    val scaleDown = AnimatorSet()
                    scaleDown.play(scaleDownX).with(scaleDownY)
                    scaleDown.start()
                }
                MotionEvent.ACTION_UP -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
                MotionEvent.ACTION_CANCEL -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
            }
            false
        }
        updateSelection(fodIcon, holder, position)
    }

    private fun updateSelection(fodIcon: FodResource, holder: ViewHolder, position: Int) {
        val accentColor: Int = ResourceHelper.getAccent(holder.fodLayout.context)
        if (fodIcon.selected) {
            holder.fodLayout.setBackgroundColor(accentColor)
            holder.fodLayout.invalidate(true)
        } else {
            holder.fodLayout.setBackgroundColor(
                ContextCompat.getColor(
                    holder.fodLayout.context,
                    android.R.color.transparent
                )
            )
            holder.fodLayout.invalidate(true)
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i);
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fodCard: MaterialCardView = view.findViewById(R.id.fodCard)
        val fodLayout: LinearLayout = view.findViewById(R.id.fodLayout)
        val fodIcon: ImageView = view.findViewById(R.id.fodIcon)
    }

}