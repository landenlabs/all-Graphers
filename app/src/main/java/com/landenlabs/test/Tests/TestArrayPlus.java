package com.landenlabs.test.Tests;

import com.landenlabs.test.Data.ArrayCache;
import com.landenlabs.test.Data.ArrayCache16;
import com.landenlabs.test.Data.ArrayListPlus;

import java.io.IOException;
import java.util.ArrayList;

import static com.landenlabs.test.utils.MemUtils.initMemory;
import static com.landenlabs.test.utils.MemUtils.showMemory;

public class TestArrayPlus {
    private static long prevMemory = 0;
    private static Runtime runtime;
    private static int[] SIZES = new int[] { 7, 77, 37, 97, 37, 127, 67, 17, 7, 3, 2, 2, 2, 2 };

    public static void main(String[] args) throws IOException {
        ArrayCache cache = new ArrayCache16();
        {
            initMemory();
            ArrayListPlus<Integer> nums = null;
            int val = 0;
            for (int idx = 0; idx < SIZES.length; idx++) {
                nums = new ArrayListPlus<>(cache);
                int len = SIZES[idx]; // Math.round((float)Math.random() * 10) * 7 + 13;
                for (int tmpIdx = 0; tmpIdx < len; tmpIdx++) {
                    nums.add(val++);
                }
            }
            showMemory(String.format("swap test arr=%,d\n", nums.size()));
        }

        {
            initMemory();
            ArrayList<Integer> nums = null;
            int val = 0;
            for (int idx = 0; idx < SIZES.length; idx++) {
                nums = new ArrayList<>();
                int len = SIZES[idx]; // Math.round((float)Math.random() * 10) * 7 + 13;
                for (int tmpIdx = 0; tmpIdx < len; tmpIdx++) {
                    nums.add(val++);
                }
            }
            showMemory(String.format("normal test arr=%,d\n", nums.size()));
        }


        System.out.println("--Test done--");
    }

}
