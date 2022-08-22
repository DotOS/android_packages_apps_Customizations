package com.dot.customizations.picker

import android.annotation.SuppressLint
import android.app.WallpaperColors
import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.view.View
import androidx.fragment.app.Fragment
import com.dot.customizations.model.color.ColorUtils.isMonetEnabled
import com.dot.customizations.model.color.ColorCustomizationManager
import com.dot.customizations.model.theme.OverlayManagerCompat
import com.google.android.material.resources.MaterialAttributes

@SuppressLint("RestrictedApi")
interface WallpaperColorThemePreview {
    fun shouldApplyWallpaperColors(): Boolean {
        val activity = (this as Fragment).activity
        return if (activity == null || activity.isFinishing) {
            Log.w(
                "WallpaperColorThemePreview",
                "shouldApplyWallpaperColors: activity is null or finishing"
            )
            false
        } else if (!isMonetEnabled(activity)) {
            Log.w("WallpaperColorThemePreview", "Monet is not enabled")
            false
        } else {
            val instance =
                ColorCustomizationManager.getInstance(activity, OverlayManagerCompat(activity))
            "preset" != instance!!.currentColorSource
        }
    }

    fun updateSystemBarColor(context: Context?) {
        val resolveOrThrow = MaterialAttributes.resolveOrThrow(
            context!!,
            android.R.attr.colorPrimary,
            "android.R.attr.colorPrimary is not set in the current theme"
        )
        val window = (this as Fragment).requireActivity()
            .window
        window.statusBarColor = resolveOrThrow
        window.navigationBarColor = resolveOrThrow
    }

    fun updateWorkspacePreview(
        surfaceView: SurfaceView,
        workspaceSurfaceHolderCallback: WorkspaceSurfaceHolderCallback?,
        wallpaperColors: WallpaperColors?
    ) {
        if (shouldApplyWallpaperColors()) {
            val visibility = surfaceView.visibility
            surfaceView.visibility = View.GONE
            if (workspaceSurfaceHolderCallback != null) {
                workspaceSurfaceHolderCallback.cleanUp()
                if (workspaceSurfaceHolderCallback.mShouldUseWallpaperColors) {
                    workspaceSurfaceHolderCallback.mWallpaperColors = wallpaperColors
                    workspaceSurfaceHolderCallback.mIsWallpaperColorsReady = true
                    workspaceSurfaceHolderCallback.maybeRenderPreview()
                }
                surfaceView.setUseAlpha()
                surfaceView.alpha = 0f
                workspaceSurfaceHolderCallback.setListener {
                    surfaceView.top = -1
                    surfaceView.animate().alpha(1.0f).setDuration(300L).start()
                }
            }
            surfaceView.visibility = visibility
        }
    }


}