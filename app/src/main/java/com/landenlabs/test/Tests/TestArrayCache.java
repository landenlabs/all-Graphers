package com.landenlabs.test.Tests;

import com.landenlabs.test.Data.ArrayCache;
import com.landenlabs.test.Data.ArrayCache16;

import java.io.IOException;
import java.util.Arrays;

import static com.landenlabs.test.utils.MemUtils.initMemory;
import static com.landenlabs.test.utils.MemUtils.showMemory;

public class TestArrayCache {

    public static void main(String[] args) throws IOException {
        ArrayCache cache = new ArrayCache16();
        initMemory();
        for (int idx = 0; idx < 5; idx++) {
            cache.add(new Object[200]);
            cache.add(new Object[20]);
            cache.add(new Object[60]);
            cache.add(new Object[300]);
            cache.add(new Object[40]);
            cache.add(new Object[100]);
            showMemory(String.format("ArrayCache %2d: cache size=%d ", idx, cache.size()));
        }

        Integer[] nums = new Integer[35];
        Arrays.fill(nums, 123);
        Object[] arr;
        arr = cache.swap(nums, 22);
        arr = cache.swap(arr, 40);
        arr = cache.swap(arr, 50);
        arr = cache.swap(arr, 20);
        System.out.printf("swap test arr=%d\n", arr.length);

        System.out.println("--Test done--");
    }

}
