package com.android.settings.dotextras.custom.sections.batterystyles;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import androidx.annotation.ColorInt;

public class Utils {

    @ColorInt
    public static int getColorStateListDefaultColor(Context context, int resId) {
        final ColorStateList list =
                context.getResources().getColorStateList(resId, context.getTheme());
        return list.getDefaultColor();
    }

    @ColorInt
    public static int getColorAttrDefaultColor(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

}
