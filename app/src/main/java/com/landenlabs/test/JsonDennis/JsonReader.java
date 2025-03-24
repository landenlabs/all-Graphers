/*
    Dennis Lang  (landenlabs.com)
    Dec-2024
 */
package com.landenlabs.test.JsonDennis;

import com.landenlabs.test.Data.ArrayCache;
import com.landenlabs.test.Data.ArrayCache16;
import com.landenlabs.test.Data.ArrayListPlus;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Character.isWhitespace;

/**
 * Parse Json into nested hash maps.
 * Json syntax - https://www.json.org/json-en.html
 */
public class JsonReader {
    private final String jsonStr;
    private final boolean throwError;
    private int idx;
    private int level;

    public final JsonData data = new JsonData();
    private final ArrayCache cache = new ArrayCache16();
    // private final ArrayCache cache = new ArrayCacheNOP();

    public JsonReader(String jsonStr, boolean throwError) {
        this.jsonStr = jsonStr;
        this.throwError = throwError;
        level = idx = 0;
        try {
            next(data.base, 0);
        } catch (Exception ex) {
            gotError();
        }
    }

    Object gotError() {
        String errMsg;
        if (idx < jsonStr.length()) {
            errMsg = "Error at position=" + idx
                    + ", json=" + jsonStr.substring(idx, idx + 10)
                    + " length=" + jsonStr.length();
        } else {
            errMsg = "Error at position=" + idx + " JsonTotalLen=" + jsonStr.length();
        }
        if (throwError)
            throw new IllegalStateException(errMsg);
        else
            System.err.println(errMsg);

        return null;
    }

    // Parse next field
    private void next(Map<String, Object> data, int endLevel) {
        while (idx < jsonStr.length()) {
            char c = jsonStr.charAt(idx++);
            if (isWhitespace(c))
                continue;

            switch (c) {
                case '{':
                    level++;
                    break;
                case '}':
                    level--;
                    break;
                case '"':
                    String name = getQuoted();
                    if (skipWhite() == ':')
                        idx++;
                    data.put(name, getValue(level));
                    break;
                case ',':
                    break;
                default:
                    gotError();
                    return; // Error
            }
            if (level == endLevel)
                return;
        }
    }

    private String getQuoted() {
        int beg = idx++;
        int end = beg;
        do { end++; end = jsonStr.indexOf('"', end); }
            while (jsonStr.charAt(end-1) == '\\');

        idx = end+1;
        return jsonStr.substring(beg, end);
    }

    private Object getValue(int ourLevel) {
        char c = skipWhite();
        int beg = idx;
        if (c == '"') {
            idx++;
            return getQuoted();
        } else if (c == '[') {
            idx++;
            skipWhite();
            ArrayListPlus<Object> array = new ArrayListPlus<>(2, cache);
            // ArrayList<Object> array = new ArrayList<>(2);
            while (idx < jsonStr.length() && jsonStr.charAt(idx) != ']') {
                // String debStr = jsonStr.substring((idx));
                array.add(getValue(ourLevel));
                if (skipWhite() == ',')
                    idx++;
            }
            // array.trimToSize();
            // System.out.println("Array size=" + array.size() + " capacity=" + array.getCapacity());
            idx++;  // skip over ']'
            return array;
        } else if (c == '{') {
            Map<String, Object> map = new HashMap<>();
            next(map, ourLevel);
            return map;
        } else {
            toFieldEnd();
            String value = jsonStr.substring(beg, idx);
            if (value.isEmpty() || value.equals("null"))
                return null;  // { } or [ ]
            if (value.equalsIgnoreCase("null"))
                return null;
            if (value.equalsIgnoreCase("true"))
                return Boolean.TRUE;
            if (value.equalsIgnoreCase("false"))
                return Boolean.FALSE;
            if (value.indexOf('.') == -1)
                return Long.parseLong(value);
            return Double.parseDouble(value);
        }
    }

    private char skipWhite() {
        char c = '?';
        while (idx < jsonStr.length() && isWhitespace(c = jsonStr.charAt(idx)))
            idx++;
        return c;
    }

    // Advance until one of:  comma, brace, bracket or whitespace.
    private void toFieldEnd() {
        char c = '?';
        while (idx < jsonStr.length()) {
            c = jsonStr.charAt(idx);
            if (isWhitespace(c) || c == ',' || c == '}' || c == ']')
                break;
            idx++;
        }
    }
}
