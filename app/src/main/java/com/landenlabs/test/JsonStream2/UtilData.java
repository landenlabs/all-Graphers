/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.test.JsonStream2;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Data manipulation routines.
 */
public class UtilData {

    public interface Filter<DD, RR> {
        RR filter(DD data);
    }

    public static <DD, RR> RR ifData(DD data, Filter<DD, RR> haveData, Filter<DD, RR> noData) {
        return (data != null) ? haveData.filter(data) : noData.filter(data);
    }

    public static boolean isAllDone(CompletableFuture<?> future) {
        return (future == null) || future.isCancelled() || future.isDone() || future.isCompletedExceptionally();
    }

    public static boolean isDone(CompletableFuture<?> future) {
        try {
            return (future != null) && future.isDone() && !future.isCompletedExceptionally();
        } catch (Exception ex) {
            return false;   // is Cancel or CompletedExceptionally
        }
    }

    public static class Mutable<TT> {
        public TT value;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> T cast(@NonNull Object obj) {
        //noinspection unchecked
        return (T) Objects.requireNonNull(obj);
    }

    /** @noinspection DataFlowIssue*/
    public static boolean isEqual(@Nullable Number num1, @Nullable Number num2) {
        return !anyNull(num1, num2) && num1.floatValue() == num2.floatValue();
    }

    public static int hashElse(@Nullable Object obj, int defHash) {
        return (obj == null) ? defHash : obj.hashCode();
    }


    public static <TT> boolean hasValue(ArrayList<TT> list, int idx) {
        return (list != null && idx < list.size() && list.get(idx) != null);
    }

    public static boolean isFloat(Number number) {
        return (number != null && !Float.isNaN(number.floatValue()));
    }

    public static void withFloat(Number number, Consumer<Float> consumer) {
        if  (isFloat(number)) consumer.accept( number.floatValue());
    }

    public static float asFloat(Object obj, float defVal) {
        return parseFloat(obj, defVal);
    }

    public static double asDouble(Object obj, double defVal) {
        return parseDouble(obj, defVal);
    }

    // See identical methods toRange()
    public static int clamp(int value, int minVal, int maxVal) {
        return Math.min(Math.max(value, minVal), maxVal);
    }
    // See identical methods toRangeD()
    public static double clamp(double value, double minVal, double maxVal) {
        return Math.min(Math.max(value, minVal), maxVal);
    }

