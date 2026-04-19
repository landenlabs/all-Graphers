/*
 * Copyright (c) 2026 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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
