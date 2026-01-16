/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

@FunctionalInterface
public interface StreamCb {
    void onValue(JsonStream streamer, String name, Object value, int type);
}
