package com.landenlabs.test.JsonGson1;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class GeoJsonFeature {
    public String id;
    public String type;
    public Geometry geometry;
    public Properties properties;

    public static class Geometry {
        public String type;
        // Polygon coordinates are List<List<List<Double>>>
        // MultiPolygon coordinates are List<List<List<List<Double>>>>
        // See custom Gson deserializer
        public Object coordinates;
    }

    public static class Properties {
        @SerializedName("upper_level")
        public String upperLevel;

        public String phenomenon;

        @SerializedName("raw_type")
        public String rawType;

        @SerializedName("continuation_time")
        public String continuationTime;

        @SerializedName("active_at")
        public String activeAt;

        @SerializedName("icao_site_id")
        public String icaoSiteId;

        @SerializedName("issue_time")
        public String issueTime;

        public int clusterCount;

        @SerializedName("issuing_station")
        public String issuingStation;

        @SerializedName("airmet_category")
        public String airmetCategory;

        @SerializedName("moving_speed")
        public Integer movingSpeed;

        public String type;

        @SerializedName("lower_level")
        public String lowerLevel;

        @SerializedName("fir_ids")
        public List<String> firIds;

        public long validTime;

        @SerializedName("moving_dir")
        public String movingDir;

        @SerializedName("airmet_id")
        public String airmetId;

        public String data;

        @SerializedName("expire_at")
        public String expireAt;
    }
}