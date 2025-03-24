/*
    Dennis Lang  (landenlabs.com)
    Dec-2024
 */
package com.landenlabs.test.JsonDennis;

import java.util.ArrayList;
import java.util.Map;

/**
 * Dump to console Json nested map data.
 */
public class JsonDump {

    public static void dump(JsonData data) {
        dumpMap(data.base, 0);
    }
    
    private static void out(String msg) {
        System.out.print(msg);
    }
    private static void outLn() {
        System.out.println();
    }
    private static void outLn(Object msg) {
        System.out.print(msg);
    }

    public static void dumpMap(Map<String, Object> data, int level) {
        dumpName("{", level);
        outLn();
        String sep = "";
        for (Map.Entry<String,Object> item : data.entrySet()) {
            out(sep);
            dumpName(quoteIt(item.getKey()), level+1);
            out(" : ");
            dump(item.getValue(), level );
            sep = ",\n";
        }
        outLn();
        dumpName("}", level);
    }

    private static String quoteIt(String value) {
        return "\"" + value.replaceAll("\"", "\\\"") + "\"";
    }
    private static void dumpName(String name, int level) {
        for (int idx =0; idx < level; idx++)
            out("  ");
        out( name);
    }
    public static void dump(Object value, int level) {
        if (value == null)
            outLn("null");
        else if (value instanceof  String)
            outLn(quoteIt((String)value));
        else if (value instanceof  Number)
            outLn(value);
        else if (value instanceof Boolean) {
            outLn(value);
        } else if (value.getClass().getSimpleName().startsWith("ArrayList") ) {
            outLn();
            dumpName("[", level);
            String sep = "";
            ArrayList<Object> arrValue = (ArrayList<Object>)value;
            for (Object item : arrValue) {
                out(sep);
                dumpName("", level+1);
                dump(item, level+1);
                sep = ",";
            }
            dumpName("]", level);
        } else if (value instanceof Map<?,?>) {
            outLn();
            dumpMap((Map<String,Object>)value, level+1);
        } else
            System.err.println("Error");
    }
}
