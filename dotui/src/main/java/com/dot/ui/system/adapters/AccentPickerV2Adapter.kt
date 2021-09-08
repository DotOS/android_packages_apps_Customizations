package com.dot.ui.system.adapters

import android.content.Context
import android.content.om.IOverlayManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.ServiceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dot.ui.R
import com.dot.ui.system.items.AccentColor
import com.dot.ui.system.OverlayController
import com.dot.ui.utils.ResourceHelper

class AccentPickerV2Adapter(private val items: ArrayList<AccentColor>) :
    RecyclerView.Adapter<AccentPickerV2Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_v2_accentcolor, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val accent = items[position]
        val mContext = holder.itemView.context
        val overlayController = OverlayController(
            OverlayController.Categories.ACCENT_CATEGORY,
            mContext.packageManager,
            IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
        )
        if (accent.title != null) holder.title.text = accent.title
        if (accent.isMonet) {
            holder.preview.imageTintList = ColorStateList.valueOf(accent.color!!)
            holder.preview.setOnClickListener {
                select(position)
                updateMonet(accent, holder)
            }
            updateMonet(accent, holder)
        } else {
            val nightModeFlags: Int = mContext.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    holder.preview.imageTintList = ColorStateList.valueOf(accent.colorDark!!)
                    holder.title.text = ResourceHelper.colorToHex(accent.colorDark!!)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    holder.preview.imageTintList =
                        ColorStateList.valueOf(accent.colorLight!!)
                    holder.title.text = ResourceHelper.colorToHex(accent.colorLight!!)
                }
            }

            holder.preview.setOnClickListener {
                select(position)
                overlayController.AccentColors().setOverlay(accent.packageName!!, accent, holder)
                updateSystem(accent, holder)
            }
            updateSystem(accent, holder)
        }
    }

    private fun updateMonet(
        accentColor: AccentColor,
        holder: ViewHolder,
    ) {
        holder.checked.visibility = if (accentColor.selected) View.VISIBLE else View.GONE
        holder.checked.imageTintList = ColorStateList.valueOf(accentColor.textColorBody!!)
    }

    private fun updateSystem(accentColor: AccentColor, holder: ViewHolder) {
        holder.checked.visibility = if (accentColor.selected) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            val selected = items[i].selected
            items[i].selected = pos == i
            if (selected != items[i].selected) notifyItemChanged(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val preview: ImageButton = view.requireViewById(R.id.accentColorCircle)
        val checked: ImageView = view.requireViewById(R.id.accentChecked)
        val title: TextView = view.requireViewById(R.id.accentTitle)
    }
}