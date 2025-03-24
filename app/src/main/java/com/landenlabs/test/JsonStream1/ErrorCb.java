package com.landenlabs.test.JsonStream1;

@FunctionalInterface
public interface ErrorCb {
    void onError(int pos, String msg);
}