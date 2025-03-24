/*
    Dennis Lang  (landenlabs.com)
    Dec-2024
 */
package com.landenlabs.test.JsonStream1;

import static com.landenlabs.test.JsonStream1.JDouble.JDOUBLE;

import java.util.*;

import static java.lang.Character.isWhitespace;

/**
 * Parse Json into nested hash maps.
 * Json syntax - https://www.json.org/json-en.html
 */
public class JsonStreamString implements JsonStream {
    private final String jsonStr;
    private final int len;
    private int idx;
    private int level;

    public static final String N_ROOT = "";
    public static final String N_ARRAY = "NA";
    public static final int T_MAP = -2;
    public static final int T_ARRAY = -1;
    public static final String START_ARRAY = "SA";
    public static final String END_ARRAY = "EA";
    public static final String START_MAP = "SM";
    public static final String END_MAP = "EM";
    public static final String IGNORE = "IG";

    public static final int MAX_LEVELS = 20;

    private StreamCb streamCb;
    private final ErrorCb errorCb;

    private final ArrayDeque<StreamCb> queueCb = new ArrayDeque<>(10);

    public void push(StreamCb streamCb, String dbgTag) {
        queueCb.addLast(this.streamCb);
        this.streamCb = streamCb;
    }
    public void pop() {
        this.streamCb = queueCb.removeLast();
    }


    public JsonStreamString(String jsonStr, StreamCb streamCb, ErrorCb errorCb) {
        this.jsonStr = jsonStr;
        this.streamCb = streamCb;
        this.errorCb = errorCb;
        this.len = jsonStr.length();

        level = idx = 0;

        try {
            next(0, N_ROOT);
        } catch (Exception ex) {
            System.err.println("JsonStream exception=" + ex);
            gotError();
        }
    }

    void gotError() {
        String errMsg;
        if (idx < len) {
            errMsg = "Error at position=" + idx
                    + ", json=" + jsonStr.substring(idx, idx + 10)
                    + " length=" + len;
        } else {
            errMsg = "Error at position=" + idx + " JsonTotalLen=" + len;
        }
        if (errorCb != null)
           errorCb.onError(idx, errMsg);
        else
            System.err.println(errMsg);
    }

    void dbgOnValue(JsonStreamString streamer, String name, Object value, int type) {
        // if (IGNORE != value)
            streamCb.onValue(streamer, name, value, type);
    }

    public int getLevel() {
        return level;
    }

    // Parse next field
    public void next(int endLevel, String name) {
        while (idx < len) {
            char c = skipWhite();
            idx++;

            switch (c) {
                case '{':
                    dbgOnValue(this, name, START_MAP, T_MAP);
                    level++;
                    break;
                case '}':
                    level--;
                    dbgOnValue(this, name, END_MAP, T_MAP);
                    break;
                case ']':
                    idx--;
                    return;
                case '"':
                    String name2 = getQuoted();
                    if (skipWhite() == ':')
                        idx++;
                    dbgOnValue(this, name2, getValue(level, name2), T_MAP);
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

    private Object getValue(int ourLevel, String name) {
        char c = skipWhite();
        int beg = idx;
        if (c == '"') {
            idx++;
            return getQuoted();
        } else if (c == '[') {
            idx++;
            skipWhite();
            dbgOnValue(this, name, START_ARRAY, T_ARRAY);
            level++;

            int arrIdx = 0;
            while (idx < len && jsonStr.charAt(idx) != ']') {
                dbgOnValue(this, N_ARRAY, getValue(ourLevel, N_ARRAY), arrIdx++);
                if (skipWhite() == ',')
                    idx++;
            }
            idx++;  // skip over ']'
            level--;
            return END_ARRAY;
        } else if (c == '{') {
            next(ourLevel, name);
            return IGNORE;
        } else {
            toFieldEnd();
            String value = jsonStr.substring(beg, idx);
            if (value.isEmpty() )
                return null;  // { } or [ ]
            if (value.indexOf('.') != -1)
                return Double.parseDouble(value);
                // return JDOUBLE.set(value); //  Double.parseDouble(value);
            if (value.equalsIgnoreCase("null"))
                return null;
            if (value.equalsIgnoreCase("true"))
                return Boolean.TRUE;
            if (value.equalsIgnoreCase("false"))
                return Boolean.FALSE;
            return Long.parseLong(value);
        }
    }

    private char skipWhite() {
        char c = '?';
        while (idx < len && isWhitespace(c = jsonStr.charAt(idx)))
            idx++;
        return c;
    }

    // Advance until one of:  comma, brace, bracket or whitespace.
    private void toFieldEnd() {
        char c = '?';
        while (idx < len) {
            c = jsonStr.charAt(idx);
            if (isWhitespace(c) || c == ',' || c == '}' || c == ']')
                break;
            idx++;
        }
    }
}
