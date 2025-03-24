package com.landenlabs.test.JsonStream1;

import androidx.annotation.Nullable;

import com.landenlabs.test.Data.PolyItems.*;
import com.landenlabs.test.Data.SunVectorDataI;
import com.landenlabs.test.Data.WLatLng;
import org.joda.time.DateTime;

import java.util.ArrayList;


public class SunVectorData  implements SunVectorDataI  {
    public ArrayList<Item> items;

    /*
    private static final ArrayList<Item> workingItems = new ArrayList<>();
    private static final ArrayList<GeoPolygon> polygons = new ArrayList<>(10);
    private static final ArrayList<WLatLng> coords = new ArrayList<>(100);
    */
    private static  ArrayList<Item> workingItems;
    private static  ArrayList<GeoPolygon> polygons;
    private static  ArrayList<WLatLng> coords;

    private static String type = "";
    private static String geoType = "";
    private static WLatLng wlatLng;
    private static int arrayDepth = 0;
    public static Properties prop;

    // ---------------------------------------------------------------------------------------------

    public SunVectorData(ArrayList<Item> items) {
        this.items = items;
    }

    private static void onError(int pos, String msg) {
        System.err.printf("Failed at %d, msg=%s\n", pos, msg);
    }

    private static void onFeature(JsonStream streamer, String name, Object value, int typeIdx) {
        if (JsonStreamString.START_ARRAY == value && "coordinates".equals(name)) {
            streamer.push(SunVectorData::onCoordinates, "onCoord");
            arrayDepth = 0; // not balanced, startArray is one less than endArray, must reset manually.
            maxArrayDepth = 10;
            polygons.clear();
        } else if (JsonStreamString.START_MAP == value && "properties".equals(name)) {
            geoType = type;
            prop = new Properties();
            streamer.push(SunVectorData::onProperties1, "onProp");
        } else if (name.equals("type") && typeIdx == JsonStreamString.T_MAP) {
            type = toString(value);
        } else if (JsonStreamString.END_ARRAY == value) {
            // System.out.println("End array");
        } else if (JsonStreamString.END_MAP == value &&  JsonStreamString.N_ARRAY == name) {
            if (prop != null && polygons.size() > 0)
                workingItems.add(new Item(prop, new GeoPolygons(new ArrayList<>(polygons))));
            else
                System.err.println("Missing prop=" + dbgObject(prop) + " or polygons=" + dbgObject(polygons));
            prop = null;
            polygons.clear();
        }
    }

    private static String dbgObject(Object obj) {
        return (obj == null) ? "null" : obj.getClass().getSimpleName();
    }

    private static int maxArrayDepth = 10;
    private static void onCoordinates(JsonStream streamer, String name, Object value, int typeIdx) {
        // System.out.printf("%d: %s", streamer.getLevel(), " ".repeat(streamer.getLevel()));
        if (JsonStreamString.START_MAP == value) {
            // System.out.println("Start map");
        } else if (JsonStreamString.END_MAP == value) {
            // System.out.println("End map");
        } else if (JsonStreamString.START_ARRAY == value) {
            arrayDepth++;
            if (arrayDepth == 2) {

            } else if (arrayDepth == 1)
                coords.clear();

        } else if (JsonStreamString.END_ARRAY == value) {
            if (arrayDepth == maxArrayDepth) {
                coords.add(wlatLng);
            } else if (arrayDepth == maxArrayDepth -1 ) {
                polygons.add(new GeoPolygon(new ArrayList<>(coords)));
                coords.clear();
            } else if (arrayDepth == 0) {
                streamer.pop();
            }
            --arrayDepth;
        } else {
            double valueF = (Double)value;
            // double valueF = ((JDouble)value).dValue;
            if (typeIdx == 0) {
                maxArrayDepth = arrayDepth;
                wlatLng = new WLatLng(0, valueF);
            } else
                wlatLng.latitude = valueF;
        }
    }

    @Nullable
    public static DateTime getDateTime(Object value) {
        return (value != null) ? new DateTime(value.toString()) : null;
    }

    @Nullable
    public static String toString(Object value) {
        return (value != null) ? value.toString() : null;
    }
    @Nullable
    public static String toSpecialString(Object value) {
        return (value != null) ? value.toString().replace("\\n", "\n") : null;
    }

    @Nullable
    public static Long getLong(Object value) {
        return (value instanceof Long) ? (Long)value : null;
        /*
        try {
            if (value != null)
                return Long.parseLong(value.toString());
        } catch (Exception ignore) {
        }
        return null;
         */
    }

    @Nullable
    public static Double getDouble(Object value) {
        return (value instanceof Number) ? ((Number)value).doubleValue() : null;
        /*
        try {
            if (value != null)
                return Double.parseDouble(value.toString());
        } catch (Exception ignore) {
        }
        return null;
         */
    }


    private static void onProperties1(JsonStream streamer, String name, Object value, int typeIdx) {
        if (JsonStreamString.START_MAP == value) {
            prop = new Properties();
        } else if (JsonStreamString.END_MAP == value) {
            streamer.pop();
        } else {
            switch (name) {
                case "active_at":
                    prop.activeTime = getDateTime(value);
                    break;
                case "issue_time":
                    prop.issueTime = getDateTime(value);
                    break;
                case "expire_at":
                    prop.expireTime = getDateTime(value);
                    break;
                case "continuation_time":
                    prop.continuationTime = getDateTime(value);
                    break;
                case "validTime":
                    prop.validTMilli = getLong(value);
                    break;
                case "airmet_id":
                    prop.id = toString(value);
                    break;
                case "airmet_category":
                    prop.category = toString(value);
                    break;
                case "phenomenon":
                    prop.phenomenon = toString(value);
                    break;
                case "type":
                    prop.type = toString(value);
                    break;
                case "raw_type":
                    prop.rawType = toString(value);
                    break;
                case "icao_site_id":
                    prop.icao = toString(value);
                    break;
                case "issuing_station":
                    prop.issuingStation = toString(value);
                    break;
                case "lower_level":
                    prop.lowerLevel = toString(value);
                    break;
                case "upper_level":
                    prop.upperLevel = toString(value);
                    break;
                case "moving_speed":
                    prop.movingSpeed = getDouble(value);
                    break;
                case "moving_dir":
                    prop.movingDir = getDouble(value);
                    break;
                case "data":
                    prop.data = toSpecialString(value);
                    break;

                    // TODO - remove these cases
                case "clusterCount":
                case "fir_ids":
                case JsonStreamString.N_ARRAY:
                    break;
                default:
                    System.out.println("Ignoring " + name);
            }
        }
    }

