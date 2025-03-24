package com.landenlabs.test.JsonStream1;

public interface JsonStream {
    void push(StreamCb streamCb, String dbgTag) ;
    void pop();
    int getLevel();
}
