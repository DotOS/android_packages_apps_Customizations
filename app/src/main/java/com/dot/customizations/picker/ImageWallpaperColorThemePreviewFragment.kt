package com.dot.customizations.picker

import android.annotation.SuppressLint
import android.app.WallpaperColors
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.RemoteViews
import com.dot.customizations.R
import com.dot.customizations.R.*
import com.dot.customizations.model.color.WallpaperColorResources
import com.google.android.material.resources.MaterialAttributes
import com.dot.customizations.widget.LockScreenPreviewer

class ImageWallpaperColorThemePreviewFragment : ImagePreviewFragment(), WallpaperColorThemePreview {
    var mIgnoreInitialColorChange = false
    var mWallpaperColors: WallpaperColors? = null
    public override fun createWorkspaceSurfaceCallback(surfaceView: SurfaceView): WorkspaceSurfaceHolderCallback {
        return WorkspaceSurfaceHolderCallback(surfaceView, context, shouldApplyWallpaperColors())
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val bundle2 = arguments
        if (bundle2 != null && bundle2.getInt("preview_mode") == 0) {
            mIgnoreInitialColorChange = true
        }
    }

    @SuppressLint("RestrictedApi")
    public override fun onWallpaperColorsChanged(wallpaperColors: WallpaperColors?) {
        if (mIgnoreInitialColorChange || wallpaperColors == null) {
            updateWorkspacePreview(mWorkspaceSurface, mWorkspaceSurfaceCallback, null)
        } else if (wallpaperColors != mWallpaperColors && shouldApplyWallpaperColors()) {
            mWallpaperColors = wallpaperColors
            RemoteViews.ColorResources.create(
                context,
                WallpaperColorResources(wallpaperColors).mColorOverlay
            ).apply(context)
            updateSystemBarColor(context)
            requireView().setBackgroundColor(
                MaterialAttributes.resolveOrThrow(
                    requireContext(),
                    android.R.attr.colorPrimary,
                    "android.R.attr.colorPrimary is not set in the current theme"
                )
            )
            val from = LayoutInflater.from(context)
            val viewGroup =
                requireView().findViewById<ViewGroup>(R.id.section_header_container)
            viewGroup.removeAllViews()
            setUpToolbar(
                from.inflate(layout.section_header, viewGroup),
                true
            )
            mFullScreenAnimation.ensureToolbarIsCorrectlyLocated()
            mFullScreenAnimation.ensureToolbarIsCorrectlyColored()
            val viewGroup2 =
                requireView().findViewById<ViewGroup>(R.id.fullscreen_buttons_container)
            viewGroup2.removeAllViews()
            setFullScreenActions(
                from.inflate(
                    layout.fullscreen_buttons,
                    viewGroup2
                )
            )
            mBottomActionBar.setColor(from.context)
            updateWorkspacePreview(mWorkspaceSurface, mWorkspaceSurfaceCallback, wallpaperColors)
            val viewGroup3 =
                requireView().findViewById<ViewGroup>(R.id.separated_tabs_container)
            viewGroup3.removeAllViews()
            setUpTabs(
                from.inflate(layout.separated_tabs, viewGroup3)
                    .findViewById(R.id.separated_tabs)
            )
            mLockScreenPreviewer.release()
            mLockPreviewContainer.removeAllViews()
            val lockScreenPreviewer = LockScreenPreviewer(lifecycle, context, mLockPreviewContainer)
            mLockScreenPreviewer = lockScreenPreviewer
            lockScreenPreviewer.setDateViewVisibility(!mFullScreenAnimation.isFullScreen)
        }
        mIgnoreInitialColorChange = false
        super.onWallpaperColorsChanged(wallpaperColors)
    }
}