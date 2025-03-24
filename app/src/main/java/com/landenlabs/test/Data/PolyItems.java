package com.landenlabs.test.Data;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PolyItems {

    public static class Properties {
        public DateTime activeTime;      //   "active_at": "2025-03-14T14:30:00+00:00",
        public DateTime issueTime;       //   "issue_time": "2025-03-14T13:58:00+00:00",
        public DateTime expireTime;      //   "expire_at": "2025-03-14T17:30:00+00:00"
        public DateTime continuationTime;  //   "continuation_time": null,
        public Long validTMilli;         //   "validTime": 1741970280000,

        public String id;                //   "airmet_id": "21",
        public String category;          //   "airmet_category": null,
        public String phenomenon;        //   "phenomenon": "TURB",
        public String type;              //   "type": "MTW",
        public String rawType;           //   "raw_type": "MOD MTW",
        public String icao;              //   "icao_site_id": "LIIB",
        public String issuingStation;    //   "issuing_station": "LIIB",
        public String lowerLevel;        //   "lower_level": "FL020",
        public String upperLevel;        //   "upper_level": "FL100",
        public Double movingSpeed;       //   "moving_speed": null,
        public Double movingDir;         //   "moving_dir": 90,
        public Integer clusterCount;     //   "clusterCount": 1,

        public String data;  //  "data": "LIRR AIRMET 21 VALID 141430/141730 LIIB-\nLIRR ROMA FIR MOD MTW FCST WI N4334 E01036 - N4340 E01119 - N4328\nE01315 - N4252 E01303 - N4124 E01426 - N4107 E01513 - N4017 E01539 -\nN4119 E01359 - N4220 E01232 - N4334 E01036 FL020/100 MOV E NC",
        //   "fir_ids": [  "LIRR"  ],


        @Override
        public int hashCode() {
            return Objects.hash(
            activeTime,
            issueTime,
            expireTime,
            continuationTime,
            validTMilli,
            id,
            category,
            phenomenon,
            type,
            rawType,
            icao,
            issuingStation,
            lowerLevel,
            upperLevel,
            movingSpeed,
            movingDir,
            // clusterCount,
            data
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Properties))
                return false;
            Properties properties = (Properties)obj;
            boolean same = hashCode() == properties.hashCode();

            boolean hashSame = hashCode() == properties.hashCode();
            if (same != hashSame)
                System.err.println("equals and hashcode differ !!");
            return same;
        }
    }

    public enum GeoType { None, Polygon, MultiPolygon, Point, Polyline };
    public interface GeoItem {
        GeoType getType();
    }

    public static class GeoPolygon {
        public WLatLngBounds bounds;
        public ArrayList<WLatLng> points;

        public GeoPolygon(ArrayList<WLatLng> points) {
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
            if (!(obj instanceof GeoPolygon))
                return false;
            GeoPolygon other = (GeoPolygon) obj;
            boolean same = this.bounds.equals(other.bounds) && points.equals(other.points);

            boolean sameHash = hashCode() == other.hashCode();
            if (same != sameHash)
                System.err.println("equals and hashCode differ");

            return same;
        }
    }

    public static class GeoPolygons implements GeoItem {

        public ArrayList<GeoPolygon> polygons;

        public GeoPolygons(ArrayList<GeoPolygon> polygons) {
            this.polygons = polygons;
        }

        // Used my GeoMultiPolygons
        protected GeoPolygons() {
        }

        public GeoPolygons(HashMap<String, Object> geoValues) {
            List<Object> coords = com.landenlabs.test.JsonDennis.JsonData.get(geoValues, "coordinates", null);
            assert(coords != null);
            polygons = new ArrayList<>(coords.size());
            for (Object item : coords) {
                if (item instanceof List) {
                    List<Object> inPoints = (List<Object>) item;
                    ArrayList<WLatLng> outPoints = new ArrayList<>(inPoints.size());
                    for (Object point : inPoints) {
                        List<Double> lngLat = (List<Double>) point;
                        outPoints.add(new WLatLng(lngLat.get(1), lngLat.get(0)));
                    }
                    polygons.add(new GeoPolygon(outPoints));
                }
            }

            //ALog.d.tagMsg("Polygon", "size=", polygons.size());
        }

        public GeoPolygons(JSONArray coords) throws JSONException {
            assert(coords != null);
            polygons = new ArrayList<>(coords.length());
            int len = coords.length();
            for (int idx = 0; idx < len; idx++) {
                Object item = coords.get(idx);
                if (item instanceof JSONArray) {
                    JSONArray inPoints = (JSONArray) item;
                    ArrayList<WLatLng> outPoints = new ArrayList<>(inPoints.length());
                    int inPntCnt = inPoints.length();
                    for (int ptIdx = 0; ptIdx < inPntCnt; ptIdx++) {
                        JSONArray jcoord = inPoints.getJSONArray(ptIdx);
                        double lng = jcoord.getDouble(0);
                        double lat = jcoord.getDouble(1);
                        outPoints.add(new WLatLng(lat, lng));
                    }
                    polygons.add(new GeoPolygon(outPoints));
                }
            }

            //ALog.d.tagMsg("Polygon", "size=", polygons.size());
        }
        @Override
        public GeoType getType() { return GeoType.Polygon; }

        @Override
        public int hashCode() {
            return Objects.hash(polygons);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof GeoPolygons))
                return false;
            GeoPolygons other = (GeoPolygons) obj;
            boolean same = Objects.equals(this.polygons, other.polygons);
            boolean sameHash = hashCode() == other.hashCode();
            if (same != sameHash)
                System.err.println("equals and hashCode differ");
            return same;
        }
    }

    public static class GeoMultiPolygons extends GeoPolygons {

        // public ArrayList<GeoPolygon> polygons;

        public GeoMultiPolygons(ArrayList<GeoPolygon> polygons) {
           super(polygons);
        }

        public GeoMultiPolygons(HashMap<String, Object> geoValues) {
            List<Object> coords = com.landenlabs.test.JsonDennis.JsonData.get(geoValues, "coordinates", null);
            assert(coords != null);
            polygons = new ArrayList<>(coords.size());
            for (Object item : coords) {
                if (item instanceof List) {
                    List<Object> inLists = (List<Object>) item;
                    for (Object itemList: inLists) {
                        if (itemList instanceof List) {
                            List<Object> inPoints = (List<Object>) itemList;
                            ArrayList<WLatLng> outPoints = new ArrayList<>(inPoints.size());
                            for (Object point : inPoints) {
                                List<Double> lngLat = (List<Double>) point;
                                outPoints.add(new WLatLng(lngLat.get(1), lngLat.get(0)));
                            }
                            polygons.add(new GeoPolygon(outPoints));
                        }
                    }
                }
            }

            //ALog.d.tagMsg("Polygon", "size=", polygons.size());
        }

        public GeoMultiPolygons(JSONArray coords) throws JSONException {
            assert(coords != null);
            polygons = new ArrayList<>(coords.length());
            int len = coords.length();
            for (int idx = 0; idx < len; idx++) {
                Object item = coords.get(idx);
                if (item instanceof JSONArray) {
                    JSONArray innerList = (JSONArray) item;
                    int len2 = innerList.length();
                    for (int idx2 = 0; idx2 < len2; idx2++) {
                        Object innerObj= innerList.get(idx2);
                        JSONArray inPoints = (JSONArray) innerObj;
                        ArrayList<WLatLng> outPoints = new ArrayList<>(inPoints.length());
                        int inPntCnt = inPoints.length();
                        for (int ptIdx = 0; ptIdx < inPntCnt; ptIdx++) {
                            JSONArray jcoord = inPoints.getJSONArray(ptIdx);
                            double lng = jcoord.getDouble(0);
                            double lat = jcoord.getDouble(1);
                            outPoints.add(new WLatLng(lat, lng));
                        }
                        polygons.add(new GeoPolygon(outPoints));
                    }
                }
            }

            //ALog.d.tagMsg("Polygon", "size=", polygons.size());
        }
        @Override
        public GeoType getType() { return GeoType.Polygon; }

        @Override
        public int hashCode() {
            return Objects.hash(polygons);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof GeoPolygons))
                return false;
            GeoPolygons other = (GeoPolygons) obj;
            boolean same = Objects.equals(this.polygons, other.polygons);
            boolean sameHash = hashCode() == other.hashCode();
            if (same != sameHash)
                System.err.println("equals and hashCode differ");
            return same;
        }
    }

    public static class Item {
        public Properties properties;
        public GeoItem geoItem;

        public Item(Properties prop, GeoItem geoItem) {
            this.properties = prop;
            this.geoItem = geoItem;
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, geoItem);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Item))
                return false;
            Item other = (Item)obj;
            boolean same = this.properties.equals(other.properties)
                    && this.geoItem.equals(other.geoItem);

            boolean hashSame = hashCode() == other.hashCode();
            if (same != hashSame)
                System.err.println("equals and hashcode differ !!");
            return same;
        }
    }
}
