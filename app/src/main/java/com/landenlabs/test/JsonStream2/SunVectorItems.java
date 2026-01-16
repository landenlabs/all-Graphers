/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;


import static com.landenlabs.test.JsonStream2.UtilData.getIt;

import android.graphics.drawable.Icon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.landenlabs.all_graphers.ui.logger.ALog;
import com.landenlabs.test.Data.WLatLng;
import com.landenlabs.test.Data.WLatLngBounds;

import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Collection of SUN Vector data classes used by SunVectorLayer.
 */
public class SunVectorItems {

    // =============================================================================================
    public static class Properties {
        // Property names
        public static final String P_PHENOMENON = "phenomenon";     // Airmet/Sigmet
        // public static final String P_PHENOMENA = "phenomena";       // Alerts
        public static final String P_CATEGORY = "category";       // Alerts
        public static final String P_FLIGHT_RULE = "flight_rule";   // METAR
        public static final String P_SOURCE_ID = "source_id";       // TAF
        public static final String P_OCEAN_1 = "processing_source"; // SOFAR - Ocean Sensor
        public static final String P_OCEAN_2 = "payloadType";       // SOFAR - Ocean Sensor
        public static final String P_BALLON1 = "ballon1";           // WindBorne
        public static final String P_STORM_NAME = "storm_name";     // Tropical Track storm name
        public static final String P_STORM_ID = "storm_id";         // Tropical forecast line id

        // public String name;
        public Map<String, Object> properties;
        private static final Map<String, Object> noProperties = Collections.unmodifiableMap(new HashMap<>());

        public Properties() {
            properties = new HashMap<>(16);
        }

        public Properties(Map<String, Object> properties) {
            this.properties = properties;
        }

        // Optimized construction, sharing duplicate keys.
        public Properties(Map<String, Object> properties, Map<String,String> dupKeys) {
            this.properties = new HashMap<>(properties.size());
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                String dupKey = dupKeys.putIfAbsent(key, key);
                key = (dupKey != null) ? dupKey : key;
                this.properties.put(key , entry.getValue());
            }
        }

