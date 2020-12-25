package com.android.settings.dotextras.custom.sections.fod

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.CompatResources
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.FeatureManager
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class FodAnimationAdapter(
    private val featureManager: FeatureManager,
    private val items: ArrayList<FodResource>
) :
    RecyclerView.Adapter<FodAnimationAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_fod_animation,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fodIcon: FodResource = items[position]
        fodIcon.selected =
            featureManager.System().getInt(featureManager.System().FOD_ANIM, 0) == fodIcon.id
        Glide.with(holder.fodIcon)
            .load(getAnimationPreview(holder.fodIcon.context, fodIcon.resource))
            .thumbnail(0.1f)
            .placeholder(android.R.color.transparent)
            .into(holder.fodIcon)
        holder.fodLayout.setOnClickListener {
            featureManager.System().setInt(featureManager.System().FOD_ANIM, fodIcon.id)
            select(position)
            updateSelection(fodIcon, holder)
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
        updateSelection(fodIcon, holder)
    }

    private fun updateSelection(fodIcon: FodResource, holder: ViewHolder) {
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

    private fun getAnimationPreview(context: Context, drawableName: String): Drawable? {
        return ResourceHelper.getDrawable(context, ResourceHelper.getFodAnimationPackage(context), drawableName)
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            items[i].selected = pos == i
            notifyItemChanged(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fodCard: MaterialCardView = view.findViewById(R.id.fodCard)
        val fodLayout: LinearLayout = view.findViewById(R.id.fodLayout)
        val fodIcon: ImageView = view.findViewById(R.id.fodIcon)
    }

}