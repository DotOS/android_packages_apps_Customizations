/*
 * Copyright (C) 2020 The dotOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dotextras.custom.sections.fod

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView

class FodAnimationAdapter(
    private val featureManager: FeatureManager,
    private val items: ArrayList<FodResource>,
) :
    RecyclerView.Adapter<FodAnimationAdapter.ViewHolder>() {

    private val ANIMATION_STYLES_NAMES = arrayOf(
        "fod_miui_normal_recognizing_anim",
        "fod_miui_aod_recognizing_anim",
        "fod_miui_aurora_recognizing_anim",
        "fod_miui_aurora_cas_recognizing_anim",
        "fod_miui_light_recognizing_anim",
        "fod_miui_pop_recognizing_anim",
        "fod_miui_pulse_recognizing_anim",
        "fod_miui_pulse_recognizing_white_anim",
        "fod_miui_rhythm_recognizing_anim",
        "fod_miui_star_cas_recognizing_anim",
        "fod_op_cosmos_recognizing_anim",
        "fod_op_energy_recognizing_anim",
        "fod_op_mclaren_recognizing_anim",
        "fod_op_ripple_recognizing_anim",
        "fod_op_scanning_recognizing_anim",
        "fod_op_stripe_recognizing_anim",
        "fod_op_wave_recognizing_anim",
        "fod_pureview_dna_recognizing_anim",
        "fod_pureview_future_recognizing_anim",
        "fod_pureview_halo_ring_recognizing_anim",
        "fod_pureview_molecular_recognizing_anim",
        "fod_rog_fusion_recognizing_anim",
        "fod_rog_pulsar_recognizing_anim",
        "fod_rog_supernova_recognizing_anim")

    override fun getItemCount(): Int = items.size

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
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.fodIcon)
        holder.fodLayout.setOnClickListener {
            featureManager.System().setInt(featureManager.System().FOD_ANIM, fodIcon.id)
            select(position)
            updateSelection(fodIcon, holder, position)
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
            fodIcon.listenerAnim?.invoke(getAnimationPreview(holder.itemView.context,
                ANIMATION_STYLES_NAMES[position]) as AnimationDrawable?)
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
        return ResourceHelper.getDrawable(context,
            ResourceHelper.getFodAnimationPackage(context),
            drawableName)
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