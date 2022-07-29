package com.dot.customizations.module

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.dot.customizations.model.*
import com.dot.customizations.model.CustomizationSectionController.CustomizationSectionNavigationController
import com.dot.customizations.model.applock.AppLockSectionController
import com.dot.customizations.model.color.ColorSectionController
import com.dot.customizations.model.color.MonetSectionController
import com.dot.customizations.model.extras.ExtrasSectionController
import com.dot.customizations.model.grid.GridOptionsManager
import com.dot.customizations.model.grid.GridSectionController
import com.dot.customizations.model.mode.DarkModeSectionController
import com.dot.customizations.model.themedicon.ThemedIconSectionController
import com.dot.customizations.model.themedicon.ThemedIconSwitchProvider

/**
 * [CustomizationSections] for the customization picker.
 */
class DefaultCustomizationSections : CustomizationSections {
    override fun getAllSectionControllers(
        activity: Activity,
        lifecycleOwner: LifecycleOwner, wallpaperColorsViewModel: WallpaperColorsViewModel,
        workspaceViewModel: WorkspaceViewModel, permissionRequester: PermissionRequester,
        wallpaperPreviewNavigator: WallpaperPreviewNavigator,
        sectionNavigationController: CustomizationSectionNavigationController,
        savedInstanceState: Bundle?
    ): List<CustomizationSectionController<*>> {
        val sectionControllers: MutableList<CustomizationSectionController<*>> = ArrayList()

        // Wallpaper section.
        sectionControllers.add(
            WallpaperSectionController(
                activity, lifecycleOwner, permissionRequester, wallpaperColorsViewModel,
                workspaceViewModel, sectionNavigationController, wallpaperPreviewNavigator,
                savedInstanceState
            )
        )

        // Monet Color Picker section.
        sectionControllers.add(
            ColorSectionController(
                activity,
                wallpaperColorsViewModel,
                lifecycleOwner,
                sectionNavigationController,
                savedInstanceState
            )
        )

        // Dark/Light theme section.
        sectionControllers.add(
            DarkModeSectionController(
                activity,
                lifecycleOwner.lifecycle
            )
        )

        // Themed app icon section.
        sectionControllers.add(
            ThemedIconSectionController(
                ThemedIconSwitchProvider.getInstance(activity), workspaceViewModel,
                savedInstanceState
            )
        )

        // App grid section.
        sectionControllers.add(
            GridSectionController(
                GridOptionsManager.getInstance(activity), sectionNavigationController
            )
        )

        // Monet Settings section.
        sectionControllers.add(MonetSectionController(sectionNavigationController))

        // App Lock
        sectionControllers.add(AppLockSectionController(sectionNavigationController))

        // Dot Extras
        sectionControllers.add(ExtrasSectionController(sectionNavigationController))
        return sectionControllers
    }
}