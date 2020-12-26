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
package com.android.settings.dotextras.custom.sections.cards

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.GLOBAL
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.PAGER
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.RGB
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SECURE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWIPE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWITCH
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.ColorSheetUtils
import com.android.settings.dotextras.custom.views.ColorSheet
import com.android.settings.dotextras.system.FeatureManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

class ContextCardsAdapter(
    private val contentResolver: ContentResolver,
    private val items: ArrayList<ContextCards>,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            SWITCH -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feature_card_switch, parent, false)
                return ViewHolder(view)
            }
            SWIPE -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feature_card_slider, parent, false)
                return ViewHolderSwipe(view)
            }
            PAGER -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feature_card_swipe, parent, false)
                return ViewHolderPager(view)
            }
            RGB -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feature_card_rgb, parent, false)
                return ViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feature_card_switch, parent, false)
                return ViewHolder(view)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contextCard: ContextCards = items[position]
        val featureManager = FeatureManager(contentResolver)
        when (getItemViewType(position)) {
            SWITCH -> {
                val isByDefault =
                    if (contextCard.isCardChecked) featureManager.Values().ON else featureManager.Values().OFF
                when (contextCard.featureType) {
                    SECURE -> contextCard.isCardChecked = featureManager.Secure()
                        .getInt(
                            contextCard.feature,
                            isByDefault
                        ) == featureManager.Values().ON
                    GLOBAL -> contextCard.isCardChecked = featureManager.Global()
                        .getInt(
                            contextCard.feature,
                            isByDefault
                        ) == featureManager.Values().ON
                    SYSTEM -> contextCard.isCardChecked = featureManager.System()
                        .getInt(
                            contextCard.feature,
                            isByDefault
                        ) == featureManager.Values().ON
                }
                (holder as ViewHolder).cardIcon.setImageResource(contextCard.iconID)
                holder.cardIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        holder.cardIcon.context,
                        contextCard.accentColor
                    )
                )
                holder.cardTitle.text = contextCard.title
                holder.cardTitle.isSelected = true
                holder.cardTitle.setTextColor(
                    ContextCompat.getColor(
                        holder.cardTitle.context,
                        contextCard.accentColor
                    )
                )
                holder.cardSubtitle.text = contextCard.subtitle
                holder.cardSubtitle.isSelected = true
                holder.cardSubtitle.setTextColor(
                    ContextCompat.getColor(
                        holder.cardSubtitle.context,
                        contextCard.accentColor
                    )
                )
                if (contextCard.summary == null)
                    holder.cardSummary.visibility = View.INVISIBLE
                else
                    holder.cardSummary.text = contextCard.summary
                holder.cardClickable.setOnClickListener {
                    contextCard.listener?.invoke(if (!contextCard.isCardChecked) featureManager.Values().ON else featureManager.Values().OFF)
                    when (contextCard.featureType) {
                        SECURE -> featureManager.Secure().setInt(
                            contextCard.feature,
                            if (!contextCard.isCardChecked) featureManager.Values().ON else featureManager.Values().OFF
                        )
                        GLOBAL -> featureManager.Global().setInt(
                            contextCard.feature,
                            if (!contextCard.isCardChecked) featureManager.Values().ON else featureManager.Values().OFF
                        )
                        SYSTEM -> {
                            if (Settings.System.canWrite(holder.itemView.context)) {
                                featureManager.System().setInt(
                                    contextCard.feature,
                                    if (!contextCard.isCardChecked) featureManager.Values().ON else featureManager.Values().OFF
                                )
                            } else {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                intent.data =
                                    Uri.parse("package:" + holder.itemView.context.packageName)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(holder.itemView.context, intent, null)
                            }
                        }
                    }
                    contextCard.isCardChecked = !contextCard.isCardChecked
                    updateSwitchSelection(contextCard, holder)
                }
                holder.cardClickable.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val scaleDownX = ObjectAnimator.ofFloat(
                                holder.cardLayout,
                                "scaleX", 0.9f
                            )
                            val scaleDownY = ObjectAnimator.ofFloat(
                                holder.cardLayout,
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
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
                            )
                            scaleDownX2.duration = 200
                            scaleDownY2.duration = 200
                            val scaleDown2 = AnimatorSet()
                            scaleDown2.play(scaleDownX2).with(scaleDownY2)
                            scaleDown2.start()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            val scaleDownX2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
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
                updateSwitchSelection(contextCard, holder)
            }
            SWIPE -> {
                (holder as ViewHolderSwipe).cardSeek.max = contextCard.max
                holder.cardSeek.min = contextCard.min
                holder.cardSeek.progressTintList = ColorStateList.valueOf(
                    holder.cardSeek.context.getColor(
                        contextCard.accentColor
                    )
                )
                when (contextCard.featureType) {
                    SECURE -> contextCard.value =
                        featureManager.Secure().getInt(contextCard.feature, contextCard.default)
                    GLOBAL -> contextCard.value =
                        featureManager.Global().getInt(contextCard.feature, contextCard.default)
                    SYSTEM -> contextCard.value =
                        featureManager.System().getInt(contextCard.feature, contextCard.default)
                }
                holder.cardSeek.progress = contextCard.value
                holder.cardIcon.setImageResource(contextCard.iconID)
                holder.cardIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        holder.cardIcon.context,
                        contextCard.accentColor
                    )
                )
                holder.cardTitle.text = contextCard.title
                holder.cardTitle.isSelected = true
                holder.cardSubtitle.isSelected = true
                holder.cardTitle.setTextColor(
                    ContextCompat.getColor(
                        holder.cardTitle.context,
                        contextCard.accentColor
                    )
                )
                holder.cardSubtitle.text = contextCard.subtitle
                holder.cardSubtitle.setTextColor(
                    ContextCompat.getColor(
                        holder.cardSubtitle.context,
                        contextCard.accentColor
                    )
                )
                if (contextCard.summary == null)
                    holder.cardSummary.visibility = View.INVISIBLE
                else
                    holder.cardSummary.text = contextCard.summary
                holder.cardSeek.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        contextCard.listener?.invoke(progress)
                        updateSwipeSelection(contextCard, (holder as ViewHolderSwipe), progress)
                        when (contextCard.featureType) {
                            SECURE -> featureManager.Secure()
                                .setInt(contextCard.feature, progress)
                            GLOBAL -> featureManager.Global()
                                .setInt(contextCard.feature, progress)
                            SYSTEM -> {
                                if (Settings.System.canWrite((holder as ViewHolderSwipe).itemView.context)) {
                                    featureManager.System()
                                        .setInt(contextCard.feature, progress)
                                } else {
                                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                    intent.data =
                                        Uri.parse("package:" + (holder as ViewHolderSwipe).itemView.context.packageName)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity((holder as ViewHolderSwipe).itemView.context,
                                        intent,
                                        null)
                                }
                            }
                        }
                    }
                })
                holder.cardSeek.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val scaleDownX = ObjectAnimator.ofFloat(
                                holder.cardLayout,
                                "scaleX", 0.9f
                            )
                            val scaleDownY = ObjectAnimator.ofFloat(
                                holder.cardLayout,
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
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
                            )
                            scaleDownX2.duration = 200
                            scaleDownY2.duration = 200
                            val scaleDown2 = AnimatorSet()
                            scaleDown2.play(scaleDownX2).with(scaleDownY2)
                            scaleDown2.start()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            val scaleDownX2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
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
                updateSwipeSelection(contextCard, holder, contextCard.value)
            }
            PAGER -> {
                (holder as ViewHolderPager).cardPager.adapter = contextCard.pagerAdapter
                when (contextCard.featureType) {
                    SECURE -> holder.cardPager.currentItem =
                        featureManager.Secure()
                            .getInt(contextCard.feature, contextCard.default)
                    GLOBAL -> holder.cardPager.currentItem =
                        featureManager.Global()
                            .getInt(contextCard.feature, contextCard.default)
                    SYSTEM -> holder.cardPager.currentItem =
                        featureManager.System()
                            .getInt(contextCard.feature, contextCard.default)
                }
                holder.cardLeft.setOnClickListener {
                    holder.cardPager.setCurrentItem(
                        holder.cardPager.currentItem - 1,
                        true
                    )
                }
                holder.cardRight.setOnClickListener {
                    holder.cardPager.setCurrentItem(
                        holder.cardPager.currentItem + 1,
                        true
                    )
                }
                holder.cardIcon.setImageResource(contextCard.iconID)
                holder.cardIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        holder.cardIcon.context,
                        contextCard.accentColor
                    )
                )
                holder.cardTitle.text = contextCard.title
                holder.cardTitle.isSelected = true
                holder.cardTitle.setTextColor(
                    ContextCompat.getColor(
                        holder.cardTitle.context,
                        contextCard.accentColor
                    )
                )
                holder.cardApply.setOnClickListener {
                    val pos = holder.cardPager.currentItem
                    when (contextCard.featureType) {
                        SECURE -> featureManager.Secure().setInt(contextCard.feature, pos)
                        GLOBAL -> featureManager.Global().setInt(contextCard.feature, pos)
                        SYSTEM -> {
                            if (Settings.System.canWrite(holder.itemView.context))
                                featureManager.System().setInt(contextCard.feature, pos)
                            else {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                intent.data =
                                    Uri.parse("package:" + holder.itemView.context.packageName)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(holder.itemView.context,
                                    intent,
                                    null)
                            }
                        }
                    }
                    contextCard.listener?.invoke(pos)
                }
                holder.cardClickable.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val scaleDownX = ObjectAnimator.ofFloat(
                                holder.cardLayout,
                                "scaleX", 0.9f
                            )
                            val scaleDownY = ObjectAnimator.ofFloat(
                                holder.cardLayout,
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
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
                            )
                            scaleDownX2.duration = 200
                            scaleDownY2.duration = 200
                            val scaleDown2 = AnimatorSet()
                            scaleDown2.play(scaleDownX2).with(scaleDownY2)
                            scaleDown2.start()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            val scaleDownX2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
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
            }
            RGB -> {
                when (contextCard.featureType) {
                    SECURE -> contextCard.colorInt = featureManager.Secure()
                        .getInt(
                            contextCard.feature,
                            contextCard.defaultColor
                        )
                    GLOBAL -> contextCard.colorInt = featureManager.Global()
                        .getInt(
                            contextCard.feature,
                            contextCard.defaultColor
                        )
                    SYSTEM -> contextCard.colorInt = featureManager.System()
                        .getInt(
                            contextCard.feature,
                            contextCard.defaultColor
                        )
                }
                (holder as ViewHolder).cardIcon.setImageResource(contextCard.iconID)
                holder.cardTitle.text = contextCard.title
                holder.cardTitle.isSelected = true
                holder.cardSubtitle.text = contextCard.subtitle
                holder.cardSubtitle.isSelected = true
                if (contextCard.summary == null)
                    holder.cardSummary.visibility = View.INVISIBLE
                else
                    holder.cardSummary.text = contextCard.summary
                holder.cardClickable.setOnClickListener {
                    val colorSheet = ColorSheet()
                    colorSheet.colorPicker(holder.cardClickable.resources.getIntArray(R.array.materialColors),
                        onResetListener = {
                            contextCard.colorInt = contextCard.defaultColor
                            contextCard.colorListener?.invoke(contextCard.defaultColor)
                            when (contextCard.featureType) {
                                SECURE -> featureManager.Secure()
                                    .setInt(contextCard.feature, contextCard.defaultColor)
                                GLOBAL -> featureManager.Global()
                                    .setInt(contextCard.feature, contextCard.defaultColor)
                                SYSTEM -> {
                                    if (Settings.System.canWrite(holder.itemView.context)) {
                                        featureManager.System()
                                            .setInt(contextCard.feature, contextCard.defaultColor)
                                    } else {
                                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                        intent.data =
                                            Uri.parse("package:" + holder.itemView.context.packageName)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(holder.itemView.context, intent, null)
                                    }
                                }
                            }
                            updateRGBSection(contextCard, holder)
                        },
                        listener = { color ->
                            contextCard.colorInt = color
                            contextCard.colorListener?.invoke(color)
                            when (contextCard.featureType) {
                                SECURE -> featureManager.Secure().setInt(contextCard.feature, color)
                                GLOBAL -> featureManager.Global().setInt(contextCard.feature, color)
                                SYSTEM -> {
                                    if (Settings.System.canWrite(holder.itemView.context)) {
                                        featureManager.System().setInt(contextCard.feature, color)
                                    } else {
                                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                        intent.data =
                                            Uri.parse("package:" + holder.itemView.context.packageName)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(holder.itemView.context, intent, null)
                                    }
                                }
                            }
                            updateRGBSection(contextCard, holder)
                        }).show(contextCard.fragmentManager!!)
                }
                holder.cardClickable.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val scaleDownX = ObjectAnimator.ofFloat(
                                holder.cardLayout,
                                "scaleX", 0.9f
                            )
                            val scaleDownY = ObjectAnimator.ofFloat(
                                holder.cardLayout,
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
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
                            )
                            scaleDownX2.duration = 200
                            scaleDownY2.duration = 200
                            val scaleDown2 = AnimatorSet()
                            scaleDown2.play(scaleDownX2).with(scaleDownY2)
                            scaleDown2.start()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            val scaleDownX2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleX", 1f
                            )
                            val scaleDownY2 = ObjectAnimator.ofFloat(
                                holder.cardLayout, "scaleY", 1f
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
                updateRGBSection(contextCard, holder)
            }
        }
    }

    private fun updateRGBSection(contextCard: ContextCards, holder: ViewHolder) {
        holder.cardTitle.text = ColorSheetUtils.colorToHex(contextCard.colorInt)
        val accentColor: Int = adjustAlpha(
            contextCard.colorInt, 0.4f
        )
        holder.cardIcon.imageTintList = ColorStateList.valueOf(contextCard.colorInt)
        holder.cardSubtitle.setTextColor(contextCard.colorInt)
        holder.cardLayout.setCardBackgroundColor(accentColor)
        holder.cardTitle.setTextColor(contextCard.colorInt)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSwipeSelection(
        contextCard: ContextCards,
        holder: ViewHolderSwipe,
        progress: Int,
    ) {
        if (contextCard.slideListener == null) {
            if (contextCard.extraTitle != null)
                holder.cardTitle.text = progress.toString() + " ${contextCard.extraTitle}"
            else
                holder.cardTitle.text = "$progress%"
        } else {
            contextCard.slideListener!!.invoke(progress, holder.cardTitle)
        }
    }

    private fun updateSwitchSelection(contextCard: ContextCards, holder: ViewHolder) {
        val accentColor: Int = adjustAlpha(
            ContextCompat.getColor(
                holder.cardLayout.context,
                contextCard.accentColor
            ), 0.4f
        )
        if (contextCard.isCardChecked) {
            holder.cardLayout.setCardBackgroundColor(accentColor)
        } else {
            holder.cardLayout.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.cardLayout.context,
                    R.color.colorPrimaryBackground
                )
            )
        }
        if (holder.cardTitle.text == holder.cardTitle.context.getString(R.string.enabled) || holder.cardTitle.text == holder.cardTitle.context.getString(
                R.string.disabled
            )
        ) {
            if (contextCard.isCardChecked)
                holder.cardTitle.text = holder.cardTitle.context.getString(R.string.enabled)
            else
                holder.cardTitle.text = holder.cardTitle.context.getString(R.string.disabled)
        }
    }

    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardLayout: MaterialCardView = view.findViewById(R.id.cardLayout)
        val cardClickable: LinearLayout = view.findViewById(R.id.cardClickable)
        val cardTitle: TextView = view.findViewById(R.id.cardTitle)
        val cardSubtitle: TextView = view.findViewById(R.id.cardSubtitle)
        val cardSummary: TextView = view.findViewById(R.id.cardSummary)
        val cardIcon: ImageView = view.findViewById(R.id.cardIcon)
    }

    inner class ViewHolderSwipe(view: View) : RecyclerView.ViewHolder(view) {
        val cardLayout: MaterialCardView = view.findViewById(R.id.cardLayout)
        val cardClickable: LinearLayout = view.findViewById(R.id.cardClickable)
        val cardTitle: TextView = view.findViewById(R.id.cardTitle)
        val cardSubtitle: TextView = view.findViewById(R.id.cardSubtitle)
        val cardSummary: TextView = view.findViewById(R.id.cardSummary)
        val cardIcon: ImageView = view.findViewById(R.id.cardIcon)
        val cardSeek: SeekBar = view.findViewById(R.id.cardSeek)
    }

    inner class ViewHolderPager(view: View) : RecyclerView.ViewHolder(view) {
        val cardLayout: MaterialCardView = view.findViewById(R.id.cardLayout)
        val cardClickable: LinearLayout = view.findViewById(R.id.cardClickable)
        val cardTitle: TextView = view.findViewById(R.id.cardTitle)
        val cardSubtitle: TextView = view.findViewById(R.id.cardSubtitle)
        val cardSummary: TextView = view.findViewById(R.id.cardSummary)
        val cardIcon: ImageView = view.findViewById(R.id.cardIcon)
        val cardPager: ViewPager2 = view.findViewById(R.id.cardPager)
        val cardLeft: ImageButton = view.findViewById(R.id.cardLeft)
        val cardRight: ImageButton = view.findViewById(R.id.cardRight)
        val cardApply: MaterialButton = view.findViewById(R.id.cardApply)
    }

    object Type {
        const val SWITCH = 0
        const val SWIPE = 1
        const val PAGER = 2
        const val RGB = 3

        const val SECURE = 0
        const val SYSTEM = 1
        const val GLOBAL = 2
    }

}