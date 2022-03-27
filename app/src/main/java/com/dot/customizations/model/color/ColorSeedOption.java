package com.dot.customizations.model.color;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.dot.customizations.R;

import java.util.Map;
import java.util.Objects;

public class ColorSeedOption extends ColorOption {
    public final int[] mPreviewColorIds = {R.id.color_preview_0, R.id.color_preview_1, R.id.color_preview_2, R.id.color_preview_3};
    public final PreviewInfo mPreviewInfo;
    public final String mSource;

    public ColorSeedOption(String str, Map<String, String> map, boolean z, String str2, int i, PreviewInfo previewInfo) {
        super(str, map, z, i);
        this.mSource = str2;
        this.mPreviewInfo = previewInfo;
    }

    @Override // com.dot.customizations.model.CustomizationOption
    public void bindThumbnailTile(View view) {
        int i;
        Resources resources = view.getContext().getResources();
        PreviewInfo previewInfo = this.mPreviewInfo;
        Objects.requireNonNull(previewInfo);
        int i2 = 0;
        int[] iArr = (resources.getConfiguration().uiMode & 48) == 32 ? previewInfo.darkColors : previewInfo.lightColors;
        if (view.isActivated()) {
            i = resources.getDimensionPixelSize(R.dimen.color_seed_option_tile_padding_selected);
        } else {
            i = resources.getDimensionPixelSize(R.dimen.color_seed_option_tile_padding);
        }
        while (true) {
            int[] iArr2 = this.mPreviewColorIds;
            if (i2 < iArr2.length) {
                ImageView imageView = (ImageView) view.findViewById(iArr2[i2]);
                imageView.getDrawable().setColorFilter(iArr[i2], PorterDuff.Mode.SRC);
                imageView.setPadding(i, i, i, i);
                i2++;
            } else {
                view.setContentDescription(view.getContext().getString(R.string.wallpaper_color_title));
                return;
            }
        }
    }

    @Override // com.dot.customizations.model.CustomizationOption
    public int getLayoutResId() {
        return R.layout.color_seed_option;
    }

    @NonNull
    @Override // com.dot.customizations.model.color.ColorOption
    public String getSource() {
        return this.mSource;
    }

    public static class PreviewInfo {
        public int[] darkColors;
        public int[] lightColors;

        public PreviewInfo(int[] iArr, int[] iArr2) {
            this.lightColors = iArr;
            this.darkColors = iArr2;
        }
    }
}
