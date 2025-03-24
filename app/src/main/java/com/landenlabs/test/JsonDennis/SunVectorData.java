package com.landenlabs.test.JsonDennis;

import com.landenlabs.test.Data.PolyItems;
import com.landenlabs.test.Data.PolyItems.GeoPolygons;
import com.landenlabs.test.Data.PolyItems.GeoType;
import com.landenlabs.test.Data.PolyItems.Item;
import com.landenlabs.test.Data.PolyItems.Properties;
import com.landenlabs.test.Data.SunVectorDataI;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SunVectorData  implements SunVectorDataI  {
    public ArrayList<Item> items;

    // ---------------------------------------------------------------------------------------------


    public SunVectorData(ArrayList<Item> items) {
        this.items = items;
    }

    public static SunVectorData parse(String json) {
        Number num;
        // ALog.d.tagMsg(ALog.TAG_PREFIX, "Polygon start parse json len=", json.length());
        ArrayList<Item> items = new ArrayList<>();

        JsonReader reader = new JsonReader(json, false);
        // ALog.d.tagMsg(ALog.TAG_PREFIX, "Polygon using parsed json");
        JsonData jsonData = reader.data;
        List<Object> features = jsonData.get("features", null);

        for (Object jObj : features) {

            HashMap<String, Object> properties = get(jObj, "properties", null);
            HashMap<String, Object> geoValues = get(jObj, "geometry", null);

            if (properties != null && geoValues != null) {
                PolyItems.Properties prop = new Properties();

                try {
                    prop.activeTime = getDateTime(properties, "active_at", null);
                    prop.issueTime = getDateTime(properties, "issue_time", null);
                    prop.expireTime = getDateTime(properties, "expire_at", null);
                    prop.continuationTime = getDateTime(properties, "continuation_time", null);
                    prop.validTMilli = get(properties, "validTime", 0L);

                    prop.id = get(properties, "airmet_id", null);
                    prop.category = get(properties, "airmet_category", null);
                    prop.phenomenon = get(properties, "phenomenon", null);
                    prop.type = get(properties, "type", null);
                    prop.rawType = get(properties, "raw_type", null);
                    prop.icao = get(properties, "icao_site_id", null);
                    prop.issuingStation = get(properties, "issuing_station", null);
                    prop.lowerLevel = get(properties, "lower_level", null);
                    prop.upperLevel = get(properties, "upper_level", null);
                    num = get(properties, "moving_speed", null);
                    prop.movingSpeed = (num == null) ? null : num.doubleValue();
                    num = get(properties, "moving_dir", null);
                    prop.movingDir =  (num == null) ? null : num.doubleValue();
                    prop.data = get(properties, "data", "").replace("\\n", "\n");

                    PolyItems.GeoItem geoItem = null;
                    String geoTypeStr = get(geoValues, "type", "none");
                    GeoType geoType = GeoType.valueOf(geoTypeStr);
                    switch (geoType) {
                        case MultiPolygon:
                            // TODO - support multi-polygons
                            geoItem = new PolyItems.GeoMultiPolygons(geoValues);
                            break;
                        case Polygon:
                            geoItem = new GeoPolygons(geoValues);
                            break;
                        default:
                            // ALog.e.tagMsg(ALog.TAG_PREFIX, "Unknown vector type ", geoTypeStr);
                            break;
                    }

                    if (geoItem != null) {
                        items.add(new Item(prop, geoItem));
                    }
                } catch (Exception ex) {
                    // ALog.e.tagMsg(ALog.TAG_PREFIX, "Failed parsing vector data ", ex);
                    System.out.println(ex.getStackTrace());
                }
            }
        }

        // ALog.d.tagMsg(ALog.TAG_PREFIX, "Polygon done parse items cnt=", items.size());
        return items.isEmpty() ? null : new SunVectorData(items);
    }

    public static DateTime getDateTime(Object obj, String key, DateTime defValue) {
        String timeStr = get(obj, key, null);
        return  (timeStr != null) ? new DateTime(timeStr) : null;
    }

    public static <TT> TT get(Object data, String find, TT defValue) {
        return com.landenlabs.test.JsonDennis.JsonData.get(data, find, defValue);
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
