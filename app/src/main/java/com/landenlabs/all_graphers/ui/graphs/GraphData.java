/*
 *
 * Copyright © 2024 The Weather Company. All rights reserved.
 *
 */

package com.landenlabs.all_graphers.ui.graphs;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.landenlabs.all_graphers.ui.data.CacheElev;
import com.landenlabs.all_graphers.ui.data.DataRange;
import com.landenlabs.all_graphers.ui.data.DataUnit;
import com.landenlabs.all_graphers.ui.data.ElevData;
import com.landenlabs.all_graphers.ui.data.Units;
import com.landenlabs.all_graphers.ui.data.WxDataHolder;

import java.util.Locale;

/**
 * Provide raw material to build graph -
 *      x axis labels
 *      x axis colors
 *      y axis labels
 *      Unit and unit conversion
 *      Data point loader
 */
public class GraphData  {

    public static boolean NORMALIZE_X_AXIS = false;
    public static boolean COLOR_X_AXIS = true;

    public GraphType graphType = GraphType.None;
    public String localizedTitle;
    public long dataMilli = 0;

    private WxDataHolder wx;

    public  final float[] yRange = new float[] {1000f, 10000f};
    public  final String[] yLabels = new String[] {
            "10,000'",
            "9,000'",
            "8,000'",
            "7,000'",
            "6,000'",
            "5,000'",
            "4,000'",
            "3,000'",
            "2,000'",
            "1,000'",
    };

    private static final DataRange iceRange = new DataRange(0, 1f);
    private static final String[] iceLabels = new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
    // https://catalog-explorer-useast1.qa.ssds.weather.com/v2/catalogui/#/tilepaletter/palette/fip_cm
    private static final int[] iceColors = new int[] {
            Color.argb(255, 25, 110, 110),      // 0 was transparent
            Color.argb(255, 51, 225, 225),      // 1
            Color.argb(225, 26, 125, 251),      // 2
            Color.argb(255, 101, 51, 150)       // 4
    };
    private static final DataRange rhRange = new DataRange(0, 100);
    private static final String[] rhLabels = new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
    // https://catalog-explorer-useast1.qa.ssds.weather.com/v2/catalogui/#/tilepaletter/palette/jefferson_relativeHumidity
    private static final int[] rhColors = new int[]{
            Color.argb(255,0,25,0), // 0
            Color.argb(255,0,25,0), // 10
            Color.argb(255,0,51,0), // 20
            Color.argb(255,0,76,0), // 30
            Color.argb(255,0,102,0), // 40
            Color.argb(255,0,127,0), // 50
            Color.argb(255,0,153,0), // 60
            Color.argb(255,0,178,0), // 70
            Color.argb(255,0,204,0), // 80
            Color.argb(255,0,229,0), // 90
            Color.argb(255,0,254,0), // 100
    };
    private static final DataRange turbRange = new DataRange(0, 7f);
    private static final String[] turbLabels = new String[] { "None", "Light", "Moderate", "Severe" }; // TODO - localize
    // https://catalog-explorer-useast1.qa.ssds.weather.com/v2/catalogui/#/tilepaletter/palette/turbulence_cm
    private static final int[] turbColors = new int[] {
            Color.argb(255, 255, 205, 46),      // 0 was transparent
            Color.argb(255, 255, 205, 46),      // 1
            Color.argb(255, 255, 205, 46),      // 2
            Color.argb(255, 255, 156, 0),       // 3
            Color.argb(255, 255, 119, 1),       // 4
            Color.argb(255, 226, 72, 0),        // 5
            Color.argb(255, 226, 72, 0),        // 6
            Color.argb(255, 226, 72, 0)         // 7
    };


    // These need to be dynamic and computed from the data.
    private static final DataRange tempRangeF = new DataRange( -30, 70 );   // Fahrenheit
    private static final String[] tempLabelsF = new String[] { "-30", "-20", "-10", "0", "10", "20", "30", "40", "50", "60", "70"};
    private static final DataRange tempRangeC = new DataRange( -35, 20 );   // Celsius
    private static final String[] tempLabelsC = new String[] { "-35", "-30", "-25", "-20", "-15", "-10", "-5", "0", "5", "10", "15", "20"};
    // https://catalog-explorer-useast1.qa.ssds.weather.com/v2/catalogui/#/tilepaletter/palette/temp
    private static final int[] tempColors = new int[] {
            Color.argb(255, 160, 130, 190),     // -30
            Color.argb(255, 200, 170, 220),     // -20
            Color.argb(255, 110, 0, 70),        // -10
            Color.argb(255, 160, 50, 140),      // 0
            Color.argb(255, 205, 95, 200),      // 10
            Color.argb(255, 170, 225, 250),     // 20
            Color.argb(255, 100, 15, 190),      // 30
            Color.argb(255, 20, 20, 150),       // 40
            Color.argb(255, 115, 105, 100),     // 50
            Color.argb(255, 215, 215, 50),      // 60
            Color.argb(255, 220, 150, 0),       // 70
    };


    private static final DataRange windRange = new DataRange( 0, 70 );     // mph
    private static final String[] windLabels = new String[] { "0", "10",  "20",  "30",  "40", "50", "60", "70"};
    // https://catalog-explorer-useast1.qa.ssds.weather.com/v2/catalogui/#/tilepaletter/palette/windspeed
    private static final int[] windColors = new int[] {
            Color.argb(255, 137, 223, 255),     // 0
            Color.argb(255, 90, 210, 255),      // 5
            Color.argb(255, 0, 184, 255),       // 10
            Color.argb(255, 0, 144, 255),       // 15
            Color.argb(255, 25, 120, 255),      // 20
            Color.argb(255, 89, 114, 253),      // 25
            Color.argb(255,125, 106, 248),      // 30
            Color.argb(255, 153, 97, 240),      // 35
            Color.argb(255, 177, 87, 230),      // 40
            Color.argb(255, 197, 75, 217),      // 45
            Color.argb(255, 215, 61, 203),      // 50
            Color.argb(255, 230, 45, 186),      // 55
            Color.argb(255, 241, 26, 169),      // 60
            Color.argb(255, 250, 0, 150),       // 70  (same color repeats for higher winds)
    };


