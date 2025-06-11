/*
 * Copyright 2023 The Original Author or Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grulst.benchmark;

import grulst.HashGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Microbenchmark for HashGenerator performance testing.
 * To run the benchmark:
 * 1. Build the project with: mvn clean package
 * 2. Run the benchmark: java -jar target/benchmarks.jar
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class HashGeneratorBenchmark {

    private HashGenerator defaultGenerator;
    private HashGenerator customNodeIdGenerator;
    private long testValue;

    @Setup
    public void setup() {
        // Initialize generators
        defaultGenerator = HashGenerator.builder().build();
        customNodeIdGenerator = HashGenerator.builder().nodeId(42).build();
        testValue = 123456789L;
    }

    @Benchmark
    public void benchmarkStaticGenerateHash(Blackhole blackhole) {
        String hash = HashGenerator.generateHash();
        blackhole.consume(hash);
    }

    @Benchmark
    public void benchmarkDefaultGeneratorGenerate(Blackhole blackhole) {
        String hash = defaultGenerator.generate();
        blackhole.consume(hash);
    }

    @Benchmark
    public void benchmarkCustomNodeIdGeneratorGenerate(Blackhole blackhole) {
        String hash = customNodeIdGenerator.generate();
        blackhole.consume(hash);
    }

    /**
     * This benchmark tests the performance of creating a HashGenerator instance
     * using the builder pattern.
     */
    @Benchmark
    public void benchmarkBuilderCreation(Blackhole blackhole) {
        HashGenerator generator = HashGenerator.builder().nodeId(testValue).build();
        blackhole.consume(generator);
    }

    /**
     * Main method to run the benchmark from IDE or command line.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HashGeneratorBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
