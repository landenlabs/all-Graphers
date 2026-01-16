/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

@FunctionalInterface
public interface ErrorCb {
    void onError(int pos, String msg);
}