/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

public class UiUtils {
    public static float dpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * metrics.density;
    }

    private static float screenScale() {
        // return Math.min(1.0f, displayWidthPx / screenWidthPx);
        return 1.0f; // TODO - fix this
    }

    public static int screenScale(int iValue) {
        return (iValue <= 0) ? iValue : Math.round(iValue * screenScale());
    }

    public static ViewGroup.LayoutParams layoutFromAnyStyle(
            @NonNull Context context, @StyleRes int style, ViewGroup.LayoutParams lp) {
        if (lp instanceof GridLayout.LayoutParams) {
            return layoutFromStyle(context, style, (GridLayout.LayoutParams) lp);
        } else if (lp instanceof LinearLayout.LayoutParams) {
            return layoutFromStyle(context, style, (LinearLayout.LayoutParams)lp);
        } else  if (lp instanceof ViewGroup.MarginLayoutParams) {
            return layoutFromStyle(context, style, (ViewGroup.MarginLayoutParams)lp);
        } else {
            return layoutFromStyle(context, style, lp);
        }
    }

    public static ViewGroup.LayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, ViewGroup.LayoutParams lp) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_width,
                        android.R.attr.layout_height,
                },
                0, style);
        try {
            lp.width = screenScale(ta.getLayoutDimension(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            lp.height = screenScale(ta.getLayoutDimension(1, ViewGroup.LayoutParams.WRAP_CONTENT));
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static ViewGroup.MarginLayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, ViewGroup.MarginLayoutParams lp) {
        layoutFromStyle(context, style, (ViewGroup.LayoutParams) lp);
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_marginTop,
                        android.R.attr.layout_marginBottom,
                        android.R.attr.layout_marginStart,
                        android.R.attr.layout_marginEnd,
                },
                0, style);
        try {
            float scale = screenScale();
            lp.topMargin = screenScale(ta.getLayoutDimension(0, lp.topMargin));
            lp.bottomMargin = screenScale(ta.getLayoutDimension(1, lp.bottomMargin));
            lp.leftMargin = screenScale(ta.getLayoutDimension(2, lp.leftMargin));
            lp.rightMargin = screenScale(ta.getLayoutDimension(3, lp.rightMargin));
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static LinearLayout.LayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, LinearLayout.LayoutParams lp) {
        layoutFromStyle(context, style, (ViewGroup.LayoutParams) lp);
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_weight,     // 0
                },
                0, style);

        try {
            lp.weight = ta.getFloat(0, 0f);
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static GridLayout.LayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, GridLayout.LayoutParams lp) {
        layoutFromStyle(context, style, (ViewGroup.LayoutParams) lp);
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_row ,             // 0
                        android.R.attr.layout_rowSpan,          // 1
                        android.R.attr.layout_rowWeight ,       // 2
                        android.R.attr.layout_column ,          // 3
                        android.R.attr.layout_columnSpan,       // 4
                        android.R.attr.layout_columnWeight,     // 5
                        android.R.attr.layout_gravity,          // 6
                },
                0, style);

        try {
            lp.rowSpec =  GridLayout.spec(GridLayout.UNDEFINED,   ta.getFloat(2, 0f));
            lp.columnSpec =  GridLayout.spec(GridLayout.UNDEFINED,   ta.getFloat(5, 0f));
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static void compare(GridLayout.LayoutParams glp1, GridLayout.LayoutParams glp2) {
        String s1 = glp1.toString();
        String s2 = glp2.toString();

        GridLayout.Spec spec1 = glp1.columnSpec;
        GridLayout.Spec spec2 = glp2.columnSpec;

        if ( ! s1.equals(s2)) {
            Log.d("UI", "differ");
        }

    }
}