    interface MapSetIt {
        void setIt(Object value);
    }
    private static class MapSetter {
        public int hashCode;
        public MapSetIt mapSetIt;
        public MapSetter(int hashCode, MapSetIt mapSetIt) {
            this.hashCode = hashCode;
            this.mapSetIt = mapSetIt;
        }
    }

    private static int mapSetterIdx = 0;
    private static MapSetter[]  MAP_SETTERS = new MapSetter[] {
            new MapSetter("upper_level".hashCode(), val -> prop.upperLevel = toString(val)),
            new MapSetter("phenomenon".hashCode(), val -> prop.phenomenon = toString(val)),
            new MapSetter("raw_type".hashCode(), val -> prop.rawType = toString(val)),
            new MapSetter("continuation_time".hashCode(), val -> prop.continuationTime = getDateTime(val)),
            new MapSetter("active_at".hashCode(), val -> prop.activeTime = getDateTime(val)),
            new MapSetter("icao_site_id".hashCode(), val -> prop.icao = toString(val)),
            new MapSetter("issue_time".hashCode(), val -> prop.issueTime = getDateTime(val)),
            new MapSetter("clusterCount".hashCode(), null  ),
            new MapSetter("issuing_station".hashCode(), val -> prop.issuingStation = toString(val)),
            new MapSetter("airmet_category".hashCode(), val -> prop.category = toString(val)),
            new MapSetter("moving_speed".hashCode(), val -> prop.movingSpeed = getDouble(val)),
            new MapSetter("type".hashCode(), val -> prop.type = toString(val)),
            new MapSetter("lower_level".hashCode(), val -> prop.lowerLevel = toString(val)),
            new MapSetter("fir_ids".hashCode(), null  ),
            new MapSetter("validTime".hashCode(), val -> prop.validTMilli = getLong(val)),
            new MapSetter("moving_dir".hashCode(), val -> prop.movingDir = getDouble(val)),
            new MapSetter("airmet_id".hashCode(), val -> prop.id = toString(val)),
            new MapSetter("data".hashCode(), val -> prop.data = toSpecialString(val)),
            new MapSetter("expire_at".hashCode(), val -> prop.expireTime = getDateTime(val)),
            new MapSetter(JsonStreamString.N_ARRAY.hashCode(), null  ),
    };

    private static void onProperties2(JsonStream streamer, String name, Object value, int typeIdx) {
        // try {
            if (JsonStreamString.START_MAP == value) {
                prop = new Properties();
            } else if (JsonStreamString.END_MAP == value) {
                streamer.pop();
            } else {
                int hashCode = name.hashCode();
                for (int idx = 0; idx < MAP_SETTERS.length; idx++) {
                    MapSetter mapSetter = MAP_SETTERS[mapSetterIdx];
                    mapSetterIdx = (mapSetterIdx + 1) % MAP_SETTERS.length;

                    if (mapSetter.hashCode == hashCode) {
                        if (mapSetter.mapSetIt != null)
                            mapSetter.mapSetIt.setIt(value);
                        return;
                    }
                }

                System.err.println("Failed to find match " + name);
            }
        // } catch (Exception ex) {
        //     System.err.println("failed " + ex);
        // }
    }

    public static SunVectorData parse(String jsonStr)   {

        if (workingItems == null) {
            workingItems = new ArrayList<>(64);
            polygons = new ArrayList<>(100);
            coords = new ArrayList<>(1000);
        } else {
            workingItems.clear();
            polygons.clear();
            coords.clear();
        }

        JsonStream baseStreamer = new JsonStreamString(jsonStr,
                (streamer, name, value, type) -> {
                    if (JsonStreamString.START_ARRAY == value && "features".equals(name))
                        streamer.push(SunVectorData::onFeature, "onFeature");
                }, SunVectorData::onError);

        return workingItems.isEmpty() ? null : new SunVectorData(workingItems);
    }

    public static SunVectorData parse(byte[] jsonBytes)   {

        if (workingItems == null) {
            workingItems = new ArrayList<>(64);
            polygons = new ArrayList<>(100);
            coords = new ArrayList<>(1000);
        } else {
            workingItems.clear();
            polygons.clear();
            coords.clear();
        }

        JsonStream baseStreamer = new JsonStreamBytes(jsonBytes,
                (streamer, name, value, type) -> {
                    if (JsonStreamString.START_ARRAY == value && "features".equals(name))
                        streamer.push(SunVectorData::onFeature, "onFeature");
                }, SunVectorData::onError);

        return workingItems.isEmpty() ? null : new SunVectorData(workingItems);
    }

    public static void release() {
        workingItems = null;    // 89
        polygons = null;        // 73
        coords = null;          // 19381
    }


    @Override
    public ArrayList<Item> getItems() {
        return items;
    }

    @Override
    public boolean equals(SunVectorDataI other) {
        return items.equals(other.getItems());
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
