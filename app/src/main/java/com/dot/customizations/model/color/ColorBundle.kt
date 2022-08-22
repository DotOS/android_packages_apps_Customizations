/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.dot.customizations.model.color

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.PathParser
import com.dot.customizations.R
import com.dot.customizations.model.ResourceConstants.PATH_SIZE
import com.dot.customizations.model.ResourceConstants
import com.dot.customizations.monet.Style
import java.util.*
import kotlin.math.roundToInt

/**
 * Represents a preset color available for the user to chose as their theming option.
 */
class ColorBundle @VisibleForTesting internal constructor(
    mTitle: String,
    overlayPackages: Map<String, String>, isDefault: Boolean, style: Style, index: Int,
    override val previewInfo: PreviewInfo
) : ColorOption(mTitle, overlayPackages, isDefault, style, index) {

    override fun bindThumbnailTile(view: View) {
        val res = view.context.resources
        val primaryColor = previewInfo.resolvePrimaryColor(res)
        val secondaryColor = previewInfo.resolveSecondaryColor(res)
        val padding =
            if (view.isActivated) res.getDimensionPixelSize(R.dimen.color_seed_option_tile_padding_selected) else res.getDimensionPixelSize(
                R.dimen.color_seed_option_tile_padding
            )
        for (i in mPreviewColorIds.indices) {
            val colorPreviewImageView = view.findViewById<ImageView>(mPreviewColorIds[i])
            val color = if (i % 2 == 0) primaryColor else secondaryColor
            colorPreviewImageView.drawable.setColorFilter(color, PorterDuff.Mode.SRC)
            colorPreviewImageView.setPadding(padding, padding, padding, padding)
        }
        view.contentDescription = getContentDescription(view.context)
    }

    override fun getLayoutResId(): Int = R.layout.color_option

    override val source: String = ColorOptionsProvider.COLOR_SOURCE_PRESET

    override fun getTitle(): String {
        return mTitle
    }

    /**
     * The preview information of [ColorBundle]
     */
    class PreviewInfo(
        @field:ColorInt val secondaryColorLight: Int,
        @field:ColorInt val secondaryColorDark: Int, // Monet system palette and accent colors
        @field:ColorInt val primaryColorLight: Int,
        @field:ColorInt val primaryColorDark: Int,
        val icons: List<Drawable>,
        val shapeDrawable: Drawable?,
        @field:Dimension @param:Dimension val bottomSheetCornerRadius: Int
    ) : ColorOption.PreviewInfo {
        @ColorInt
        private var mOverrideSecondaryColorLight = Color.TRANSPARENT

        @ColorInt
        private var mOverrideSecondaryColorDark = Color.TRANSPARENT

        @ColorInt
        private var mOverridePrimaryColorLight = Color.TRANSPARENT

        @ColorInt
        private var mOverridePrimaryColorDark = Color.TRANSPARENT

        /**
         * Returns the accent color to be applied corresponding with the current configuration's
         * UI mode.
         * @return one of [.secondaryColorDark] or [.secondaryColorLight]
         */
        @ColorInt
        fun resolveSecondaryColor(res: Resources): Int {
            val night = (res.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    == Configuration.UI_MODE_NIGHT_YES)
            if (mOverrideSecondaryColorDark != Color.TRANSPARENT
                || mOverrideSecondaryColorLight != Color.TRANSPARENT
            ) {
                return if (night) mOverrideSecondaryColorDark else mOverrideSecondaryColorLight
            }
            return if (night) secondaryColorDark else secondaryColorLight
        }

        /**
         * Returns the palette (main) color to be applied corresponding with the current
         * configuration's UI mode.
         * @return one of [.secondaryColorDark] or [.secondaryColorLight]
         */
        @ColorInt
        fun resolvePrimaryColor(res: Resources): Int {
            val night = (res.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    == Configuration.UI_MODE_NIGHT_YES)
            if (mOverridePrimaryColorDark != Color.TRANSPARENT
                || mOverridePrimaryColorLight != Color.TRANSPARENT
            ) {
                return if (night) mOverridePrimaryColorDark else mOverridePrimaryColorLight
            }
            return if (night) primaryColorDark else primaryColorLight
        }

        /**
         * Sets accent colors to override the ones in this bundle
         */
        fun setOverrideAccentColors(
            overrideColorAccentLight: Int,
            overrideColorAccentDark: Int
        ) {
            mOverrideSecondaryColorLight = overrideColorAccentLight
            mOverrideSecondaryColorDark = overrideColorAccentDark
        }

        /**
         * Sets palette colors to override the ones in this bundle
         */
        fun setOverridePaletteColors(
            overrideColorPaletteLight: Int,
            overrideColorPaletteDark: Int
        ) {
            mOverridePrimaryColorLight = overrideColorPaletteLight
            mOverridePrimaryColorDark = overrideColorPaletteDark
        }
    }

    /**
     * The builder of ColorBundle
     */
    class Builder {
        /**
         * Gets title of this [ColorBundle] object
         * @return title string
         */
        var title: String? = null

        @ColorInt
        private var mSecondaryColorLight = Color.TRANSPARENT

        @ColorInt
        private var mSecondaryColorDark = Color.TRANSPARENT

        // System and Monet colors
        @ColorInt
        private var mPrimaryColorLight = Color.TRANSPARENT

        @ColorInt
        private var mPrimaryColorDark = Color.TRANSPARENT
        private val mIcons: MutableList<Drawable> = ArrayList()
        private var mIsDefault = false
        private var mStyle: Style = Style.TONAL_SPOT
        private var mIndex = 0
        protected var mPackages: MutableMap<String, String> = HashMap()

        /**
         * Builds the ColorBundle
         * @param context [Context]
         * @return new [ColorBundle] object
         */
        fun build(context: Context): ColorBundle {
            if (title == null) {
                title = context.getString(R.string.adaptive_color_title)
            }
            return ColorBundle(
                title!!, mPackages, mIsDefault, mStyle, mIndex,
                createPreviewInfo(context)
            )
        }

        /**
         * Creates preview information
         * @param context the [Context]
         * @return the [PreviewInfo] object
         */
        fun createPreviewInfo(context: Context): PreviewInfo {
            var shapeDrawable: ShapeDrawable? = null
            val system = Resources.getSystem()
            val pathString = system.getString(
                system.getIdentifier(
                    ResourceConstants.CONFIG_ICON_MASK,
                    "string", ResourceConstants.ANDROID_PACKAGE
                )
            )
            var path: Path? = null
            if (!TextUtils.isEmpty(pathString)) {
                path = PathParser.createPathFromPathData(pathString)
            }
            if (path != null) {
                val shape = PathShape(path, PATH_SIZE, PATH_SIZE)
                shapeDrawable = ShapeDrawable(shape)
                shapeDrawable.intrinsicHeight = PATH_SIZE.roundToInt()
                shapeDrawable.intrinsicWidth = PATH_SIZE.roundToInt()
            }
            return PreviewInfo(
                mSecondaryColorLight,
                mSecondaryColorDark, mPrimaryColorLight, mPrimaryColorDark, mIcons,
                shapeDrawable, system.getDimensionPixelOffset(
                    system.getIdentifier(
                        ResourceConstants.CONFIG_CORNERRADIUS,
                        "dimen", ResourceConstants.ANDROID_PACKAGE
                    )
                )
            )
        }

        val packages: Map<String, String>
            get() = Collections.unmodifiableMap(mPackages)

        /**
         * Sets title of bundle
         * @param title specified title
         * @return this of [Builder]
         */
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        /**
         * Sets color accent (light)
         * @param colorSecondaryLight color accent light in [ColorInt]
         * @return this of [Builder]
         */
        fun setColorSecondaryLight(@ColorInt colorSecondaryLight: Int): Builder {
            mSecondaryColorLight = colorSecondaryLight
            return this
        }

        /**
         * Sets color accent (dark)
         * @param colorSecondaryDark color accent dark in [ColorInt]
         * @return this of [Builder]
         */
        fun setColorSecondaryDark(@ColorInt colorSecondaryDark: Int): Builder {
            mSecondaryColorDark = colorSecondaryDark
            return this
        }

        /**
         * Sets color system palette (light)
         * @param colorPrimaryLight color system palette in [ColorInt]
         * @return this of [Builder]
         */
        fun setColorPrimaryLight(@ColorInt colorPrimaryLight: Int): Builder {
            mPrimaryColorLight = colorPrimaryLight
            return this
        }

        /**
         * Sets color system palette (dark)
         * @param colorPrimaryDark color system palette in [ColorInt]
         * @return this of [Builder]
         */
        fun setColorPrimaryDark(@ColorInt colorPrimaryDark: Int): Builder {
            mPrimaryColorDark = colorPrimaryDark
            return this
        }

        /**
         * Sets icon for bundle
         * @param icon icon in [Drawable]
         * @return this of [Builder]
         */
        fun addIcon(icon: Drawable): Builder {
            mIcons.add(icon)
            return this
        }

        /**
         * Sets overlay package for bundle
         * @param category the category of bundle
         * @param packageName tha name of package in the category
         * @return this of [Builder]
         */
        fun addOverlayPackage(category: String, packageName: String): Builder {
            mPackages[category] = packageName
            return this
        }

        /**
         * Sets the style of this color seed
         * @param style color style of [Style]
         * @return this of [Builder]
         */
        fun setStyle(style: Style): Builder {
            mStyle = style
            return this
        }

        /**
         * Sets color option index of bundle
         * @param index color option index
         * @return this of [Builder]
         */
        fun setIndex(index: Int): Builder {
            mIndex = index
            return this
        }

        /**
         * Sets as default bundle
         * @return this of [Builder]
         */
        fun asDefault(): Builder {
            mIsDefault = true
            return this
        }
    }
}