/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;


import static com.landenlabs.test.JsonStream2.JsonTokens.END_ARRAY;
import static com.landenlabs.test.JsonStream2.JsonTokens.END_MAP;
import static com.landenlabs.test.JsonStream2.JsonTokens.IGNORE;
import static com.landenlabs.test.JsonStream2.JsonTokens.N_ARRAY;
import static com.landenlabs.test.JsonStream2.JsonTokens.N_ROOT;
import static com.landenlabs.test.JsonStream2.JsonTokens.START_ARRAY;
import static com.landenlabs.test.JsonStream2.JsonTokens.START_MAP;
import static com.landenlabs.test.JsonStream2.JsonTokens.T_ARRAY;
import static com.landenlabs.test.JsonStream2.JsonTokens.T_MAP;


import java.util.ArrayDeque;

/**
 * Parse Json as a stream, calling 'StreamCb' callbacks as state
 * changes and/or name/value pairs are provided.
 * <p>
 *  Example
 *  <pre>
 *      JsonStream baseStreamer = new JsonStreamBytes(jsonBytes,
 *         (streamer, name, value, type) -> {
 *             if (JsonStream.START_ARRAY == value && "features".equals(name))
 *                 streamer.push(this::onFeature, "onFeature");
 *         }, SunVectorBldJstream::onError);
 *
 *  </pre>
 *
 * Json syntax - https ://www.json.org/json-en.html
 */
public class JsonStreamBytes implements JsonStream {
    private final byte[] jsonBytes;
    private final int len;
    private int idx;
    private int level;

    // Byte equivalent of characters constants.
    public static final byte B_LBRACE = '{';
    public static final byte B_RBRACE = '}';
    public static final byte B_LBRACKET = '[';
    public static final byte B_RBRACKET = ']';
    public static final byte B_DQUOTE = '"';
    public static final byte B_COLON = ':';
    public static final byte B_COMMA = ',';
    public static final byte B_BSLASH = '\\';

    // Functional callbacks
    private String dbgTag = "";
    private StreamCb streamCb;
    private final ErrorCb errorCb;

    private record CbHolder(StreamCb streamCb, String dbgTag) {}
    private final ArrayDeque<JsonStreamBytes.CbHolder> queueCb = new ArrayDeque<>(10);


    /**
     * Common constructor to stream parse json bytes.
     */
    public JsonStreamBytes(byte[] jsonBytes, StreamCb streamCb, ErrorCb errorCb) {
        this.jsonBytes = jsonBytes;
        this.streamCb = streamCb;
        this.errorCb = errorCb;
        this.len = jsonBytes.length;

        level = idx = 0;

        try {
            next(0, N_ROOT);
        } catch (Exception ex) {
            System.err.println("JsonStream exception=" + ex);
            gotError();
        }
    }

    /**
     * Experimental constructor intended for split json threaded optimization.
     */
    public JsonStreamBytes(byte[] jsonBytes, int startAt, int endAt, StreamCb streamCb, ErrorCb errorCb) {
        this.jsonBytes = jsonBytes;
        this.streamCb = streamCb;
        this.errorCb = errorCb;
        this.len = endAt;
        this.idx = startAt;

        level = 0;

        try {
            next(-1, N_ARRAY);
        } catch (Exception ex) {
            System.err.println("JsonStream exception=" + ex);
            gotError();
        }
    }

    /**
     * Switch to new callback, push previous onto queue.
     */
    public void push(StreamCb streamCb, String dbgTag) {
        // ALog.d.tagMsg(this, "Json push=", this.dbgTag, " -> ", dbgTag);
        queueCb.addLast(new CbHolder(this.streamCb, this.dbgTag));
        this.streamCb = streamCb;
        this.dbgTag = dbgTag;
    }

    /**
     * Restore previous callback.
     */
    public void pop() {
        CbHolder cbHolder = queueCb.removeLast();
        // ALog.d.tagMsg(this, "Json pop ", dbgTag, " -> ", cbHolder.dbgTag);
        this.streamCb = cbHolder.streamCb;
        this.dbgTag = cbHolder.dbgTag();
    }

    private void gotError() {
        String errMsg;
        if (idx < len) {
            errMsg = "Error at position=" + idx
             //       + ", json=" + ByteUtils.toString(jsonBytes, idx, 10) // (byte[], offset, length)
                    + " length=" + len;
        } else {
            errMsg = "Error at position=" + idx + " JsonTotalLen=" + len;
        }
        if (errorCb != null)
           errorCb.onError(idx, errMsg);
        else
            System.err.println(errMsg);
    }

