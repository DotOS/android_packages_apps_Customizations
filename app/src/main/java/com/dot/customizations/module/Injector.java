/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.module;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.dot.customizations.compat.WallpaperManagerCompat;
import com.dot.customizations.model.CategoryProvider;
import com.dot.customizations.model.WallpaperInfo;
import com.dot.customizations.monitor.PerformanceMonitor;
import com.dot.customizations.network.Requester;
import com.dot.customizations.picker.PreviewFragment.PreviewMode;
import com.dot.customizations.picker.individual.IndividualPickerFragment;

/**
 * Interface for a provider of "injected dependencies." (NOTE: The term "injector" is somewhat of a
 * misnomer; this is more aptly a service registry as part of a service locator design pattern.)
 */
public interface Injector {
    AlarmManagerWrapper getAlarmManagerWrapper(Context context);

    BitmapCropper getBitmapCropper();

    CategoryProvider getCategoryProvider(Context context);

    CurrentWallpaperInfoFactory getCurrentWallpaperFactory(Context context);

    ExploreIntentChecker getExploreIntentChecker(Context context);

    FormFactorChecker getFormFactorChecker(Context context);

    LoggingOptInStatusProvider getLoggingOptInStatusProvider(Context context);

    NetworkStatusNotifier getNetworkStatusNotifier(Context context);

    PartnerProvider getPartnerProvider(Context context);

    PerformanceMonitor getPerformanceMonitor();

    Requester getRequester(Context context);

    SystemFeatureChecker getSystemFeatureChecker();

    UserEventLogger getUserEventLogger(Context context);

    WallpaperManagerCompat getWallpaperManagerCompat(Context context);

    WallpaperPersister getWallpaperPersister(Context context);

    WallpaperPreferences getPreferences(Context context);

    WallpaperRefresher getWallpaperRefresher(Context context);

    WallpaperRotationRefresher getWallpaperRotationRefresher();

    Fragment getPreviewFragment(
            Context context,
            WallpaperInfo wallpaperInfo,
            @PreviewMode int mode,
            boolean viewAsHome,
            boolean testingModeEnabled);

    PackageStatusNotifier getPackageStatusNotifier(Context context);

    IndividualPickerFragment getIndividualPickerFragment(String collectionId);

    LiveWallpaperInfoFactory getLiveWallpaperInfoFactory(Context context);

    DrawableLayerResolver getDrawableLayerResolver();

    Intent getDeepLinkRedirectIntent(Context context, Uri uri);

    String getDownloadableIntentAction();

    CustomizationSections getCustomizationSections();
}
