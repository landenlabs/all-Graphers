package com.landenlabs.test.Data;

import com.landenlabs.test.JsonStream2.SunVectorData;
import com.landenlabs.test.JsonStream2.SunVectorItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountThem {

    public void print() {
        System.out.printf("    CountThem: map=%,d map[]=%,d leaf=%,d list=%,d list[]=%,d\n", mapCnt, mapItemCnt, mapItemCnt - mapCnt, arrayCnt, arrayItemCnt );
    }

    public int mapCnt = 0;
    public int mapItemCnt = 0;
    public int arrayCnt = 0;
    public int arrayItemCnt = 0;

    public void clear() {
       mapCnt = 0;
       mapItemCnt = 0;
       arrayCnt = 0;
       arrayItemCnt = 0;
    }

    public static CountThem count(Map<String, Object> mapData, CountThem counters) {
        counters.mapCnt++;
        counters.mapItemCnt += mapData.size();
        for (Object obj : mapData.values()) {
            if (obj instanceof Map map2)
                count(map2, counters);
            else if (obj instanceof List list2) {
                count(list2, counters);
            }
        }
        return counters;
    }

    public static CountThem count(List list, CountThem counters) {
        counters.arrayCnt++;
        counters.arrayItemCnt += list.size();
        for (Object obj : list) {
            if (obj instanceof Map map2)
                count(map2, counters);
            else if (obj instanceof List list2) {
                count(list2, counters);
            }
        }
        return counters;
    }

    public static CountThem count(SunVectorData vData, CountThem counters) {
        counters.arrayCnt++;
        counters.arrayItemCnt += vData.items.size();
        for (SunVectorItems.SunItem item : vData.items) {
            count(item.properties.properties, counters);
        }
        return counters;
    }

}
