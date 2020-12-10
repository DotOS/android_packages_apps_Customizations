package com.android.settings.dotextras.custom.displays

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.displays.models.AppInfo

class IconsDisplay : Fragment() {

    private val appsNeeded: Int = 4

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.display_icons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arrayAppIds: ArrayList<Int> = arrayListOf(
            R.id.ic0,
            R.id.ic1,
            R.id.ic2,
            R.id.ic3
        )
        val appIcons: ArrayList<AppInfo> = ArrayList()
        val packageManager = requireActivity().packageManager
        try {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
            for (resolveInfo in resolveInfoList) {
                appIcons.add(
                    AppInfo(
                        resolveInfo.activityInfo.name,
                        resolveInfo.activityInfo.loadIcon(packageManager)
                    )
                )
                if (appIcons.size == appsNeeded)
                    break
            }
            appIcons.sortBy { it.name }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        for (i in 0 until appsNeeded) {
            val strokeBackground = GradientDrawable()
            strokeBackground.shape = GradientDrawable.OVAL
            strokeBackground.setStroke(
                2,
                ContextCompat.getColor(requireContext(), android.R.color.secondary_text_light)
            )
            view.findViewById<ImageView>(arrayAppIds[i]).setImageDrawable(appIcons[i].icon)
            view.findViewById<ImageView>(arrayAppIds[i]).background = strokeBackground
        }
    }

}