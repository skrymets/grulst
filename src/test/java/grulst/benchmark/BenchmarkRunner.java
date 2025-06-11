/*
 * Copyright 2025 The Original Author or Authors
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

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * A simple runner for executing JMH benchmarks.
 * <p>
 * This class provides a convenient way to run the benchmarks from an IDE or command line
 * without having to use the jar file directly.
 */
public class BenchmarkRunner {

    /**
     * Main method to run all benchmarks in the project.
     *
     * @param args Command line arguments (not used)
     * @throws RunnerException If there's an error running the benchmarks
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // Include all benchmark classes in the grulst.benchmark package
                .include("grulst.benchmark.*")
                // Use 1 fork for consistency
                .forks(1)
                // Output results to a file
                .result("jmh-result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();

        System.out.println("Benchmarks complete. Results saved to jmh-result.json");
    }
}
