/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

import static android.text.TextUtils.isEmpty;
import static com.landenlabs.test.JsonStream2.JsonTokens.END_MAP;
import static com.landenlabs.test.JsonStream2.JsonTokens.START_ARRAY;
import static com.landenlabs.test.JsonStream2.JsonTokens.START_MAP;
import static com.landenlabs.test.JsonStream2.UtilData.asDouble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.landenlabs.all_graphers.ui.logger.ALog;
import com.landenlabs.test.Data.WLatLng;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Populate SunVectorData from a Json 'stream' parser.
 */
public class SunVectorBldJstream {
    public String prodId;

    protected ArrayList<SunVectorItems.SunItem> workingItems;
    protected ArrayList<SunVectorItems.SunGeoPolygon> polygons;
    protected ArrayList<WLatLng> coords;
    protected ArrayList<Object> workingProps;

    protected String idStr = "";
    protected String typeStr = "";
    protected String geoTypeStr = "";
    protected WLatLng wLatLng;
    protected int arrayDepth = 0;
    protected SunVectorItems.Properties prop;

    protected Map<String, String> commonStr = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    synchronized
    public SunVectorData parse(@NonNull byte[] jsonBytes, @Nullable String prodId, int[] xyz)   {
        this.prodId = prodId;
        initBuffers();
        new JsonStreamBytes(jsonBytes,
                (streamer, name, value, type) -> {
                    if (START_ARRAY == value && "features".equals(name))
                        streamer.push(this::onFeature, "onFeature");
                }, SunVectorBldJstream::onError);

        // ALog.d.tagFmt(this, "Parse completed %s, inSize=%d, outCnt=%d ", prodId, jsonBytes.length, workingItems.size());

        // Make copy of workingItems to allocate exact required memory
        SunVectorData result = new SunVectorData(prodId, xyz, new ArrayList<>(workingItems));
        releaseBuffers();  // Release memory
        return result;
    }

    void initBuffers() {
        if (workingItems == null) {
            // Create generous working arrays to avoid incremental growth overhead.
            workingItems = new ArrayList<>(64);
            polygons = new ArrayList<>(100);
            coords = new ArrayList<>(1000);
            workingProps = new ArrayList<>(2);  // Array member to Properties.
        } else {
            workingItems.clear();
            polygons.clear();
            coords.clear();
            workingProps.clear();
        }
    }

    public void releaseBuffers() {
        workingItems = null;
        workingProps = null;
        polygons = null;
        coords = null;
    }

    // ---------------------------------------------------------------------------------------------

    static void onError(int pos, String msg) {
        ALog.e.tagFmt(ALog.TAG_PREFIX, "Failed at %d, msg=%s\n", pos, msg);
    }

    private void onFeature(JsonStream streamer, String name, Object value, int typeIdx) {
        if (START_ARRAY == value && "coordinates".equals(name)) {
            streamer.push(this::onCoordinates, "onCoord");
            arrayDepth = 0;         // not balanced, startArray is one less than endArray, must reset manually.
            maxArrayDepth = 10;     // Set to large value so comparison fails unless initialized correctly.
            polygons.clear();
            coords.clear();
        } else if (START_MAP == value && "properties".equals(name)) {
            geoTypeStr = typeStr;
            prop = new SunVectorItems.Properties();
            streamer.push(this::onProperties, "onProp");
        } else if (name.equals("type") && typeIdx == JsonTokens.T_MAP) {
            typeStr = toString(value);
        } else if (name.equals("id") && typeIdx == JsonTokens.T_MAP) {
            idStr = toString(value);
        } else if (JsonTokens.END_ARRAY == value) {
            // System.out.println("End array");
        } else if (END_MAP == value && JsonTokens.N_ARRAY.equals(name)) {
            if (prop != null && ! polygons.isEmpty()) {
                try {
                    SunVectorItems.GeoType geoTyp = SunVectorItems.GeoType.from(geoTypeStr);
                    SunVectorItems.SunGeoItem geoItem = switch (geoTyp) {
                        case Point -> new SunVectorItems.SunGeoPoint(polygons);
                        case Polygon ->  new SunVectorItems.SunGeoPolygons(new ArrayList<>(polygons));
                        case MultiPolygon ->  new SunVectorItems.SunGeoMultiPolygons(new ArrayList<>(polygons));
                        case MultiLines, LineString ->  new SunVectorItems.SunGeoMultiLines(new ArrayList<>(polygons));
                        default -> null;
                    };
                    if (geoItem != null) {
                        if (isEmpty(idStr))
                            idStr = (String)prop.properties.get("native_site_id");
                        workingItems.add(new SunVectorItems.SunItem(idStr, prop, geoItem, prodId));
                    } else {
                        ALog.e.tagMsg(this,  "Json parse failed missing GeoItem for type=", geoTypeStr);
                    }
                } catch (Exception ex) {
                    ALog.e.tagMsg(this, "Json parse failed type=", geoTypeStr, " ex=", ex);
                }
            } else
                ALog.e.tagMsg(this, "Missing prop=", prop, " or polygons=", polygons);

            // prop = null;
            polygons.clear();
            idStr = null;
        }
    }

