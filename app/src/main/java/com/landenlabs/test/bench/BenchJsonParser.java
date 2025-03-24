
package com.landenlabs.test.bench;

/*

Using custom ArrayListPlus with ArrayCache the json parser is faster
than standard parser.


Benchmark                            (N)  Mode  Cnt  Score   Error  Units
BenchJsonParser.testJsonDenParser  10000  avgt    2  5.743          ms/op
BenchJsonParser.testJsonStdParser  10000  avgt    2  6.926          ms/op
 */

/*
@BenchmarkMode(Mode.AverageTime)
// @BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 1, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 10000, timeUnit = TimeUnit.MILLISECONDS)
// @Warmup(iterations = 3)
// @Measurement(iterations = 20)

public class BenchJsonParser {

    volatile public static long parser1Size = 0;
    volatile public static long parser2Size = 0;
    private static String json;

    // @Param({"50000"})
    @Param({"100000"})
    private int N;

    @Setup
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // @Benchmark
    public void testJsonDenReader(Blackhole blackhole) {
        try {
            com.landenlabs.test.JsonDennis.JsonReader reader = new com.landenlabs.test.JsonDennis.JsonReader(json, true);
            blackhole.consume(reader.data.base.size());
            parser1Size = reader.data.base.size();
            // System.out.println("--Parse1 done, size=" + reader.data.base.size());
        } catch (Exception ex) {
            System.out.println("--testJsonDenReader failed=" + ex.toString());
        }
    }
    @Benchmark
    public void testJsonDenParser(Blackhole blackhole) {
        try {
            SunVectorData vectorData = com.landenlabs.test.JsonDennis.SunVectorData.parse(json);
            blackhole.consume(vectorData);
            // System.out.println("--Parse1 done, size=" + reader.data.base.size());
        } catch (Exception ex) {
            System.out.println("--testJsonDenParser failed=" + ex.toString());
        }
    }

    // @Benchmark
    public void testJsonStdReader(Blackhole blackhole) {
        try {
            com.landenlabs.test.JsonOrg.JsonReader reader = new com.landenlabs.test.JsonOrg.JsonReader(json, true);
            blackhole.consume(reader);
            parser2Size = reader.jsonObject.length();
            // System.out.println("--Parse2 done, size=" + parser2Size);
        } catch (Exception ex) {
            System.out.println("--testJsonStdReader failed=" + ex.toString());
        }
    }

    @Benchmark
    public void testJsonStdParser(Blackhole blackhole) {
        try {
            com.landenlabs.test.JsonOrg.SunVectorData vectorData = com.landenlabs.test.JsonOrg.SunVectorData.parse(json);
            blackhole.consume(vectorData);
            // System.out.println("--Parse2 done, size=" + parser2Size);
        } catch (Exception ex) {
            System.out.println("--testJsonStdParser failed=" + ex.toString());
        }
    }

    public static void main(String[] args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
        System.out.println("testJsonDenParser size=" + parser1Size);
        System.out.println("testJsonStdParser size=" + parser2Size);
        System.out.println("--Bench test json Parse done--");
    }
}
*/