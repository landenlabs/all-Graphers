/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

public interface JsonStream {
    void push(StreamCb streamCb, String dbgTag) ;
    void pop();
    int getLevel();
}
