package com.dot.customizations.module;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.dot.customizations.model.CustomizationSectionController;
import com.dot.customizations.model.CustomizationSectionController.CustomizationSectionNavigationController;
import com.dot.customizations.model.PermissionRequester;
import com.dot.customizations.model.WallpaperColorsViewModel;
import com.dot.customizations.model.WallpaperPreviewNavigator;
import com.dot.customizations.model.WallpaperSectionController;
import com.dot.customizations.model.WorkspaceViewModel;
import com.dot.customizations.model.color.ColorSectionController;
import com.dot.customizations.model.extras.ExtrasSectionController;
import com.dot.customizations.model.grid.GridOptionsManager;
import com.dot.customizations.model.grid.GridSectionController;
import com.dot.customizations.model.mode.DarkModeSectionController;
import com.dot.customizations.model.themedicon.ThemedIconSectionController;
import com.dot.customizations.model.themedicon.ThemedIconSwitchProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CustomizationSections} for the customization picker.
 */
public final class DefaultCustomizationSections implements CustomizationSections {

    @Override
    public List<CustomizationSectionController<?>> getAllSectionControllers(Activity activity,
                                                                            LifecycleOwner lifecycleOwner, WallpaperColorsViewModel wallpaperColorsViewModel,
                                                                            WorkspaceViewModel workspaceViewModel, PermissionRequester permissionRequester,
                                                                            WallpaperPreviewNavigator wallpaperPreviewNavigator,
                                                                            CustomizationSectionNavigationController sectionNavigationController,
                                                                            @Nullable Bundle savedInstanceState) {
        List<CustomizationSectionController<?>> sectionControllers = new ArrayList<>();

        // Wallpaper section.
        sectionControllers.add(new WallpaperSectionController(
                activity, lifecycleOwner, permissionRequester, wallpaperColorsViewModel,
                workspaceViewModel, sectionNavigationController, wallpaperPreviewNavigator,
                savedInstanceState));

        sectionControllers.add(new ColorSectionController(activity, wallpaperColorsViewModel, lifecycleOwner, savedInstanceState));

        // Dark/Light theme section.
        sectionControllers.add(new DarkModeSectionController(activity,
                lifecycleOwner.getLifecycle()));

        // Themed app icon section.
        sectionControllers.add(new ThemedIconSectionController(
                ThemedIconSwitchProvider.getInstance(activity), workspaceViewModel,
                savedInstanceState));

        // App grid section.
        sectionControllers.add(new GridSectionController(
                GridOptionsManager.getInstance(activity), sectionNavigationController));

        // Dot Extras
        sectionControllers.add(new ExtrasSectionController(sectionNavigationController));

        return sectionControllers;
    }
}
