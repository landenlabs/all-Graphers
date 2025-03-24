/*
    Dennis Lang  (landenlabs.com)
    Dec-2024
 */
package com.landenlabs.test.JsonDennis;

import java.util.HashMap;
import java.util.Map;

/**
 * Extract data from Json parsed into nested hash maps.
 */
public class JsonData {

    public final Map<String, Object> base = new HashMap<>();

    public int getInt(String find, int defValue) {
        return get(base, find, defValue);
    }
    public float getFloat(String find, float defValue) {
        return get(base, find, defValue);
    }
    public  <TT> TT get(String find, TT defValue) {
        return get(base, find, defValue);
    }

    public static <TT> TT get(Object data, String find, TT defValue) {
        try {
            Map<String, Object> base = (Map<String, Object>)data;
            String[] parts = find.split("[.]");
            Object obj = base;
            for (String part : parts) {
                Map<String, Object> mapLevel = (Map<String, Object>) obj;
                obj = "*".equals(part) ? mapLevel.values().iterator().next() : mapLevel.get(part);
            }
            return (obj != null) ? ((TT)obj) : defValue;
        } catch (Exception ignore) {
        }
        return defValue;
    }
}
