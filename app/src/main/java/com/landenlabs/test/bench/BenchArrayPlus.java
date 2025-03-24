// package com.landenlabs.test.bench;

package com.landenlabs.test.bench;

/*
@BenchmarkMode(Mode.AverageTime)
// @BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 1, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 8000, timeUnit = TimeUnit.MILLISECONDS)
// @Warmup(iterations = 3)
// @Measurement(iterations = 20)

public class BenchArrayPlus {


    volatile public static long parser1Size = 0;
    volatile public static long parser2Size = 0;
    private static String json;

    // @Param({"50000"})
    @Param({"10000"})
    private int N;

    private static int[] SIZES = new int[] { 7, 77, 37, 97, 37, 127, 67, 17, 7, 3, 2, 2, 2, 2 };
    ArrayCache cache = new ArrayCache16();

    // @Setup
    public void setup() {
        try {
            if (json == null || json.length() == 0) {
                String jsonPath = new java.io.File("src/main/resources", "test3.json").getCanonicalPath();
                System.out.println("jsonPath=" + jsonPath);
                json = new String(Files.readAllBytes(Paths.get(jsonPath)));
                System.out.println("json string length=" + json.length());
                long startMilli = System.currentTimeMillis();
                JsonReader reader = new JsonReader(json, true);
                long endMilli = System.currentTimeMillis();
                System.out.println(" Load milli=" + (endMilli - startMilli) + " size=" + reader.data.base.size());
                System.out.println("--setup done--");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private long workArrayPlus() {
        ArrayListPlus<Integer> nums = null;
        int val = 0;
        for (int idx = 0; idx < SIZES.length; idx++) {
            nums = new ArrayListPlus<>(cache);
            int len = SIZES[idx]; // Math.round((float)Math.random() * 10) * 7 + 13;
            for (int tmpIdx = 0; tmpIdx < len; tmpIdx++) {
                nums.add(val++);
            }
        }
        // System.out.printf("swap test arr=%d\n", nums.size());
        return parser1Size = (long)nums.size();
    }

    private long workArrayStd()   {
        ArrayList<Integer> nums = null;
        int val = 0;
        for (int idx = 0; idx < SIZES.length; idx++) {
            nums = new ArrayList<>();
            int len = SIZES[idx]; // Math.round((float)Math.random() * 10) * 7 + 13;
            for (int tmpIdx = 0; tmpIdx < len; tmpIdx++) {
                nums.add(val++);
            }
        }
        // System.out.printf("normal test arr=%d\n", nums.size());
        return parser2Size = (long)nums.size();
    }

    // @Benchmark
    public void testArrayPlus(Blackhole blackhole) {
        try {
            blackhole.consume(workArrayPlus());
            // System.out.println("--Parse1 done, size=" + reader.data.base.size());
        } catch (Exception ex) {
            System.out.println("--Parse1a failed=" + ex.toString());
        }
    }

    // @Benchmark
    public void testArrayStd(Blackhole blackhole) {
        try {
            blackhole.consume(workArrayStd());
            // System.out.println("--Parse2 done, size=" + parser2Size);
        } catch (Exception ex) {
            System.out.println("--Parse2a failed=" + ex.toString());
        }
    }

    public static void main(String[] args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
        System.out.println("ArrayPlus  size=" + parser1Size);
        System.out.println("ArrayStd   size=" + parser2Size);
        System.out.println("--Bench test jsonParse done--");
    }
}
*/