    public String[]     xLabels = windLabels;
    public DataRange    xRange = windRange;
    public int[]        colors = windColors;
    private CacheElev   wxCacheElev;
    private String      wxProdname;
    private int         wxProdCode;
    public final DataUnit dataUnit = new DataUnit();

    public GraphData(@NonNull Context context, @NonNull WxDataHolder wx) {
        this.wx = wx;
    }

    public void disposeImpl() {
    }

    public static boolean isTemperatureMetric() {
        return false;
    }

    public void setType(@NonNull GraphType graphType) {
        assert (graphType != GraphType.None);
        this.graphType = graphType;

        switch (graphType) {
            case IceProbability:
                // xAxis 0 .. 100
                localizedTitle = "% max icing probability";
                xLabels = iceLabels;
                colors = iceColors;
                //xx wxCacheElev = wx.icingData;
                wxProdname = "FIPaltitudeabovemsl";
                wxProdCode = ElevData.BASE_PROD_IC;
                dataUnit.unit = Units.Percent;
                dataUnit.inRange = xRange = iceRange;
                break;
            case Humidity:
                // xAxis 0 .. 100
                localizedTitle = "%";
                xLabels = rhLabels;
                colors = rhColors;
                //xx wxCacheElev = wx.humidityData;
                wxProdname = "RelativeHumidity";
                wxProdCode = ElevData.BASE_PROD_TH;
                dataUnit.unit = Units.Percent;
                dataUnit.inRange = xRange = rhRange;
                break;
            case Temperature:
                // xAxis dynamic
                localizedTitle = isTemperatureMetric() ?  "°C" : "°F";

                colors = tempColors;
                //xxw xCacheElev = wx.temperatureData;
                wxProdname =  "Temperature";
                wxProdCode = ElevData.BASE_PROD_TH;
                dataUnit.unit = Units.TemperatureKelvin;
                dataUnit.inMetric = true;   // kelvin
                dataUnit.outMetric = isTemperatureMetric();
                if (dataUnit.outMetric) {
                    dataUnit.inRange = xRange = tempRangeC;
                    xLabels = tempLabelsC;
                } else {
                    dataUnit.inRange = xRange = tempRangeF;
                    xLabels = tempLabelsF;
                }
                break;
            case Turbulence:
                // xAxis Light, Moderate, Severe, Extreme
                /*
                Nick -
                    Severe = EDR equal to or above .45
                    Moderate = EDR is equal to or above .20 and below .45
                    Light = EDR is above .10 and below .20
                    None = EDR is less than or equal to .10

                 Ryan -
                   0 roughly corresponds to smooth flying conditions,
                   1 is occasional light turbulence,
                   2 is light turbulence,
                   3 is light to moderate turbulence,
                   4 is moderate turbulence,
                   5 is moderate to severe turbulence,
                   6 is severe turbulence,
                   7 is extreme turbulence..."
                 */
                localizedTitle = "Intensity";
                xLabels = turbLabels;
                colors = turbColors;
                //xx wxCacheElev = wx.turbulenceData;
                wxProdname =  "GTGaltitudeabovemsl";
                wxProdCode = ElevData.BASE_PROD_TR;
                dataUnit.unit = Units.None;
                dataUnit.inRange = xRange = turbRange;
                break;
            case WindSpeed:
                // xAxis dynamic
                localizedTitle = "Knots";   // TODO - change based on units
                xLabels = windLabels;
                colors = windColors;
                //xx wxCacheElev = wx.windSpeedData;
                wxProdname =  "WindSpeed";
                wxProdCode = ElevData.BASE_PROD_WS;
                dataUnit.unit = Units.SpeedKnots;
                dataUnit.inMetric = true;   // meters per second
                dataUnit.outMetric = false; // settings.isTemperatureMetric();
                dataUnit.inRange = xRange = windRange;
                break;
        }
    }

    /**
     * Load data in background threads and decrease counter as data is acquired.
     */
    /* xx
    public WxObsCounter<ElevData> getDataAsync(long timeMilli) {
        WxLocation location = wx.getLocation();
        dataMilli = timeMilli;
        WxObsCounter<ElevData> counter = new WxObsCounter<>(yLabels.length);
        int minFeet = (int)yRange[0];
        int maxFeet = (int)yRange[1];
        int stepFeet = Math.round((yRange[1] - yRange[0]) / (yLabels.length -1));
        wxCacheElev.clear();
        for (int elevFeet = minFeet; elevFeet <= maxFeet; elevFeet += stepFeet) {
            ALog.d.tagMsg(this, wxProdname, " Request elev level=", elevFeet);
            wx.loadElev(timeMilli, wxCacheElev, location, wxProdname, wxProdCode, dataUnit, elevFeet, counter);
        }
        return counter;
    }
    */

    public String getXLabels(int xIdx, float value, int step) {
        if (NORMALIZE_X_AXIS) {
            if (xRange.distance() < 10) {
                int idx = (int) (value - xRange.minValue);
                return xLabels[Math.max(0, Math.min(xLabels.length - 1, idx))];
            }
            return String.format(Locale.US, "%d", (int) (value / step) * step);
        } else {
            return  xLabels[xIdx];
        }
    }
}
