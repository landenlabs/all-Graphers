/*
    Dennis Lang  (landenlabs.com)
    Dec-2024
 */
package com.landenlabs.test.JsonStream1;

import static com.landenlabs.test.JsonStream1.JDouble.JDOUBLE;
import static java.lang.Character.isWhitespace;

import java.util.ArrayDeque;

/**
 * Parse Json into nested hash maps.
 * Json syntax - https://www.json.org/json-en.html
 */
public class JsonStreamBytes implements JsonStream {
    private final byte[] jsonBytes;
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

    public static final byte B_LBRACE = '{';
    public static final byte B_RBRACE = '}';
    public static final byte B_LBRACKET = '[';
    public static final byte B_RBRACKET = ']';
    public static final byte B_DQUOTE = '"';
    public static final byte B_COLON = ':';
    public static final byte B_COMMA = ',';
    public static final byte B_BSLASH = '\\';

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

    void gotError() {
        String errMsg;
        if (idx < len) {
            errMsg = "Error at position=" + idx
                    + ", json=" + new String(jsonBytes, idx, 10) // (byte[], offset, length)
                    + " length=" + len;
        } else {
            errMsg = "Error at position=" + idx + " JsonTotalLen=" + len;
        }
        if (errorCb != null)
           errorCb.onError(idx, errMsg);
        else
            System.err.println(errMsg);
    }

    void dbgOnValue(JsonStreamBytes streamer, String name, Object value, int type) {
        // if (IGNORE != value)
            streamCb.onValue(streamer, name, value, type);
    }

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

    private String getQuoted() {
        int beg = idx++;
        int end = beg;
        do { end++; end = findQuote(end); }
            while (jsonBytes[end-1] ==  B_BSLASH); // '\\');

        idx = end+1;
        return new String(jsonBytes, beg, end-beg);   // (byte[], offset, length)
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
            // TODO - keep as byte array for numeric conversions.
            String value = new String(jsonBytes, beg, idx-beg);     // (byte[], offset, length)
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
        byte c = (byte)'?';
        while (idx < len) {
            c = jsonBytes[idx];
            if (isWhitespace(c) || c == B_COMMA || c == B_RBRACE || c == B_RBRACKET)
                break;
            idx++;
        }
    }


    //Region [Custom JDouble class] ----------------------------------------------------------------
    /*
    public final static JDouble JDOUBLE = new JDouble();
    public static class JDouble  extends Number {

        public double dValue;

        public JDouble set(String str) {
            dValue = stringToDouble(str);
            return this;
        }

        @Override
        public int intValue() {
            assert(false);
            return 0;
        }

        @Override
        public long longValue() {
            assert(false);
            return 0;
        }

        @Override
        public float floatValue() {
            assert(false);
            return 0;
        }

        @Override
        public double doubleValue() {
            return dValue;
        }
    }

    public static double stringToDouble(String str)  {
        // Handle negative numbers
        int sign = 1;
        int startAt = 0;
        if (str.charAt(0) == '-') {
            sign = -1;
            startAt = 1;
        }

        double integerPart = 0;
        double decimalPart = 0;
        boolean isDecimalPointFound = false;
        double decimalDivisor = 1;

        // Iterate through each character in the string
        for (int i = startAt; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '.') {
                isDecimalPointFound = true;
            } else    {
                if (isDecimalPointFound) {
                    decimalPart = decimalPart * 10 + (currentChar - '0');
                    decimalDivisor *= 10;
                } else {
                    integerPart = integerPart * 10 + (currentChar - '0');
                }
            }
        }

        double finalResult = integerPart + (decimalPart / decimalDivisor);
        return sign * finalResult;
    }


    */
}
