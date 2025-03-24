package com.landenlabs.test.utils;

public class MemUtils {
    public static long prevMemory = 0L;
    public static  Runtime runtime;

    public static class Info {
        public long deltaMilli;
        long t1, t2;
        public long deltaMem;
    }
    public static Info info = new Info();

    public static void  initMemory() {
        if (runtime == null)
            runtime = Runtime.getRuntime();
        System.gc();
        prevMemory = runtime.totalMemory() - runtime.freeMemory();
        info.t1 = System.currentTimeMillis();
        info.deltaMilli = info.deltaMem = 0;
    }

    public static Info  showMemory(String msg) {
        info.t2 = System.currentTimeMillis();
        info.deltaMilli = info.t2 - info.t1;

        long maxMemory = runtime.totalMemory() - runtime.freeMemory();
        System.gc();
        long activeMemory = runtime.totalMemory() - runtime.freeMemory();
        info.deltaMem = (activeMemory - prevMemory);
        System.out.printf("%-30s milli=%,6d memory=%,10d max=%,10d\n", msg, info.deltaMilli, info.deltaMem, maxMemory);
        prevMemory = activeMemory;

        info.t1 = System.currentTimeMillis();   // Ready for next run.
        return info;
    }
}
