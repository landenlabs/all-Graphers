/*
 * Copyright (c) 2026 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 */

package com.landenlabs.test.JsonStream2;

public interface JsonStream {
    void push(StreamCb streamCb, String dbgTag) ;
    void pop();
    int getLevel();
}
