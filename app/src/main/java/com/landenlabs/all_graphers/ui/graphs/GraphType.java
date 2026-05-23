/*
 *
 * Copyright (c) 2026 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
 */

package com.landenlabs.all_graphers.ui.graphs;

public enum GraphType {
    // TODO - keep in sync with string array R.array.array_graph_menu
    /*
        <item>Icing probability</item>
        <item>Relative Humidity</item>
        <item>Temperature</item>
        <item>Turbulence</item>
        <item>Wind speed</item>
     */
    IceProbability, Humidity, Temperature, Turbulence,  WindSpeed, None;

    public static GraphType from(int idx ) {
        if (idx >= 0 && idx < values().length)
            return values()[idx];
        return None;
    }
}
