/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Draw simple bar graph.
 * Supports multiple series
 */
public abstract class GraphBaseView extends View {

    public GraphBaseView(Context context) {
        super(context);
    }

    public GraphBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GraphBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GraphBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void setSeriesCfg(int series, @ColorInt int minColor, @ColorInt int maxColor, @NonNull String name);

    /**
     * Add data value on a series.
     *
     * @param x      0...n
     * @param y      magnitude of value
     * @param series Each series has its own color.
     */
    public abstract void addSeriesPoint(int series, float x, float y);

    public abstract void clear();

    public abstract float getMinY();

    public abstract void setMinY(float minY);

    public abstract float getMaxY();

    public abstract void setMaxY(float maxY);

    public abstract void setLineColor(@ColorInt int color);

    public abstract void setFillStartColor(@ColorInt int color);

    public abstract void setFillEndColor(@ColorInt int color);

}
