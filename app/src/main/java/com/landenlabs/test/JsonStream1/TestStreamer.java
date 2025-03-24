package com.landenlabs.test.JsonStream1;

import com.landenlabs.test.utils.MemUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;

import static com.landenlabs.test.utils.MemUtils.initMemory;
import static com.landenlabs.test.utils.MemUtils.showMemory;

public class TestStreamer {


    //region [testQueue]
    private static void testQueue() {
        ArrayDeque<String> queue = new ArrayDeque<>(10);
        queue.addLast("first");
        queue.addLast("second");
        queue.addLast("third");
        System.out.println("pop 3rd=" + queue.removeLast());
        System.out.println("pop 2nd=" + queue.removeLast());
        System.out.println("pop 1st=" + queue.removeLast());
    }
    //endregion

    //region [dumpStreamer]
    private static void dumpStreamer() {
        try {
            System.out.println("[Start] TestStreamer\n");

            initMemory();
            String jsonPath = new java.io.File("src/main/resources", "test2.json").getCanonicalPath();
            System.out.println("jsonPath=" + jsonPath);
            String json = new String(Files.readAllBytes(Paths.get(jsonPath)));
            System.out.printf("  json length=%,d \n", json.length());

            JsonStreamString streamer = new JsonStreamString(json, TestStreamer::onData, TestStreamer::onError);
            System.out.println();
        } catch (Exception ex) {
            String stackTrace = stackTraceToString(ex);
            System.out.println("\n[Exception]  " + ex.getMessage() + "\n" + stackTrace);
            // ex.printStackTrace();
        }
    }

    private static void onError(int pos, String msg) {
        System.err.printf("Failed at %d, msg=%s\n", pos, msg);
    }

    private static String quoteIfString(Object value) {
        return (value instanceof String)
                ? String.format("\"%s\"", value)
                : (value == null) ? "null" : value.toString();
    }
    private static int PREV_LEVEL = -1;
    private static void onData(JsonStream streamer, String name, Object value, int typeIdx) {
        if (streamer.getLevel() == PREV_LEVEL)
            System.out.println(",");
        else
            System.out.println();
        PREV_LEVEL = streamer.getLevel();

        System.out.print("  ".repeat(streamer.getLevel()));
        if (JsonStreamString.START_ARRAY == value) {
            if (name == null || name.length() == 0)
                System.out.print("[");
            else
                System.out.printf("\"%s\": [", name);
        } else if (JsonStreamString.END_ARRAY == value) {
            System.out.print("]");
        } else if (JsonStreamString.START_MAP == value) {
            if (name == null || name.length() == 0)
                System.out.print("{");
            else
                System.out.printf("\"%s\": {", name);
        } else if (JsonStreamString.END_MAP == value) {
            System.out.print("}");
        } else {
            if (typeIdx == JsonStreamString.T_MAP)
                System.out.printf("\"%s\": ",  name);
            System.out.print(quoteIfString(value));
        }
    }
    //endregion


    private static void testStreamer() {
        try {
            System.out.println("[Start] TestStreamer");

            String jsonPath = new java.io.File("src/main/resources", "test2.json").getCanonicalPath();
            System.out.println("jsonPath=" + jsonPath);
            String json = new String(Files.readAllBytes(Paths.get(jsonPath)));
            System.out.printf("  json length=%,d \n\n", json.length());

            initMemory();
            SunVectorData vectorData = SunVectorBuilder.parse(json);
            showMemory("testStreamer items=" + (vectorData.items.size()));

        } catch (Exception ex) {
            // ex.fillInStackTrace();
            String stackTrace = stackTraceToString(ex);
            System.out.println("\n[Exception]  " + ex.getMessage() + "\n" + stackTrace);
            // ex.printStackTrace();
        }
    }


    public static String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     */
    public static void main(String[] args) {
        MemUtils.runtime = Runtime.getRuntime();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });

        System.out.println("Java JRE=" + System.getProperty("java.version"));
        // dumpStreamer();
        testStreamer();
        System.out.println("\n[Done] TestStreamer");
    }

}
