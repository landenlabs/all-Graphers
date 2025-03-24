package com.landenlabs.test;

import static com.landenlabs.test.utils.MemUtils.initMemory;
import static com.landenlabs.test.utils.MemUtils.showMemory;

import com.landenlabs.all_graphers.ui.logger.ALog;
import com.landenlabs.test.Data.PolyItems;
import com.landenlabs.test.Data.SunVectorDataI;
import com.landenlabs.test.Data.WLatLng;
import com.landenlabs.test.JsonStream1.SunVectorBuilder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
// @RunWith(AndroidJUnit4.class)
public class App_TestJson1 {
    Context appContext;

    public App_TestJson1(Context context) {
        this.appContext = context;
    }

    /* @Before
    public void useAppContext() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.landenlabs.all_graphers", appContext.getPackageName());
    }
     */

    private String loadAssetFile(String filename) {
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

    // @Test
    public void test1() {
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });

        System.out.println("Java JRE=" + System.getProperty("java.version"));
        long denDelta=0, stdDelta=0, stmDelta=0;
        int CNT = 100;

        System.out.println("Start");
        try {
            int total;
            String jsonStr = loadAssetFile("test3.json");
            initMemory();

            com.landenlabs.test.JsonDennis.JsonReader reader = new com.landenlabs.test.JsonDennis.JsonReader(jsonStr, true);
            showMemory("JsonReader");

            total = 0;
            com.landenlabs.test.JsonDennis.SunVectorData vdata1 = null;
            for (int idx = 0; idx < CNT; idx++) {
                vdata1 = com.landenlabs.test.JsonDennis.SunVectorData.parse(jsonStr);
                total += vdata1.items.size();
            }
            denDelta = showMemory(String.format("Den VectorData size=%,d", total)).deltaMilli;

            total = 0;
            com.landenlabs.test.JsonOrg.SunVectorData vdata2 = null;
            for (int idx = 0; idx < CNT; idx++) {
                vdata2 = com.landenlabs.test.JsonOrg.SunVectorData.parse(jsonStr);
                total += vdata2.items.size();
            }
            stdDelta = showMemory(String.format("Org VectorData size=%,d", total)).deltaMilli;

            total = 0;
            com.landenlabs.test.JsonStream1.SunVectorData vdata3 = null;
            for (int idx = 0; idx < CNT; idx++) {
                vdata3 = SunVectorBuilder.parse(jsonStr);
                total += vdata3.items.size();
            }
            SunVectorBuilder.release();
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
                System.err.println("vdata1 == vdata3");

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
