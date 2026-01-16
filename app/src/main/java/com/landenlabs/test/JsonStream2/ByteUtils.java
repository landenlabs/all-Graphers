/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Isolate conversion of Bytes to String.
 */
public class ByteUtils {

    @NonNull
    public static String toString(@NonNull byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @NonNull
    public static String toString(@NonNull byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length);
    }

    public static void gc() {
        System.gc();
    }

    @Nullable
    public static String readLine(@NonNull BufferedReader br) throws IOException {
        return br.readLine();
    }

    public static byte[] readNBytes(InputStream inputStream, int maxLen) throws IOException {
        if (maxLen == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[Math.min(maxLen, 4096)];
        int totalBytesRead = 0;
        int bytesRead;

        while (totalBytesRead < maxLen &&
                (bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, maxLen - totalBytesRead))) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        return outputStream.toByteArray();
    }

    @NonNull
    public static String getStringX(@NonNull Context context, @StringRes int strRes, boolean encoded) {
        String value = context.getResources().getString(strRes);
        if (encoded) {
            // Reverse base64 of string.  See build.gradle for encoding.
            byte[] decoded = Base64.decode(value, Base64.DEFAULT);
            return new String(decoded);
        } else {
            return value;
        }
    }

    public static double getZero2One() {
        return Math.random();
    }
}
