package com.landenlabs.test.Data;


import androidx.annotation.NonNull;

/**
 * Not thread safe
 */
public interface ArrayCache {

    void add(Object[] objArray);
    Object[] get(int len);

    @NonNull
    Object[] swap(Object[] inArray, int len);

    @NonNull
    Object[] exact(Object[] inArray, int len);

    int size();
}
