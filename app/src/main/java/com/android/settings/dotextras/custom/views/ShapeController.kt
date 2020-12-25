package com.android.settings.dotextras.custom.views

import android.content.Context
import android.content.om.IOverlayManager
import android.os.ServiceManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.themes.ShapeAdapter
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer
import com.android.settings.dotextras.system.OverlayController

class ShapeController(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(mContext).inflate(
            R.layout.item_shapes_control, this, true
        )
        val overlayController = OverlayController(
            OverlayController.Categories.ICON_SHAPE_CATEGORY,
            mContext.packageManager,
            IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
        )
        val recycler = findViewById<RecyclerView>(R.id.shapesRecycler)
        val adapter = ShapeAdapter(
            overlayController, overlayController.Shapes().getShapes(context!!)
        )
        recycler.adapter = adapter
        recycler.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacer),
                0,
                false
            )
        )
        recycler.addItemDecoration(
            ItemRecyclerSpacer(
                resources.getDimension(R.dimen.recyclerSpacerBig),
                adapter.itemCount - 1,
                true
            )
        )
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        visibility = if (overlayController.isAvailable()) VISIBLE else GONE
    }
}