    private  int maxArrayDepth = 10;
    private  void onCoordinates(JsonStream streamer, String name, Object value, int typeIdx) {
        // ALog.d.tagMsg("Coordinate-Json depth=", arrayDepth, " max=", maxArrayDepth, " name=", name, " value=", value);

        if (START_MAP == value) {
            // System.out.println("Start map");
        } else if (END_MAP == value) {
            // System.out.println("End map");
        } else if (START_ARRAY == value) {
            // if (arrayDepth == 0)
            //    coords.clear();
            arrayDepth++;
        } else if (JsonTokens.END_ARRAY == value) {
            if (maxArrayDepth == 0) {
                // Point data.
            //    coords.add(wLatLng);
                polygons.add(new SunVectorItems.SunGeoPolygon(new ArrayList<>(coords)));
                coords.clear();
                streamer.pop();
            } else if (arrayDepth == maxArrayDepth) {
            //    coords.add(wLatLng);
            } else if (arrayDepth == maxArrayDepth -1 ) {
                polygons.add(new SunVectorItems.SunGeoPolygon(new ArrayList<>(coords)));
                coords.clear();
                if (--maxArrayDepth == 0)
                    streamer.pop();
            } else if (arrayDepth == 0) {
                streamer.pop();
            }
            --arrayDepth;
        } else {
            double valueF = asDouble(value, 0.0);   // May be Double or Long

            // double valueF = ((JDouble)value).dValue;   // See JsonStreamXXXX for use and implementation.
            if (typeIdx == 0) {
                maxArrayDepth = arrayDepth;
                wLatLng = new WLatLng(0, valueF);
            } else {
                wLatLng.latitude = valueF;
                coords.add(wLatLng);
            }
        }
    }

    @Nullable
    public static String toString(Object value) {
        return (value != null) ? value.toString() : null;
    }

    boolean inArray = false;
    ArrayDeque<SunVectorItems.Properties> nestedProp = new ArrayDeque<>();
    private  void onProperties(JsonStream streamer, String name, Object value, int typeIdx) {
        if (JsonTokens.IGNORE == value)
            return;

        if (START_MAP == value) {
            nestedProp.push(prop);
            prop = new SunVectorItems.Properties();
        } else if (END_MAP == value) {
            if (!nestedProp.isEmpty()) {
                SunVectorItems.Properties innerProp = prop;
                prop = nestedProp.pop();
                if (inArray)
                    workingProps.add(innerProp);
                else
                    prop.properties.put(name, innerProp);
            } else {
                streamer.pop();
            }
        } else if (START_ARRAY == value) {
            inArray = true; // Does not support nested array, map, array sequence
            workingProps.clear();
        } else if (JsonTokens.END_ARRAY == value) {
            inArray = false;
            prop.properties.put(name, new ArrayList<>(workingProps));
        } else if (JsonTokens.N_ARRAY.equals(name)) {
            workingProps.add(value);
        } else {
            // Share strings used for key name to reduce memory.
            name = commonStr.computeIfAbsent(name, k -> {return k;});
            // if (name != name.intern()) {
            //     System.out.println("not interned " + name);
            // }
            prop.properties.put(name, value);
        }
    }
}
