package com.landenlabs.test.JsonStream1;

import com.landenlabs.test.Data.PolyItems;
import com.landenlabs.test.Data.SunVectorDataI;

import java.util.ArrayList;

/**
 *
 */
public class SunVectorData  implements SunVectorDataI {

    public ArrayList<PolyItems.Item> items;

    public SunVectorData(ArrayList<PolyItems.Item> items) {
        this.items = items;
    }

    @Override
    public ArrayList<PolyItems.Item> getItems() {
        return items;
    }

    @Override
    public boolean equals(SunVectorDataI other) {
        return items.equals(other.getItems());
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
