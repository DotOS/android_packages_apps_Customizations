package com.dot.gamedashboard

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.dot.gamedashboard.fragments.DeleteAppSheet
import com.dot.ui.DotMaterialPreference

class GamingAppsAdapter(
    private val items: ArrayList<Launcher.Package>,
    private val activity: AppCompatActivity,
    private val callback: DeleteAppSheet.Callback
    ) :
        RecyclerView.Adapter<GamingAppsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.gaming_app_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pkg = items[position]
            val packageManager = activity.packageManager
            val info: PackageInfo =
                packageManager.getPackageInfo(pkg.name!!, PackageManager.GET_META_DATA)
            holder.container.titleView.text = info.applicationInfo.loadLabel(packageManager)
            holder.container.iconView.imageTintList = null
            holder.container.iconView.load(info.applicationInfo.loadIcon(packageManager)) {
                transformations(CircleCropTransformation())
                crossfade(100)
            }
            holder.container.setOnClickPreference {
                DeleteAppSheet.newInstance(pkg, callback)
                    .show(activity.supportFragmentManager, "removeApp")
            }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val container: DotMaterialPreference = view.requireViewById(R.id.gContainer)
        }
    }