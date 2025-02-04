/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.graph;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class UtilsUi {
    public static float dpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * metrics.density;
    }
}
