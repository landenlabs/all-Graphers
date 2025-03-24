package com.landenlabs.test.Data;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * Not thread safe
 */
public class ArrayCache16 implements ArrayCache {

    private static final int LEN = 16;
    private final Object[][] arrays = new Object[LEN][];
    int size = 0;

    public ArrayCache16() {
        Arrays.fill(arrays, null);
    }

    public  void add(Object[] objArray) {
        int idx = find(objArray.length);
        arrays[idx] = objArray;
        size = Math.max(idx+1, size);
    }

    private int find(int len) {
        for (int idx = 0; idx < size; idx++) {
            Object[] arr = arrays[idx];
            if (len <= arr.length)
                return idx;
        }
        return Math.min(size, LEN-1);
    }

    public Object[] get(int len) {
        int idx = find(len);
        if (idx >= size || arrays[idx].length < len) {
            return new Object[len];
        }
        Object[] arr = arrays[idx];
        if (size > 1) {
            // System.arraycopy(arrays, idx + 1, arrays, idx, size - idx);
            for (int tmpIdx = idx; tmpIdx < size-1; tmpIdx++)
                arrays[tmpIdx] = arrays[tmpIdx+1];
            arrays[size-1] = null;
        }
        size--;
        return arr;
    }

    @NonNull
    public Object[] swap(Object[] inArray, int len) {
        Object[] outArray = get(len);
        if (inArray != null) {
            System.arraycopy(inArray, 0, outArray, 0, Math.min(len, inArray.length));
            add(inArray);
        }
        return outArray;
    }

    @NonNull
    public Object[] exact(Object[] inArray, int len) {
        Object[] outArray = new Object[len];
        if (inArray != null) {
            System.arraycopy(inArray, 0, outArray, 0, Math.min(len, inArray.length));
            add(inArray);
        }
        return outArray;
    }

    public int size() {
        return size;
    }
}
