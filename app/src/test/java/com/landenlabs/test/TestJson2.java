package com.landenlabs.test;

import static com.landenlabs.test.utils.MemUtils.initMemory;
import static com.landenlabs.test.utils.MemUtils.showMemory;

import android.util.Log;

import com.landenlabs.all_graphers.ui.logger.ALog;
import com.landenlabs.test.Data.PolyItems;
import com.landenlabs.test.Data.SunVectorDataI;
import com.landenlabs.test.Data.WLatLng;
import com.landenlabs.test.JsonDennis.JsonReader;
import com.landenlabs.test.JsonStream1.SunVectorBuilder;

import org.junit.Test;
import org.robolectric.versioning.AndroidVersions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class TestJson2 {

    private byte[] loadLocalBytes(String filename) {
        try {
            String jsonPath = new java.io.File("src/main/assets", filename).getCanonicalPath();
            System.out.println("jsonPath=" + jsonPath);
            return Files.readAllBytes(Paths.get(jsonPath));
        } catch (Exception ex) {
            Log.e(ALog.TAG_PREFIX, "Failed to read resource file " + filename, ex);
        }
        return null;
    }

    int find(byte[] buffer, byte[] want, int mid) {
        int len = buffer.length - want.length;
        while (mid < len ) {
            boolean found = true;
            for (int fIdx = 0; fIdx < want.length; fIdx++) {
                if (buffer[mid++] != want[fIdx]) {
                    found = false;
                    break;
                }
            }
            if (found)
                return mid - want.length;
        }
        return -1;
    }

    int find(byte[] buffer, byte want, int offset) {
        int len = buffer.length;
        while (offset < len) {
            if (buffer[offset] == want)
                return offset;
            offset++;
        }
        return -1;
    }

    int rfind(byte[] buffer, byte want, int offset) {
        while (offset >= 0) {
            if (buffer[offset] == want)
                return offset;
            offset--;
        }
        return -1;
    }


    @Test
    public void test2() {
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });

        int CNT = 1;

        System.out.println("Start");
        try {
            int total;
            byte[] jsonBytes = loadLocalBytes("test2.json");
            // String jsonStr = new String(jsonBytes);

            // ------
            //  Try and split json to use 2 threads to load and then merge results.
            // ------
            int splitOff;
            splitOff = find(jsonBytes, "\"geometry\"".getBytes(), jsonBytes.length/2 -20);

            if (splitOff != -1) {
                int startFirstAt = find(jsonBytes, "\"features\"".getBytes(), 0);
                startFirstAt = find(jsonBytes, (byte)'{', startFirstAt);

                int start2ndAt = rfind(jsonBytes, (byte)'{', splitOff);
                int endFirstAt = rfind(jsonBytes, (byte)',', start2ndAt);
                int end2ndAt = rfind(jsonBytes, (byte)'}', jsonBytes.length - 2);

                com.landenlabs.test.JsonStream1.SunVectorData vdata3a;
                com.landenlabs.test.JsonStream1.SunVectorData vdata3b;
                SunVectorBuilder vectorBuilderA = new SunVectorBuilder();
                SunVectorBuilder vectorBuilderB = new SunVectorBuilder();
                vdata3a = vectorBuilderA.parse(jsonBytes, startFirstAt, endFirstAt);
                vdata3b = vectorBuilderB.parse(jsonBytes, start2ndAt, end2ndAt);
                System.out.printf(" part A size=%d,  part B size=%d, total=%d\n",
                        vdata3a.items.size(), vdata3b.items.size(), vdata3a.items.size()+ vdata3b.items.size());

                System.out.printf(" len=%,d split=%,d\n", jsonBytes.length, splitOff);
            }


            initMemory();

            total = 0;
            com.landenlabs.test.JsonStream1.SunVectorData vdata3 = null;
            SunVectorBuilder vectorBuilder = new SunVectorBuilder();
            for (int idx = 0; idx < CNT; idx++) {
                vdata3 = vectorBuilder.parse(jsonBytes);
                total += vdata3.items.size();
            }
            vectorBuilder.release();
            vectorBuilder = null;
            showMemory(String.format("Stream VectorData total=%,d", total));

        } catch (Exception ex) {
            System.out.println("\n[Exception]  " + ex.getMessage() + "\n" + ex);
        }

        System.out.println("[Done]");
    }

    /*
    public static void main(String[] args) throws IOException {
        // org.openjdk.jmh.Main.main(args);
    }
    */
}
