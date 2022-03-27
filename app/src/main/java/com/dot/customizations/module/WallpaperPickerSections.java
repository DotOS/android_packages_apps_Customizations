package com.dot.customizations.module;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.dot.customizations.model.CustomizationSectionController;
import com.dot.customizations.model.CustomizationSectionController.CustomizationSectionNavigationController;
import com.dot.customizations.model.PermissionRequester;
import com.dot.customizations.model.WallpaperColorsViewModel;
import com.dot.customizations.model.WallpaperPreviewNavigator;
import com.dot.customizations.model.WallpaperSectionController;
import com.dot.customizations.model.WorkspaceViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CustomizationSections} for the wallpaper picker.
 */
public final class WallpaperPickerSections implements CustomizationSections {

    @Override
    public List<CustomizationSectionController<?>> getAllSectionControllers(Activity activity,
                                                                            LifecycleOwner lifecycleOwner, WallpaperColorsViewModel wallpaperColorsViewModel,
                                                                            WorkspaceViewModel workspaceViewModel, PermissionRequester permissionRequester,
                                                                            WallpaperPreviewNavigator wallpaperPreviewNavigator,
                                                                            CustomizationSectionNavigationController sectionNavigationController,
                                                                            @Nullable Bundle savedInstanceState) {
        List<CustomizationSectionController<?>> sectionControllers = new ArrayList<>();

        sectionControllers.add(new WallpaperSectionController(
                activity, lifecycleOwner, permissionRequester, wallpaperColorsViewModel,
                workspaceViewModel, sectionNavigationController, wallpaperPreviewNavigator,
                savedInstanceState));

        return sectionControllers;
    }
}