        @Override
        public int hashCode() {
            return properties.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Properties properties))
                return false;
            return hashCode() == properties.hashCode();
        }

        @Nullable
        public Object find(String findKey) {
            return UtilData.find(properties, findKey, null);
        }

        @NonNull
        public Map<String, Object> get(String key, int arrIdx)  {
            try {
                return asMap(asArray(find(key)).get(arrIdx));
            } catch (Exception ex) {
                return noProperties;
            }
        }

        String get(String key) {
            return getIt(properties, key, "");
        }

        public static ArrayList<Object> asArray(Object data) {
            return (ArrayList<Object>)data;
        }

        // @Nullable
        public static Map<String, Object> asMap(Object data) {
            return (Map<String, Object>)data;
        }
    }

    // =============================================================================================
    public enum GeoType { None, Point, Polygon, MultiPolygon, MultiLines, LineString;
        public static GeoType from(String str) {
            for (GeoType geoType : GeoType.values())
                if (geoType.name().equalsIgnoreCase(str))
                    return geoType;
            return None;
        }
    }

    // =============================================================================================
    public interface SunGeoItem {
        GeoType getType();

        /*
        int addToDrawList(
                @NonNull MapGroup drawList,
                @NonNull MapView mapView,
                @NonNull String sunName,
                int idx,
                WxStyle vectorStyle,
                int elev,
                @Nullable WLatLngBounds bounds);
        */

        boolean intersects(WLatLngBounds bounds);
        boolean contains(WLatLng point);
        WLatLng getCenter();
    }

    // =============================================================================================
    public static class SunGeoPoint implements SunGeoItem {
        public final WLatLng point;

        public SunGeoPoint(ArrayList<SunGeoPolygon> polygons) {
            this.point = polygons.get(0).points.get(0);
        }

        public SunGeoPoint(WLatLng point) {
            this.point = point;
        }

        @Override
        public GeoType getType() { return GeoType.Point; }

        /*
        @Override
        public int addToDrawList(
                @NonNull MapGroup drawList,
                @NonNull MapView mapView,
                @NonNull String sunName,
                int idx,
                WxStyle vectorStyle,
                int elev,
                @Nullable WLatLngBounds bounds) {

            if (vectorStyle instanceof WxStyleIcon styleIcon) {
                Icon icon = styleIcon.getIcon();
                if (icon != null) {
                    GeoPoint gp = new GeoPoint(point.latitude, point.longitude);
                    Marker marker = new Marker(gp, sunName + idx);
                    marker.setIcon(icon);
                    marker.setEditable(false);
                    marker.setMovable(false);
                    marker.setClickable(false);
                    drawList.addItem(marker);
                    return 1;
                }
            }
            return 0;
        }
        */

        @Override
        public boolean intersects(WLatLngBounds bounds) {
            return bounds.contains(point);
        }

        @Override
        public boolean contains(WLatLng point) {
            return point.equals(this.point);
        }

        @Override
        public WLatLng getCenter() {
            return point;
        }

        @Override
        public int hashCode() {
            return point.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof SunGeoPoint))
                return false;
            return point.equals(((SunGeoPoint)obj).point);
        }
    }

    // =============================================================================================
    // TODO - replace use of GeoPolygons with GeoPolygon, clone draw logic into GeoPolygon and GeoMultiPolygons
    public static class SunGeoPolygons implements SunGeoItem {

        public final ArrayList<SunGeoPolygon> polygons;

        public SunGeoPolygons(ArrayList<SunGeoPolygon> polygons) {
            // assert(Objects.equals(ArrayListEx.first(polygons, null), ArrayListEx.last(polygons, null)));
            this.polygons = polygons;
        }

        @Override
        public GeoType getType() { return GeoType.Polygon; }

        @Override
        public WLatLng getCenter() {
            WLatLngBounds bounds = getBounds();
            return (bounds != null) ? bounds.getCenter() : WLatLng.empty();
        }

        public WLatLngBounds getBounds() {
            WLatLngBounds bounds = null;
            if (polygons.size() > 0)
                bounds = polygons.get(0).bounds;
            for (SunGeoPolygon polygon : polygons) {
                bounds.add(polygon.bounds);
            }
            return bounds;
        }

        @Override
        public int hashCode() {
            return Objects.hash(polygons);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof SunGeoPolygons other))
                return false;
            return Objects.equals(this.polygons, other.polygons);
        }

        /*
        @Override
        public int addToDrawList(
                @NonNull MapGroup drawList,
                @NonNull MapView mapView,
                @NonNull String sunName,
                int idx,
                WxStyle vectorStyle,
                int elev,
                @Nullable WLatLngBounds bounds) {

            int cnt = 0;
            for (SunGeoPolygon polygon : polygons) {
                if (bounds == null || bounds.isOverlapping(polygon.bounds)) {
                    WxPolygon wxPolygon = new WxPolygon(mapView, sunName + idx++, vectorStyle, elev, polygon);
                    wxPolygon.setMovable(false);
                    wxPolygon.setEditable(false);
                    drawList.addItem(wxPolygon);
                    cnt++;
                }
            }
            return cnt;
        }
        */

        @Override
        public boolean intersects(WLatLngBounds bounds) {
            for (SunGeoPolygon polygon : polygons)
                if (polygon.bounds.isOverlapping(bounds))
                    return true;

            return false;
        }

        @Override
        public boolean contains(WLatLng point) {
            for (SunGeoPolygon polygon : polygons)
                if (polygon.bounds.contains(point))
                    return isInside(point, polygon.points);

            return false;
        }

        public static boolean isInside(@NonNull WLatLng point, @NonNull ArrayList<WLatLng> points) {
            if (points.size() < 3)
                return false;

            double lon = point.longitude;
            double lat = point.latitude;
            boolean inside = false;

            int numPnts = points.size();
            WLatLng vertex1 = points.get(numPnts-1);
            WLatLng vertex2;

            for (int idx = 0; idx < numPnts; idx++) {
                vertex2 = points.get(idx);
                if (((vertex1.latitude > lat) != (vertex2.latitude > lat)) &&
                        (lon < (vertex2.longitude - vertex1.longitude)
                                * (lat - vertex1.latitude) / (vertex2.latitude - vertex1.latitude) + vertex1.longitude)) {
                    inside = !inside;
                }
                vertex1 = vertex2;
            }

            if ( ! inside)
                ALog.d.tagMsg(ALog.TAG_PREFIX, "point not inside polygon");

            return inside;
        }
    }

    // =============================================================================================
    public static class SunGeoMultiPolygons extends SunGeoPolygons {

        public SunGeoMultiPolygons(ArrayList<SunGeoPolygon> polygons) {
            super(polygons);
        }

        @Override
        public GeoType getType() { return GeoType.MultiPolygon; }
    }

    // =============================================================================================
    public static class SunGeoMultiLines extends SunGeoPolygons {

        public SunGeoMultiLines(ArrayList<SunGeoPolygon> polygons) {
            super(polygons);
        }

        @Override
        public GeoType getType() { return GeoType.MultiLines; }
    }

    // =============================================================================================
    public static class SunGeoPolygon {
        public final WLatLngBounds bounds;
        public final ArrayList<WLatLng> points;

        public SunGeoPolygon(ArrayList<WLatLng> points) {
            this.points = points;
            bounds = new WLatLngBounds(points);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bounds, points.size());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof SunGeoPolygon other))
                return false;
            return this.bounds.equals(other.bounds) && points.size() == other.points.size();
        }
    }

    // =============================================================================================
    @SuppressWarnings("SameParameterValue")
    public static class SunItem {
        public final String id;
        public final Properties properties;
        public final SunGeoItem geoItem;
        public final String prodId;

        public SunItem(String id, Properties prop, SunGeoItem geoItem, String prodId) {
            this.id = id;
            this.properties = prop;
            this.geoItem = geoItem;
            this.prodId = prodId;
        }

        @Nullable
        public Object getValue(@NonNull String key) {
            return properties.properties.get(key);
        }

        @NonNull
        public String getString(@NonNull String key) {
            return getStringDef(key, "");
        }

        public String getStringDef(@NonNull String key, String defValue) {
            Object obj = getValue(key);
            if (obj instanceof String)
                return obj.toString();
            return defValue;
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, geoItem);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof SunItem other))
                return false;
            return this.properties.equals(other.properties)
                    && this.geoItem.equals(other.geoItem);
        }

        private static final long PERIOD_METAR_MILLI = TimeUnit.HOURS.toMillis(1);

        public boolean isValid() {
            return   true;
        }

        /*
        // Validate utc milliseconds.
        //  ex:    "validTime": 1745859600000,
        private boolean isValidTimeMilli(String propKey, long periodMilli) {
            long validMilli = parseLong(getValue(propKey));
            boolean ok = (validMilli + periodMilli) >= System.currentTimeMillis();
            // if (!ok)
            //     ALog.d.tagMsg(this, "expired milli", validMilli, " ", new WxDateTime(validMilli));
            return ok;
        }

        // Validate time in ISO 8601 format
        //  ex:   "valid_time": "2025-04-28T17:00:00+00:00",
        private boolean isValidTime8601(String propKey) {
            WxDateTime validDt = parseIso8601(getString(propKey));
            return validDt != null && validDt.isAfterNow();
        }
        */

        public boolean append(SunItem item, String mergeKey) {
            if (Objects.equals(id, item.id)) {
                ArrayList<Object> mergeInto = Properties.asArray(getValue(mergeKey));
                ArrayList<Object> mergeFrom =
                    Properties.asArray(item.properties.properties.get(mergeKey));
                if (mergeInto != null && mergeFrom != null) {
                    mergeInto.addAll(mergeFrom);
                    return true;
                }
            }
            return false;
        }
    }
}
