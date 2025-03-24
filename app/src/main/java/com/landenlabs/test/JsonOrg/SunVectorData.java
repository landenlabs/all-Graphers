package com.landenlabs.test.JsonOrg;

import androidx.annotation.Nullable;

import com.landenlabs.test.Data.PolyItems.*;
import com.landenlabs.test.Data.SunVectorDataI;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SunVectorData implements SunVectorDataI {
    public ArrayList<Item> items;

    // ---------------------------------------------------------------------------------------------

    public SunVectorData(ArrayList<Item> items) {
        this.items = items;
    }

    public static SunVectorData parse(String jsonStr) throws JSONException {
        JSONObject jobject = new JSONObject(jsonStr);

        ArrayList<Item> items = new ArrayList<>();
        JSONArray features = jobject.getJSONArray("features");

        int featureCnt = features.length();
        for (int idx = 0; idx < featureCnt; idx++) {
            JSONObject item = features.getJSONObject(idx);
            JSONObject properties = item.getJSONObject("properties");
            JSONObject geometry = item.getJSONObject("geometry");
            String geoTypeStr = geometry.getString("type");
            JSONArray geoValues = geometry.getJSONArray("coordinates");

            if (properties != null && geoValues != null) {
                Properties prop = new Properties();

                try {
                    prop.activeTime = getDateTime(properties, "active_at", null);
                    prop.issueTime = getDateTime(properties, "issue_time", null);
                    prop.expireTime = getDateTime(properties, "expire_at", null);
                    prop.continuationTime = getDateTime(properties, "continuation_time", null);
                    prop.validTMilli = getLong(properties, "validTime", 0L);

                    prop.id = getStr(properties, "airmet_id", null);
                    prop.category = getStr(properties, "airmet_category", null);
                    prop.phenomenon = getStr(properties, "phenomenon", null);
                    prop.type = getStr(properties, "type", null);
                    prop.rawType = getStr(properties, "raw_type", null);
                    prop.icao = getStr(properties, "icao_site_id", null);
                    prop.issuingStation = getStr(properties, "issuing_station", null);
                    prop.lowerLevel = getStr(properties, "lower_level", null);
                    prop.upperLevel = getStr(properties, "upper_level", null);
                    prop.movingSpeed = getDouble(properties, "moving_speed", null);
                    prop.movingDir = getDouble(properties, "moving_dir", null);
                    prop.data = getStr(properties, "data", null);

                    GeoItem geoItem = null;
                    // String geoTypeStr = get(geoValues, "type", "none");
                    GeoType geoType = GeoType.valueOf(geoTypeStr);
                    switch (geoType) {
                        case MultiPolygon:
                            geoItem = new GeoMultiPolygons(geoValues);
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
                    System.out.println("parse sunvector error org " + ex.toString());
                    // ALog.e.tagMsg(ALog.TAG_PREFIX, "Failed parsing vector data ", ex);
                }
            }
        }

        // ALog.d.tagMsg(ALog.TAG_PREFIX, "Polygon done parse items cnt=", items.size());
        return items.isEmpty() ? null : new SunVectorData(items);
    }

    @Nullable
    public static DateTime getDateTime(JSONObject obj, String key, DateTime defValue) throws JSONException {
        String timeStr = getStr(obj, key, null);
        return  (timeStr != null) ? new DateTime(timeStr) : null;
    }

    @Nullable
    public static String getStr(JSONObject data, String find, String defValue) throws JSONException {
        String val = data.optString(find, null);
        return (val == null || val.equals("null")) ? defValue : val;
    }

    @Nullable
    public static Double getDouble(JSONObject data, String find, Double defValue)  {
        try {
            double dvalue= data.optDouble(find, Double.NaN);
            return Double.isNaN(dvalue) ? defValue : dvalue;
        } catch (Exception ex) {
            return defValue;
        }
    }
    @Nullable
    public static Long getLong(JSONObject data, String find, Long defValue)  {
        try {
            return data.optLong(find, defValue);
        } catch (Exception ex) {
            return defValue;
        }
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
