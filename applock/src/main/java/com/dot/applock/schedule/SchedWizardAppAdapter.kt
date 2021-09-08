package com.dot.applock.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dot.applock.R
import com.dot.applock.model.AppModel
import com.dot.applock.ui.CustomTextView
import com.dot.ui.utils.ObjectToolsAnimator

class SchedWizardAppAdapter(private var items: ArrayList<AppModel>, private val statusText: CustomTextView) :
    RecyclerView.Adapter<SchedWizardAppAdapter.ViewHolder>() {

    private lateinit var utils: SchedUtils

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SchedWizardAppAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedapplock, parent, false))
    }

    override fun onBindViewHolder(holder: SchedWizardAppAdapter.ViewHolder, position: Int) {
        val app: AppModel = items[position]
        utils = SchedUtils(holder.itemView.context)
        holder.appName.text = app.mLabel
        holder.appPackageName.text = app.mPackageName
        Glide.with(holder.appIcon)
            .load(app.mIcon)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(android.R.color.transparent)
            .into(holder.appIcon)
        holder.appLayout.setOnClickListener {
            if (!utils.tempGetApps().contains(app.mPackageName))
                utils.tempAddApp(app.mPackageName.toString())
            else
                utils.tempRemoveApp(app.mPackageName.toString())
            updateActionIcon(app.mPackageName!!, holder)
        }
        updateActionIcon(app.mPackageName!!, holder)
        ObjectToolsAnimator.show(holder.itemView, 300)
    }

    private fun updateActionIcon(pckg: String, holder: SchedWizardAppAdapter.ViewHolder) {
        if (!utils.tempGetApps().contains(pckg))
            holder.appAction.setImageResource(R.drawable.ic_add)
        else
            holder.appAction.setImageResource(R.drawable.ic_close)

        if (getSelectedCount() != 0)
            statusText.text = "Selected ${getSelectedCount()}/${items.size}"
        else
            statusText.text = ""
    }

    fun updateList(newItems: ArrayList<AppModel>) {
        val oldCount = items.size
        items = newItems
        notifyItemInserted(oldCount)
    }

    private fun getSelectedCount(): Int {
        var count = 0
        for (item in items) {
            if (utils.tempGetApps().contains(item.mPackageName))
                count++
        }
        return count
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appLayout: LinearLayout = view.findViewById(R.id.preference_layout)
        val appName: TextView = view.findViewById(android.R.id.title)
        val appPackageName: TextView = view.findViewById(android.R.id.summary)
        val appIcon: ImageView = view.findViewById(android.R.id.icon)
        val appAction: ImageView = view.findViewById(R.id.appLockAction)
    }
}