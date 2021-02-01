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
package com.android.settings.dotextras.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.google.android.material.card.MaterialCardView
import java.util.*
import kotlin.math.roundToInt

class DashboardAdapter(
    private val items: ArrayList<DashboardItem>,
    private val fragmentManager: FragmentManager,
    private val activity: BaseActivity,
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_header_cust,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dashboardItem: DashboardItem = items[position]
        holder.title.text = dashboardItem.card_title
        holder.fragmentlayout.id += position + Random().nextInt()
        fragmentManager.beginTransaction().replace(
            holder.fragmentlayout.id,
            dashboardItem.display_fragment
        ).commit()
        holder.fragmentlayout.setOnClickListener {
            activity.getNestedScroll().smoothScrollTo(0, 0, 200)
            activity.expandToolbar()
            activity.setTitle(dashboardItem.card_title)
            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                .replace(R.id.frameContent, dashboardItem.target_fragment, dashboardItem.card_title)
                .addToBackStack(dashboardItem.card_title)
                .commit()
        }
        holder.fragmentlayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val scaleDownX = ObjectAnimator.ofFloat(
                        holder.card,
                        "scaleX", 0.9f
                    )
                    val scaleDownY = ObjectAnimator.ofFloat(
                        holder.card,
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
                        holder.card, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.card, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
                MotionEvent.ACTION_CANCEL -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.card, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.card, "scaleY", 1f
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
        if (dashboardItem.longCard) {
            holder.fragmentlayout.minimumHeight =
                holder.fragmentlayout.resources.getDimension(R.dimen.large_card_height).roundToInt()
        } else {
            holder.fragmentlayout.minimumHeight =
                holder.fragmentlayout.resources.getDimension(R.dimen.default_card_height)
                    .roundToInt()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.card_title)
        val card: MaterialCardView = view.findViewById(R.id.card_main)
        val fragmentlayout: LinearLayout = view.findViewById(R.id.card_fragment_container)
    }
}