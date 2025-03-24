package com.landenlabs.test.JsonStream1;

@FunctionalInterface
public interface StreamCb {
    void onValue(JsonStream streamer, String name, Object value, int type);
}
