package com.dot.applock.adapter

import android.app.Activity
import android.app.AppLockManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.dot.applock.R
import com.dot.applock.databinding.ItemApplockBinding
import com.dot.applock.model.AppModel
import com.dot.ui.utils.ObjectToolsAnimator
import kotlin.collections.ArrayList

class AppLockAdapter(
    var items: ArrayList<AppModel>,
    private val am: AppLockManager,
    activity: Activity
) :
    RecyclerView.Adapter<AppLockAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemApplockBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app: AppModel = items[position]
        with(holder.binding) {
            title.text = app.mLabel
            summary.text = app.mPackageName
            icon.load(app.mIcon) {
                crossfade(200)
                placeholder(android.R.color.transparent)
                transformations(CircleCropTransformation())
            }
            appLockAction.setOnClickListener {
                if (!am.lockedPackages.contains(app.mPackageName)) am.addAppToList(app.mPackageName)
                else am.removeAppFromList(app.mPackageName)
                updateActionIcon(app.mPackageName!!, holder)
            }
            updateActionIcon(app.mPackageName!!, holder)
            ObjectToolsAnimator.show(root, 300)
        }
    }

    private fun updateActionIcon(pckg: String, holder: ViewHolder) {
        if (!am.lockedPackages.contains(pckg))
            holder.binding.appLockAction.setImageResource(R.drawable.ic_unlock)
        else
            holder.binding.appLockAction.setImageResource(R.drawable.ic_locked)
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

    data class ViewHolder(val binding: ItemApplockBinding) :
        RecyclerView.ViewHolder(binding.root)
}