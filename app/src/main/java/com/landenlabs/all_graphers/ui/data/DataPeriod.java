/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;

public enum DataPeriod {

    // Code is the same as default java enum ordinal but this guaranties its value.
    // NOTE - keep UI position in sync with code values.
    //    uiset_row_with_circle_obs_fcst

    Obs(0), Fcst(1);

    DataPeriod(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static DataPeriod from(int code) {
        for (DataPeriod period : values()) {
            if (period.code() == code)
                return period;
        }
        return Fcst;
    }
    private final int code;
}
