/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;


import static com.landenlabs.all_graphers.ui.data.StrUtils.hasText;

import android.graphics.PointF;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Elevation point query response.
 */
public class ElevData {

    public static final int MAX_VALUES = (ElevData.MAX_FEET - ElevData.MIN_FEET) / ElevData.STEP_FEET;
    public float[] values = new float[MAX_VALUES]; // [0]=surface ... [10] = 10,000' feet
    public WLatLng latLng;

    public long runtimeSec;        // epoch seconds
    public long timeSec;           // epoch seconds
    //xx public SunTimeSet sunTimeSet;

    public String prodCode;     // ex:8200
    public String prodName;     // ex:RelativeHumidityAtFL310
    public DataUnit unit;

    public ElevData() {
        clear();
    }

    synchronized
    public void clear() {
        Arrays.fill(values, Float.NaN);
    }

    private static void merge(float[] dst, float[] srcFirst, float[] srcSecond) {
        assert (dst.length == srcFirst.length && dst.length == srcSecond.length);
        for (int idx = 0; idx < dst.length; idx++) {
            dst[idx] =  Float.isNaN(srcFirst[idx]) ? srcSecond[idx] : srcFirst[idx];
        }
    }

    synchronized
    public ElevData merge(ElevData otherData) {
        if (otherData != null && otherData.latLng.equals(latLng)) {
            merge(values, values, otherData.values);
            //xx ALog.d.tagMsg(this, "elev merge numValues=", numValues(), " @=", latLng);
        } else {
            //xx ALog.d.tagMsg(this, "elev no merge @=", latLng);
        }
        return this;
    }

    synchronized
    public int numValues() {
        int num = 0;
        for (float value : values) {
            if ( ! Float.isNaN(value))
                num++;
        }
        return num;
    }

    synchronized
    public ArrayList<PointF> getPoints(boolean outMetric) {
        ArrayList<PointF> points = new ArrayList<>();
        for (int idx = 0; idx < values.length; idx++) {
            float value = values[idx];
            if ( ! Float.isNaN(value)) {
                value = unit.convert(value, outMetric);
                points.add(new PointF(value, idxToFeet(idx)));
            }
        }
        return points;
    }

    public boolean isValid() {
        return hasText(prodCode) && hasText(prodName) && latLng != null && timeSec > 0 && runtimeSec > 0;
    }

    public boolean isValid(long epochSec, WLatLng latLng, String prodCode, int elevFeet) {
        return hasText(this.prodCode)
                && this.prodCode.equals(prodCode)
                && isNear(epochSec, this.timeSec)
                && isNear(latLng, this.latLng)
                && hasValue(elevFeet);
    }

    public boolean hasValue(int elevFeet) {
        int elevIdx = feetToIdx(elevFeet);
        return elevIdx >= 0 && elevIdx < values.length && ! Float.isNaN(values[elevIdx]);
    }

    public static int feetToIdx(int elevFeet) {
        return (elevFeet - MIN_FEET) / STEP_FEET;
    }
    public static int idxToFeet(int elevIdx) {
        return elevIdx * STEP_FEET + MIN_FEET;
    }

    public static String feetToFL(int elevFeet) {
        // FL010 = 1,000 feet,   FL300 = 30,000 feet.
        return String.format("%03d", elevFeet / STEP_FL);
    }
/*
    public long getBeginSec(boolean online) {
        SunTimeSet.FrameTime frameTime = sunTimeSet.getFrameTime(0, online);
        return frameTime != null ? frameTime.epochSec : 0;
    }

    public long getEndSec(boolean online) {
        SunTimeSet.FrameTime frameTime = sunTimeSet.getFrameTime(1, online);
        return frameTime != null ? frameTime.epochSec : 0;
    }
*/
    static boolean isNear(long sec1, long sec2) {
        return Math.abs(sec1 - sec2) < TimeUnit.MINUTES.toSeconds(5);
    }
    static boolean isNear(WLatLng latLng1, WLatLng latLng2) {
        double km = kilometersBetweenLatLng(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude);
        return km < 10;
    }

    public static final float EARTH_RADIUS_METERS = 6378137f; // meters WGS84 Major axis
    public static double kilometersBetweenLatLng(double latDeg1, double lngDeg1, double latDeg2, double lngDeg2) {
        double dValue = Math.sin(Math.toRadians(latDeg1))
                * Math.sin(Math.toRadians(latDeg2))
                + Math.cos(Math.toRadians(latDeg1))
                * Math.cos(Math.toRadians(latDeg2))
                * Math.cos(Math.toRadians(lngDeg2 - lngDeg1));

        // Fix to work around Android math error when same points [30.44, -91.19] generate NaN.
        // Clamp dValue to <= 1.0 to prevent Not-a-number
        double ans =  EARTH_RADIUS_METERS / 1000.0 * Math.acos(Math.min(dValue, 1.0));
        if (Double.isNaN(ans)) {
            return 0;
        }

        return ans;
    }

    /*
    Level   Feet    WndSpd   Temp/Humidity
    FL010	01000    8270		8170
    ...
    FL500	50000    8319		8219
     */

    // Request times
    public static final int TIME_PROD_TH = 8200;
    public static final int TIME_PROD_WS = 8300;

    // Add to FEET_TO_PROD to get product number for a flight level.
    public static final int BASE_PROD_TH = 8170;  // Temperature and Humidity
    public static final int BASE_PROD_WS = 8270;  // Windspeed
    public static final int BASE_PROD_IC = 1900;  // Ice potential
    public static final int BASE_PROD_TR = 1950;
    public static final int STEP_FEET =  1000;
    public static final int MIN_FEET =  1000;
    public static final int MAX_FEET = 50000;
    public static final int STEP_FL =  100;       // Flight level FL010 = 1000 feet

}

