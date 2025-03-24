/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;


/**
 * Store current unit state and provide ability to convert value.
 */
public class DataUnit {

    public boolean inMetric = false;
    public boolean outMetric = false;
    public Units unit;
    public DataRange outRange = new DataRange(0, 100);
    public DataRange inRange;

    public float convert(float value, boolean outMetric) {
        return scale(unit.convert(value, inMetric, outMetric));
    }

    public float scale(float value) {
        if (inRange != null) {
            value += outRange.minValue - inRange.minValue;
            value = value * outRange.distance() / inRange.distance();
        }
        return value;
    }
}