    /**
     * Single method where StreamCb Functional interface is called, to make it
     * easier to debug.
     */
    void dbgOnValue(JsonStreamBytes streamer, String name, Object value, int type) {
        // ALog.d.tagMsg(this, "Json name=", name, " value=", value);  // DEBUG
        // if (IGNORE != value)
            streamCb.onValue(streamer, name, value, type);
    }

    /**
     * Json level for {} or [] nesting.
     */
    public int getLevel() {
        return level;
    }

    // Parse next field
    public void next(int endLevel, String name) {
        while (idx < len) {
            byte c = skipWhite();
            idx++;

            switch (c) {
                case B_LBRACE: // '{':
                    dbgOnValue(this, name, START_MAP, T_MAP);
                    level++;
                    break;
                case B_RBRACE: // '}':
                    level--;
                    dbgOnValue(this, name, END_MAP, T_MAP);
                    break;
                case B_RBRACKET: // ']':
                    idx--;
                    return;
                case B_DQUOTE: // '"':
                    String name2 = getQuoted();
                    if (skipWhite() == B_COLON)
                        idx++;
                    dbgOnValue(this, name2, getValue(level, name2), T_MAP);
                    break;
                case B_COMMA: // ',':
                    break;
                default:
                    gotError();
                    return; // Error
            }
            if (level == endLevel)
                return;
        }
    }

    private int findQuote(int pos) {
        while (jsonBytes[pos] != B_DQUOTE && pos < len)
            pos++;
        return pos;
    }

    /**
     * Warning - does not handle any special character conversion, such as \n to newline.
     */
    private String getQuoted() {
        int beg = idx;
        int end = beg -1; // backup 1, because do-loop starts with ++
        do { end++; end = findQuote(end); }
            while (jsonBytes[end-1] == B_BSLASH); // '\\');

        idx = end+1;
        // return new String(jsonBytes, beg, end-beg);   // (byte[], offset, length)
        return com.landenlabs.test.JsonStream2.ByteUtils.toString(jsonBytes, beg, end-beg);
    }

    private Object getValue(int ourLevel, String name) {
        byte c = skipWhite();
        int beg = idx;
        if (c == B_DQUOTE) {
            idx++;
            return getQuoted();
        } else if (c == B_LBRACKET) {   // '['
            idx++;
            skipWhite();
            dbgOnValue(this, name, START_ARRAY, T_ARRAY);
            level++;

            int arrIdx = 0;
            while (idx < len && jsonBytes[idx] != B_RBRACKET) {   // ']'
                dbgOnValue(this, N_ARRAY, getValue(ourLevel, N_ARRAY), arrIdx++);
                if (skipWhite() == B_COMMA) // ','
                    idx++;
            }
            idx++;  // skip over ']'
            level--;
            return END_ARRAY;
        } else if (c == B_LBRACE) {  // '{'
            next(ourLevel, name);
            return IGNORE;
        } else {
            toFieldEnd();
            // Optimize - keep as byte array for numeric conversions and static string comparison.
            String value = com.landenlabs.test.JsonStream2.ByteUtils.toString(jsonBytes, beg, idx-beg);     // (byte[], offset, length)
            if (value.isEmpty() )
                return null;  // { } or [ ]
            if (value.indexOf('.') != -1) {
                return Double.parseDouble(value);
                //   If using JDOUBLE, make matching change to SunVectorBldJstream, etc
                // return JDOUBLE.set(value); //  Double.parseDouble(value);
            }
            if (value.equalsIgnoreCase("null"))
                return null;
            if (value.equalsIgnoreCase("true"))
                return Boolean.TRUE;
            if (value.equalsIgnoreCase("false"))
                return Boolean.FALSE;
            return Long.parseLong(value);
        }
    }

    private boolean isWhitespace(byte b) {
        return b == (byte)' ' || b == (byte)'\t' || b == (byte)'\r' || b == (byte)'\n';
    }

    private byte skipWhite() {
        byte c = (byte)'?';
        while (idx < len && isWhitespace(c = jsonBytes[idx]))
            idx++;
        return c;
    }

    // Advance until one of:  comma, brace, bracket or whitespace.
    private void toFieldEnd() {
        byte c;
        while (idx < len) {
            c = jsonBytes[idx];
            if (isWhitespace(c) || c == B_COMMA || c == B_RBRACE || c == B_RBRACKET)
                break;
            idx++;
        }
    }
}
