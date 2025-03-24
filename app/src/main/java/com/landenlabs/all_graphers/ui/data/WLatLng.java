/*
 * IBM Confidential
 * Copyright IBM Corp. 2016, 2021. Copyright WSI Corporation 1998, 2015
 */

package com.landenlabs.all_graphers.ui.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Wrapper class to extend behavior of TWCMapLatLng
 */
public class WLatLng {
    public final double latitude;
    public final double longitude;

    public WLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    private final static WLatLng EMPTY = new WLatLng(0, 0);
    public static WLatLng empty() {
        return EMPTY;
    }

    /**
     * Normalize longitude to -180 to 180 range.
     */
    static double normalizeLng(double lng) {
        //noinspection IntegerDivisionInFloatingPointContext
        return lng - ((int) lng / 180 * 360);
    }

    public int hashCode() {
        long bits = Double.doubleToLongBits(this.latitude);
        int result = 31 + (int) (bits ^ bits >>> 32);
        bits = Double.doubleToLongBits(this.longitude);
        result = 31 * result + (int) (bits ^ bits >>> 32);
        return result;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof WLatLng)) {
            return false;
        } else {
            WLatLng otherLL = (WLatLng) other;
            return Double.doubleToLongBits(this.latitude)
                    == Double.doubleToLongBits(otherLL.latitude)
                    && Double.doubleToLongBits(this.longitude)
                    == Double.doubleToLongBits(otherLL.longitude);
        }
    }

    private static double SQ(double dvale) {
        return dvale * dvale;
    }

    /**
     * Return difference degrees square
     */
    public static double distanceSQ(WLatLng mapboxLL1, WLatLng mapboxLL2) {
        return SQ(mapboxLL1.getLatitude() - mapboxLL2.getLatitude())
                + SQ(mapboxLL1.getLongitude() - mapboxLL2.getLongitude());
    }

    /*
    public double distanceTo(WLatLng latLng) {
        LatLng pos1 = toPangeaLL();
        LatLng pos2 = latLng.toPangeaLL();
        return pos1.distanceTo(pos2);
    }
     */

    @SuppressWarnings("StringBufferReplaceableByString")
    @NonNull
    public String toString() {
        return (new StringBuilder(60)).append("latLng: (").append(latitude).append(",")
                .append(longitude)
                .append(")").toString();
    }

    @NonNull
    public String toString(String fmt) {
        return String.format(fmt, latitude, longitude);
    }

    public boolean similar(@Nullable WLatLng other) {
        return other != null
                && similar(this.latitude, other.getLatitude())
                && similar(this.longitude, other.getLongitude());
    }

    public boolean similar(double d1, double d2) {
        int SCALE = 10000;
        return (long) (d1 * SCALE) == (long) (d2 * SCALE);
    }

}
