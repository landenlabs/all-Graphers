package com.landenlabs.test.Data;

import androidx.annotation.NonNull;

/**
 * Not thread safe
 */
public class ArrayCacheNOP implements ArrayCache {

    public  void add(Object[] objArray) {
    }

    public Object[] get(int len) {
        return new Object[len];
    }

    @NonNull
    public Object[] swap(Object[] inArray, int len) {
        return exact(inArray, len);
    }

    @NonNull
    public Object[] exact(Object[] inArray, int len) {
        Object[] outArray = new Object[len];
        if (inArray != null) {
            System.arraycopy(inArray, 0, outArray, 0, Math.min(len, inArray.length));
        }
        return outArray;
    }

    public int size() {
        return 0;
    }
}
