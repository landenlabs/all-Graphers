/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;

public class DataRange {

    public float minValue;
    public float maxValue;

    public DataRange(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public float distance() {
        return maxValue - minValue;
    }

    public boolean isInside(float ... values) {
        for (float value : values)
            if (value < minValue || value > maxValue)
                return false;
        return true;
    }
}
