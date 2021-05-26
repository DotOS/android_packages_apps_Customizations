package com.dot.applock.adapter

import android.app.AppLockManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dot.applock.ObjectToolsAnimator
import com.dot.applock.R
import com.dot.applock.model.AppModel

class AppLockAdapter(private var items: ArrayList<AppModel>, private val am: AppLockManager) :
    RecyclerView.Adapter<AppLockAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppLockAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applock, parent, false))
    }

    override fun onBindViewHolder(holder: AppLockAdapter.ViewHolder, position: Int) {
        val app: AppModel = items[position]
        holder.appName.text = app.mLabel
        holder.appPackageName.text = app.mPackageName
        Glide.with(holder.appIcon)
            .load(app.mIcon)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(android.R.color.transparent)
            .into(holder.appIcon)
        holder.appAction.setOnClickListener {
            if (!am.lockedPackages.contains(app.mPackageName)) am.addAppToList(app.mPackageName)
            else am.removeAppFromList(app.mPackageName)
            updateActionIcon(app.mPackageName!!, holder)
        }
        updateActionIcon(app.mPackageName!!, holder)
        ObjectToolsAnimator.show(holder.itemView, 300)
    }

    private fun updateActionIcon(pckg: String, holder: AppLockAdapter.ViewHolder) {
        if (!am.lockedPackages.contains(pckg))
            holder.appAction.setImageResource(R.drawable.ic_unlock)
        else
            holder.appAction.setImageResource(R.drawable.ic_locked)
    }

    fun updateList(newItems: ArrayList<AppModel>) {
        val oldCount = items.size
        items = newItems
        notifyItemInserted(oldCount)
    }

    fun unlockAll() {
        for (i in items.indices) {
            val item = items[i]
            if (am.isAppLocked(item.mPackageName)) {
                am.removeAppFromList(item.mPackageName)
                notifyItemChanged(i)
            }
        }
    }

    fun lockAll() {
        for (i in items.indices) {
            val item = items[i]
            if (am.isAppOpen(item.mPackageName)) {
                am.addAppToList(item.mPackageName)
                notifyItemChanged(i)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appLayout: LinearLayout = view.findViewById(R.id.preference_layout)
        val appName: TextView = view.findViewById(android.R.id.title)
        val appPackageName: TextView = view.findViewById(android.R.id.summary)
        val appIcon: ImageView = view.findViewById(android.R.id.icon)
        val appAction: ImageButton = view.findViewById(R.id.appLockAction)
    }
}