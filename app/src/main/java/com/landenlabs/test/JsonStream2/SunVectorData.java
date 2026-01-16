/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;

import static com.landenlabs.all_graphers.ui.data.StrUtils.hasText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.landenlabs.all_graphers.ui.logger.ALog;

import java.util.ArrayList;

/**
 * Hold collection of Vector data provide from SUN SSDS vector response.
 * <p>
 * Cache and reuse
 */
public class SunVectorData   {

    public final String prodId;
    public final int[] xyz;
    public final ArrayList<SunVectorItems.SunItem> items;
    public final long parsedMilli;
    public long requestMilli = 0;

    public SunVectorData(@Nullable String prodId, int[] xyz, @NonNull ArrayList<SunVectorItems.SunItem> items) {
        this.prodId = prodId;
        this.xyz = xyz;
        this.items = items;
        this.parsedMilli = System.currentTimeMillis();
    }

    public SunVectorData cloneIt() {
        return new SunVectorData(prodId, xyz, new ArrayList<>(items));
    }

    public ArrayList<SunVectorItems.SunItem> getItems() {
        return items;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof SunVectorData) && items.equals(((SunVectorData) obj).getItems());
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    public void append(SunVectorData vectorData) {
        items.addAll(vectorData.items);
    }

    boolean isValid() {
        boolean ok = hasText(prodId) && (items != null) && (!items.isEmpty()) && isFresh();
        if (!ok)
            ALog.d.tagFmt(this, "NOT VALID prod=%s, size=%d, tile=[%d, %d, %d] %s",
                    prodId, (items != null ? items.size() : 0), xyz[0], xyz[1], xyz[2], this);
        return ok;
    }
    boolean isFresh() {
        return (parsedMilli > 0);
        // return (System.currentTimeMillis() - parsedMilli) < TimeUnit.MINUTES.toSeconds(VEC_DATA_MAX_AGE_MIN);
    }
}
