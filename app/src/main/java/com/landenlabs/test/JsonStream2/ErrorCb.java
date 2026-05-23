/*
 * Copyright (c) 2026 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 */

package com.landenlabs.test.JsonStream2;

@FunctionalInterface
public interface ErrorCb {
    void onError(int pos, String msg);
}