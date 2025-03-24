package com.landenlabs.test.Data;

import java.util.ArrayList;

public interface SunVectorDataI {
    boolean equals(SunVectorDataI other);
    int hashCode();

    ArrayList<PolyItems.Item> getItems();
}
