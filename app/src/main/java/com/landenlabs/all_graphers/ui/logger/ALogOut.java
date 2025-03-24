/*
 * IBM Confidential
 * Copyright IBM Corp. 2016, 2021. Copyright WSI Corporation 1998, 2015
 */

package com.landenlabs.all_graphers.ui.logger;


import static com.landenlabs.all_graphers.ui.logger.ALog.isUnitTest;
import static com.landenlabs.all_graphers.ui.logger.ALogUtils.asString;
import static com.landenlabs.all_graphers.ui.logger.ALogUtils.joinStrings;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface which defines Log println and open methods.
 */

public class ALogOut {

    public Context context = null;
    public LogPrinter outPrn = new SysLog();

    public interface OnLog {
        void onLog(int level, Object tag, String msg);
    }

    public interface LogPrinter {
        int MAX_TAG_LEN = 0;    // was 100, but lets move user tag into message body
        int MAX_TAG_LEN_API24 = 0;  // was 23

        void println(int priority, String tag, Object... msgs);

        void open(@NonNull Context context);

        int maxTagLen();

        default void addListener(@Nullable Object tag, @NonNull OnLog onLog) {}
        default void removeListener(@Nullable Object tag) {}
    }

    // =============================================================================================
    public static class SysLog implements LogPrinter {

        // IllegalArgumentException	is thrown if the tag.length() > 23
        // for Nougat (7.0) releases (API <= 23) and prior, there is
        // no tag limit of concern after this API level.
        static final int LOG_TAG_LEN = MAX_TAG_LEN_API24;

        public void println(int priority, String tag, Object... msgs) {
            Context context = null;
            String msg = joinStrings(context, msgs);
            if (isUnitTest()) {
                // System.out.println(tag + msg);
            } else {
                Log.println(priority, tag, msg);
            }

            synchronized(logListeners) {
                for (Map.Entry<Object, OnLog> entry : logListeners.entrySet()) {
                    entry.getValue().onLog(priority, tag + asString(entry.getKey()), msg);
                }
            }
        }

        public void open(@NonNull Context context) {
        }

        public int maxTagLen() {
            return LOG_TAG_LEN;
        }


        private final Map<Object, OnLog> logListeners = new HashMap<>();

        public void addListener(@Nullable Object tag, @NonNull OnLog onLog) {
            synchronized(logListeners) {
                logListeners.put(tag, onLog);
            }
        }
        public void removeListener(@Nullable Object tag) {
            synchronized(logListeners) {
                logListeners.remove(tag);
            }
        }
    }
}
