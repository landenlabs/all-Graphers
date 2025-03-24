/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;

public enum DataTypes {

    NONE(0),
    CURRENT(1<<0),
    HOURLY(1<<1),
    DAILY(1<<2),
    RADAR(1<<3),
    CLOUDS(1<<4),
    TEMPERATURE(1<<5),
    WIND(1<<6);

    private final int value;

    DataTypes(int value) {
        this.value = value;
    }

    public static DataTypes from(String name) {
        for (DataTypes dtype : values()) {
            if (dtype.name().equalsIgnoreCase(name))
                return dtype;
        }
        return NONE;
    }

    public int getValue() {
        return this.value;
    }
}