    public static double roundTo(double value, int places) {
        assert (places >= 0);
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static int asInteger(Number number, int defVal) {
        return (number != null) ? number.intValue() : defVal;
    }

    public static long asLong(Number number, long defVal) {
        return (number != null) ? number.longValue() : defVal;
    }

    public static boolean anyNull(Object... objs) {
        for (Object obj : objs) {
            if (obj == null)
                return true;
        }
        return false;
    }

    @SafeVarargs
    public static <TT> TT firstNotNull(@Nullable TT... values) {
        if (values != null) {
            for (TT value : values) {
                if (value != null)
                    return value;
            }
        }
        return null;
    }

    @SafeVarargs
    public static <TT> boolean anyOf(TT item, TT... matches) {
        for (TT match : matches)
            if (item == match)
                return true;
        return false;
    }

    @SafeVarargs
    public static <TT> boolean notAnyOf(TT item, TT... matches) {
        for (TT match : matches)
            if (item == match)
                return false;
        return true;
    }

    public static <TT extends Enum<TT>> TT toEnum(@NonNull Class<TT> enumType, @Nullable String str, TT defValue) {
        if (str == null || str.trim().isEmpty()) {
            return defValue;
        }
        for (TT anEnum : enumType.getEnumConstants()) {
            if (anEnum.name().equalsIgnoreCase(str)) {
                return anEnum;
            }
        }
        return defValue;
    }

    public interface FetchF<E> {
        float get(E data);
    }

    public static <E> float maxF(@Nullable Collection<E> list, @NonNull FetchF<E> fetch) {
        float maxData = Float.NaN;
        if (list != null) {
            for (E data : list) {
                maxData = Float.isNaN(maxData) ? fetch.get(data) : Math.max(maxData, fetch.get(data));
            }
        }
        return maxData;
    }

    public static <TT> int sizeOf(Collection<TT> collection) {
        return (collection == null) ? 0 : collection.size();
    }


    @Nullable
    public static <TT> TT firstOf(@Nullable Set<TT> set) {
        return (set != null) ? set.iterator().next() : null;
    }

    @NonNull
    public static <TT> TT firstOf(@Nullable Set<TT> set, TT defValue) {
        return (set != null) ? set.iterator().next() : defValue;
    }

    // ========  Helper methods to manage Map, arrays and List containers  ===============


    /**
     * Return map value else default.
     */
    @SuppressWarnings("unchecked")
    public static <KeyT, ValT, OutT> OutT getIt(@Nullable Map<KeyT, ValT> map, KeyT key, OutT defValue) {
        return (map != null && map.containsKey(key)) ? (OutT) (map.get(key)) : defValue;
    }

    public static <KeyT, ValT> ValT getNoCase(@Nullable Map<KeyT, ValT> map, @NonNull String key) {
        if (map != null) {
            for (Map.Entry<KeyT, ValT> entry : map.entrySet()) {
                if (key.equalsIgnoreCase(entry.getKey().toString()))
                    return entry.getValue();
            }
        }
        return null;
    }

    public static Object find(Object data, String findKey, Object defValue) {
        final Object NO_MATCH = 0x1234;
        Object result;
        try {
            Object obj = data;
            if (obj instanceof Map<?, ?> mapData) {
                for (Map.Entry<?, ?> entry : mapData.entrySet()) {
                    if (findKey.equals(entry.getKey())) {
                        return entry.getValue();
                    }
                    result = find(entry.getValue(), findKey, NO_MATCH);
                    if (result != NO_MATCH)
                        return result;
                }
            } else if (obj instanceof List<?> arrData) {
                for (int idx = 0; idx < arrData.size(); idx++) {
                    result = find(arrData.get(idx), findKey, NO_MATCH);
                    if (result != NO_MATCH)
                        return  result;
                }
            }
        } catch (Exception ex) {
            // ALog.none.tagMsg(ALog.TAG_PREFIX, ex);  // Ignore error but keep code scan happy.
        }
        return defValue;
    }


    @Nullable
    public static <KeyT, ValT> KeyT findKeyOfValue(@Nullable Map<KeyT, ValT> map, ValT find, KeyT def) {
        if (map == null)
            return def;

        for (Map.Entry<KeyT, ValT> entry : map.entrySet()) {
            if (Objects.equals(find, entry.getValue()))
                return entry.getKey();
        }
        return def;
    }

    @Nullable
    public static <KeyT, ValT> List<KeyT> findKeysOfValue(@Nullable Map<KeyT, ValT> map, ValT find, List<KeyT> def) {
        if (map == null)
            return def;

        List<KeyT> keys = new ArrayList<>();
        for (Map.Entry<KeyT, ValT> entry : map.entrySet()) {
            if (Objects.equals(find, entry.getValue()))
                keys.add(entry.getKey());
        }

        return keys.isEmpty() ? def : keys;
    }

    @Nullable
    public static <String, ValT> String findKey(@Nullable Map<String, ValT> map, java.lang.String rex) {
        if (map == null)
            return null;

        java.lang.String regex = rex.replace("*", ".*");

        for (Map.Entry<String, ValT> entry : map.entrySet()) {
            String keyStr = entry.getKey();
            if (Pattern.matches(regex, (CharSequence) keyStr)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    public static <KeyT, ValT> ValT firstValueOf(@Nullable Map<KeyT, ValT> map) {
        return (map != null) ? map.values().iterator().next() : null;
    }

    @Nullable
    public static <KeyT, ValT> KeyT firstKeyOf(@Nullable Map<KeyT, ValT> map) {
        return (map != null) ? map.keySet().iterator().next() : null;
    }

    @Nullable
    public static <String, ValT> ValT find(@Nullable Map<String, ValT> map, java.lang.String rex) {
        if (map == null)
            return null;

        java.lang.String regex = rex.replace("*", ".*");

        for (Map.Entry<String, ValT> entry : map.entrySet()) {
            String keyStr = entry.getKey();
            if (Pattern.matches(regex, (CharSequence) keyStr)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static <E, F> void putIfNew(@NonNull Map<E, F> map, E key, F value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

    /**
     * Helper methods to manage Array container.
     */
    public static <E> boolean inRange(int idx, final E[] dataArray) {
        return dataArray != null && idx >= 0 && idx < dataArray.length;
    }

    public static <E> boolean contains(final E[] dataArray, E find) {
        for (E item : dataArray) {
            if (item == find)
                return true;
        }
        return false;
    }

    @Nullable
    public static Object findMap(@Nullable ArrayList<Object> arr, Object want, String mapKey) {
        if (arr == null)
            return null;

        for (Object obj : arr) {
            if (obj instanceof Map<?,?> map) {
                if (Objects.equals(map.get(mapKey), want))
                    return map;
            }
        }
        return null;
    }

    public static <E> E getIt(E[] dataArray, int idx) {
        return getIt(dataArray, idx, null);
    }

    public static <E> E getIt(E[] dataArray, int idx, E defValue) {
        return (dataArray != null && idx >= 0 && idx < dataArray.length) ? dataArray[idx] : defValue;
    }

    public static <E> int index(E[] arr, E want, int startIdx, int noValDef) {
        for (int idx = startIdx; idx < arr.length; idx++)
            if (arr[idx].equals(want))
                return idx;
        return noValDef;
    }

    /**
     * Helper methods to manage List container.
     */
    public static <E> boolean inRange(int idx, final List<E> dataList) {
        return dataList != null && idx >= 0 && idx < dataList.size();
    }
    public static <TT> boolean hasIt(List<TT> dataArray, int idx) {
        return getIt(dataArray, idx) != null;
    }
    @Nullable
    public static <TT> TT getIt(List<TT> dataArray, int idx) {
        return getIt(dataArray, idx, null);
    }

    @NonNull
    public static <TT> TT getIt(List<TT> dataArray, int idx, TT defValue) {
        return (dataArray != null && idx >= 0 && idx < dataArray.size()) ? dataArray.get(idx) : defValue;
    }


    public static String parseString(@Nullable Object obj, String defValue) {
        if (obj instanceof String str) {
            return str;
        }
        return defValue;
    }

    /**
     * Parse string value, return default if parse fails.
     */
    public static int parseInt(@Nullable Object obj, int defValue) {
        int result = defValue;
        if (obj instanceof Number num) {
            result = num.intValue();
        } else if (obj instanceof String) {
            try {
                result = Integer.parseInt((String) obj);
            } catch (Exception ignore) {
                // ALog.none.tagMsg("parseInt", exIgnore);
            }
        }
        return result;
    }

    public static long parseLong(@Nullable Object obj) {
        return parseLong(obj, 0L);
    }
    public static long parseLong(@Nullable Object obj, long defValue) {
        long result = defValue;
        if (obj instanceof Number num) {
            result = num.longValue();
        } else if (obj instanceof String) {
            try {
                result = Long.parseLong((String) obj);
            } catch (Exception ignore) {
                // ALog.none.tagMsg("parseLong", ignore);
            }
        }
        return result;
    }

    /**
     * Parse string value, return default if parse fails.
     */
    public static float parseFloat(@Nullable Object obj) {
        return parseFloat(obj, Float.NaN);
    }
    public static float parseFloat(@Nullable Object obj, float defValue) {
        float result = defValue;
        if (obj instanceof Number num) {
            result = num.floatValue();
        } else if (obj instanceof String) {
            try {
                result = Float.parseFloat(obj.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return result;
    }

    /**
     * Parse string value, return default if parse fails.
     */
    public static double parseDouble(@Nullable Object obj) { return parseDouble(obj, Double.NaN); }
    public static double parseDouble(@Nullable Object obj, double defValue) {
        double result = defValue;
        if (obj instanceof Number num) {
            result = num.doubleValue();
        } else if (obj instanceof String) {
            try {
                result = Double.parseDouble(obj.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return result;
    }
}
