/*
 * Copyright (c) 2026 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 */

package com.landenlabs.test.JsonStream2;

@FunctionalInterface
public interface StreamCb {
    void onValue(JsonStream streamer, String name, Object value, int type);
}
