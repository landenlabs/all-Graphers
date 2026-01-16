package com.landenlabs.test;

import static com.landenlabs.mvt.MvtReader.loadMvt2;
import static com.landenlabs.mvt.MvtReader.loadMvt3;
import static com.landenlabs.test.utils.MemUtils.initMemory;
import static com.landenlabs.test.utils.MemUtils.showMemory;
import static com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader.loadMvt;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.landenlabs.all_graphers.ui.logger.ALog;
import com.landenlabs.test.Data.PolyItems;
import com.landenlabs.test.Data.SunVectorDataI;
import com.landenlabs.test.Data.WLatLng;
import com.landenlabs.test.JsonStream1.SunVectorBuilder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.ITagConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TagKeyValueMapConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class A_TestJson1 {
    Context appContext;
    @Before
    public void useAppContext() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.landenlabs.all_graphers", appContext.getPackageName());
    }

    private String loadAssetString(String filename) {
        AssetManager assetManager = appContext.getAssets();
        InputStream input;
        String text = "";

        try {
            input = assetManager.open(filename);
            int size = input.available();
            byte[] buffer = new byte[size];
            int readLen = input.read(buffer);
            input.close();
            text = new String(buffer);
        } catch (Exception ex) {
            Log.e(ALog.TAG_PREFIX, "Failed to read asset file " + filename, ex);
        }
        return text;
    }

    private byte[] loadAssetBytes(String filename) {
        AssetManager assetManager = appContext.getAssets();
        InputStream input;
        byte[] buffer = null;

        try {
            input = assetManager.open(filename);
            int size = input.available();
            buffer = new byte[size];
            int readLen = input.read(buffer);
            input.close();
        } catch (Exception ex) {
            Log.e(ALog.TAG_PREFIX, "Failed to read asset file " + filename, ex);
        }
        return buffer;
    }

    private String loadLocalFile(String filename) {
        String text = "";
        try {
            String jsonPath = new java.io.File("src/main/resources", filename).getCanonicalPath();
            System.out.println("jsonPath=" + jsonPath);
            text = new String(Files.readAllBytes(Paths.get(jsonPath)));
        } catch (Exception ex) {
            Log.e(ALog.TAG_PREFIX, "Failed to read resource file " + filename, ex);
        }
        return text;
    }

    public static class LatLng {
        double latDeg;
        double lngDeg;
    }

    // https://gis.stackexchange.com/questions/401541/decoding-mapbox-vector-tiles/460173#460173
    public static LatLng pixel2deg(int xTile, int yTile, int zoom, double xPixel, double yPixel, int numPixelPerTile) {
        double n = Math.pow(2.0, zoom);
        double xTileD = xTile + (xPixel / numPixelPerTile);
        double yTileD = yTile + ((numPixelPerTile - yPixel) / numPixelPerTile);

        LatLng latLng = new LatLng();
        latLng.lngDeg = (xTileD / n) * 360.0 - 180.0;
        double lat_rad = -Math.atan(Math.sinh(Math.PI * (1 - 2 * yTileD / n)));
        latLng.latDeg = Math.toDegrees(lat_rad);
        return latLng;
    }

    /*
        ./get-watchwarnings-json.pl
        curl -sS "https://api.weather.com/v2/vector-api/products/648/features?x=0&y=0&lod=0&tile-size=256&apiKey=6f0a14af513d4dad8a14af513dadad99&time=1764691922000&format=mvt" -o alert.mvt
        curl -sS "https://api.weather.com/v2/vector-api/products/648/features?x=0&y=0&lod=0&tile-size=256&apiKey=6f0a14af513d4dad8a14af513dadad99&time=1764691922000" -o alert.json
     */
    @Test
    public void test0() {
        // byte[] jsonBytes = loadAssetBytes("alert.json");
        // String jsonStr = new String(jsonBytes);
        initMemory();
        try {
            AssetManager assetManager = appContext.getAssets();
            InputStream input = assetManager.open("alert.mvt");

            final int NUMBER_OF_DIMENSIONS = 2;
            final int SRID = 0;
            final PrecisionModel precisionModel = new PrecisionModel();
            final PackedCoordinateSequenceFactory coordinateSequenceFactory =
                    new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, NUMBER_OF_DIMENSIONS);
            GeometryFactory geoFac = new GeometryFactory(precisionModel, SRID, coordinateSequenceFactory);
            ITagConverter tagConverter = new TagKeyValueMapConverter();

            final JtsMvt mvt = loadMvt3(input, geoFac, tagConverter,  com.landenlabs.mvt.MvtReader.RING_CLASSIFIER_V1);

            int total = 0;
            for (JtsLayer layer : mvt.getLayers()) {
                ArrayList<Geometry> geometry = (ArrayList<Geometry>)layer.getGeometries();
                LatLng ll;

                for (int gIdx = 0; gIdx < geometry.size(); gIdx++) {
                    final Polygon polygon = (Polygon) geometry.get(gIdx);
                    Object obj = polygon.getUserData();
                    if (obj instanceof Map) {
                        Map<String, Object> prop = (Map<String, Object>) obj;
                        String cat = prop.get("category").toString();
                        String sig = prop.get("significance").toString();
                        String cntry = prop.get("countryCode").toString();
                        String pheno  = prop.get("phenomena").toString();

                        // System.out.printf(" %3s %8s %3s %s [", cntry, cat, pheno, sig);
                        int numGeom = polygon.getNumGeometries();

                        for (int idx = 0; idx < numGeom; idx++) {
                            final Geometry geom = polygon.getGeometryN(idx);
                            int numCoord = geom.getNumPoints();
                            total += numCoord;
                            // System.out.printf(" %d\n", numCoord);
                            Coordinate[] coor = geom.getCoordinates();

                            for (int cIdx = 0; cIdx < numCoord; cIdx++) {
                                double xPixel = coor[cIdx].x;
                                double yPixel = coor[cIdx].y;
                                ll = pixel2deg(0, 0, 0, xPixel, yPixel, 512*8); // 4096
                                // System.out.printf("%,3f, %.3f\n", ll.latDeg, ll.lngDeg);
                            }
                            // System.out.println();
                        }
                        // System.out.println("]");
                    }
                }




            }

            showMemory("Alert MVT");
            System.out.println( "MVT size="+ total);
        } catch (Exception ex) {
            ALog.e.tagMsg(this, "MVT failed ", ex);
        }
    }

    @Test
    public void test1() {
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });

        System.out.println("Java JRE=" + System.getProperty("java.version"));
        long denDelta=0, stdDelta=0, stmDelta=0;
        int CNT = 10;

        System.out.println("Start");
        try {
            int total;
            byte[] jsonBytes = loadAssetBytes("alert.json");
            String jsonStr = new String(jsonBytes);
            initMemory();

            com.landenlabs.test.JsonDennis.JsonReader reader = new com.landenlabs.test.JsonDennis.JsonReader(jsonStr, true);
            showMemory("JsonReader");

            total = 0;
            com.landenlabs.test.JsonDennis.SunVectorData vdata1 = null;
            for (int idx = 0; idx < CNT; idx++) {
                vdata1 = com.landenlabs.test.JsonDennis.SunVectorData.parse(jsonStr);
                total += vdata1.items.size();
            }
            denDelta = showMemory(String.format("%d exec, Den VectorData size=%,d", CNT, total)).deltaMilli;

            total = 0;
            com.landenlabs.test.JsonOrg.SunVectorData vdata2 = null;
            for (int idx = 0; idx < CNT; idx++) {
                vdata2 = com.landenlabs.test.JsonOrg.SunVectorData.parse(jsonStr);
                total += vdata2.items.size();
            }
            stdDelta = showMemory(String.format("%d exec, Org VectorData size=%,d", CNT, total)).deltaMilli;

            total = 0;
            com.landenlabs.test.JsonStream1.SunVectorData vdata3 = null;
            SunVectorBuilder vectorBuilder = new SunVectorBuilder();
            if (false) {
                for (int idx = 0; idx < CNT; idx++) {
                    vdata3 = vectorBuilder.parse(jsonStr);
                    total += vdata3.items.size();
                }
            } else {
                for (int idx = 0; idx < CNT; idx++) {
                    vdata3 = vectorBuilder.parse(jsonBytes);
                    total += vdata3.items.size();
                }
            }
            vectorBuilder.release();
            vectorBuilder = null;
            stmDelta = showMemory(String.format("Stream VectorData size=%,d", total)).deltaMilli;


            if ( !vdata1.equals(vdata2) ) {
                System.err.println("vdata1 != vdata2");
                showDiff(vdata1, vdata2);
            } else
                System.out.println("vdata1 == vdata2");

            if ( ! vdata1.equals(vdata3) ) {
                System.err.println("vdata1 != vdata3");
                showDiff(vdata1, vdata3);
            } else
                System.out.println("vdata1 == vdata3");

        } catch (Exception ex) {
            String stackTrace = stackTraceToString(ex);
            System.out.println("\n[Exception]  " + ex.getMessage() + "\n" + stackTrace);
            // ex.fillInStackTrace();
            // ex.printStackTrace();
        }

        System.out.printf("Den faster than std by %.2f%%\n", (stdDelta - denDelta) * 100f / stdDelta);
        System.out.printf("Stm faster than std by %.2f%%\n", (stdDelta - stmDelta) * 100f / stdDelta);
        System.out.println("[Done]");
    }

    private static void showDiff(SunVectorDataI data1, SunVectorDataI data2) {
        ArrayList<PolyItems.Item> items1 = data1.getItems();
        ArrayList<PolyItems.Item> items2 = data2.getItems();
        for (int idx = 0; idx < Math.min(items1.size(), items2.size()); idx++) {
            PolyItems.Item item1 = items1.get(idx);
            PolyItems.Item item2 = items2.get(idx);
            if ( ! item1.equals(item2)) {
                System.out.printf("Diff at item %d\n", idx);
                if ( ! item1.properties.equals(item2.properties))
                    System.out.println("  Properties differ");
                if ( ! item1.geoItem.equals(item2.geoItem)) {
                    boolean b = item1.geoItem.equals(item2.geoItem);
                    System.out.println("  GeoItems " + (b?"equal":"differ"));
                    showDiff(item1.geoItem, item2.geoItem);
                }
                return;
            }
        }
        if (items1.size() != items2.size())
            System.out.printf("Sizes differ %d != %d\n", items1.size(), items2.size());
        else
            System.out.println("--Show diff done--");
    }

    private static void showDiff(PolyItems.GeoItem item1, PolyItems.GeoItem item2) {
        if (item1 instanceof PolyItems.GeoPolygons) {
            PolyItems.GeoPolygons poly1 = (PolyItems.GeoPolygons)item1;
            PolyItems.GeoPolygons poly2 = (PolyItems.GeoPolygons)item2;
            showDiff(poly1.polygons, poly2.polygons);
        } else if (item1 instanceof PolyItems.GeoMultiPolygons) {
            PolyItems.GeoMultiPolygons poly1 = (PolyItems.GeoMultiPolygons)item1;
            PolyItems.GeoMultiPolygons poly2 = (PolyItems.GeoMultiPolygons)item2;
            showDiff(poly1.polygons, poly2.polygons);
        } else
            System.out.println("Unsupported types " + item1.getClass().getSimpleName());
    }

    private static void showDiff(ArrayList<PolyItems.GeoPolygon> polygons1,  ArrayList<PolyItems.GeoPolygon> polygons2) {
        if ( ! polygons1.equals(polygons2)) {
            for (int idx = 0; idx < Math.min(polygons1.size(), polygons2.size()); idx++) {
                PolyItems.GeoPolygon poly1 = polygons1.get(idx);
                PolyItems.GeoPolygon poly2 = polygons2.get(idx);
                if (!poly1.equals(poly2)) {
                    System.out.println(" Polygons differ at " + idx);
                    for (int ptIdx = 0; ptIdx < Math.min(poly1.points.size(), poly2.points.size()); ptIdx++) {
                        WLatLng point1 = poly1.points.get(ptIdx);
                        WLatLng point2 = poly2.points.get(ptIdx);
                        if ( ! point1.equals(point2)) {
                            double delta1 = Math.abs(point1.latitude - point2.latitude);
                            double delta2 = Math.abs(point1.longitude - point2.longitude);
                            if (delta1 > 0.0001 || delta2 > 0.0001) {
                                System.out.println("Points differ at " + ptIdx);
                                break;
                            }
                        }
                    }
                    return;
                }
            }
            if (polygons1.size() != polygons2.size())
                System.out.printf("Sizes differ %d != %d\n", polygons1.size(), polygons2.size());
            else
                System.out.println("--Show diff done--");
        }
    }

    /*
    public static void compare(JsonDennis.SunVectorData vdata1, JsonOrg.SunVectorData vdata2) {
        int size1 = vdata1.items.size();
        int size2 = vdata2.items.size();

        if (size1 != size2) {
            System.out.println("Sizes differ ");
        }
        boolean same = vdata1.items.equals(vdata2.items);
        System.out.println(same ? "Same" : "Differ");
    }
     */

    public static String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
