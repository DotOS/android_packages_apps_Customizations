package com.dot.gamedashboard

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.dot.ui.DotMaterialPreference
import com.dot.ui.utils.ObjectToolsAnimator

class AppsAdapter(private var items: ArrayList<AppModel>) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.app_layout, parent, false))
    }

    override fun onBindViewHolder(holder: AppsAdapter.ViewHolder, position: Int) {
        val app: AppModel = items[position]
        holder.appLayout.setWidgetLayout(R.layout.preference_widget_select)
        holder.appLayout.titleView.text = app.mLabel
        holder.appLayout.summary = app.mPackageName
        holder.appLayout.iconView.imageTintList = null
        holder.appLayout.iconView.load(app.mIcon) {
            transformations(CircleCropTransformation())
            crossfade(200)
        }
        holder.appLayout.setOnClickPreference {
            toggle(position)
            updateActionIcon(app, holder)
        }
        updateActionIcon(app, holder)
        ObjectToolsAnimator.show(holder.itemView, 100)
    }

    private fun updateActionIcon(app: AppModel, holder: AppsAdapter.ViewHolder) {
        val appAction: RadioButton = holder.appLayout.widgetLayout.findViewById(R.id.radioButton)
        appAction.isChecked = app.selected
    }

    private fun toggle(position: Int) {
        items[position].selected = !items[position].selected
    }

    fun getApps(): ArrayList<Launcher.Package> {
        val apps = ArrayList<Launcher.Package>()
        for (i in items.indices) {
            if (items[i].selected) apps.add(Launcher.Package(items[i].mPackageName))
        }
        return apps
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appLayout: DotMaterialPreference = view.findViewById(R.id.gContainer)
    }

    class AppModel(info: ResolveInfo, pm: PackageManager) {
        var mLabel: String? = null
        var mPackageName: String? = null
        var mIcon: Drawable? = null
        var selected = false

        init {
            mLabel = info.loadLabel(pm).toString()
            mIcon = info.loadIcon(pm)
            mPackageName = info.activityInfo.packageName
        }
    }
}