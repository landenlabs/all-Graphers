/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

/**
 * Helper class to build json strings.
 *
 *    jGroup(jField(key1, value1), jField(key2, value2)
 */
public class JsonBldr {

    public static String jGroup(String ... fields) {
        return "{" + String.join(",", fields) + "}";
    }
    public static String jField(String key, Object value) {
        if (value instanceof Number num) {
            double dNum = num.doubleValue();
            String str =  (dNum == Math.rint(dNum))
                ? String.valueOf(num.longValue())
                : String.valueOf(dNum);
            return String.format("\"%s\":\"%s\"", key, str);
        } else
            return String.format("\"%s\":\"%s\"", key, value);
    }
}
