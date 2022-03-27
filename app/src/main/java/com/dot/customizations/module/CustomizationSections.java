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
import com.dot.customizations.model.WorkspaceViewModel;

import java.util.List;

/**
 * Interface for carry {@link CustomizationSectionController}s.
 */
public interface CustomizationSections {

    /**
     * Gets a new instance of the section controller list.
     * <p>
     * Note that the section views will be displayed by the list ordering.
     *
     * <p>Don't keep the section controllers as singleton since they contain views.
     */
    List<CustomizationSectionController<?>> getAllSectionControllers(
            Activity activity,
            LifecycleOwner lifecycleOwner,
            WallpaperColorsViewModel wallpaperColorsViewModel,
            WorkspaceViewModel workspaceViewModel,
            PermissionRequester permissionRequester,
            WallpaperPreviewNavigator wallpaperPreviewNavigator,
            CustomizationSectionNavigationController sectionNavigationController,
            @Nullable Bundle savedInstanceState);
}
