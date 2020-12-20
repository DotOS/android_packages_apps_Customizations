package com.android.settings.dotextras.custom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dashboardItem: DashboardItem = items[position]
        holder.title.text = dashboardItem.card_title
        holder.fragmentlayout.id += position + Random().nextInt()
        fragmentManager.beginTransaction().replace(
            holder.fragmentlayout.id,
            dashboardItem.display_fragment
        ).commit()
        holder.fragmentlayout.setOnClickListener {
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
            activity.setTitle(dashboardItem.card_title)
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
        val fragmentlayout: LinearLayout = view.findViewById(R.id.card_fragment_container)
    }
}