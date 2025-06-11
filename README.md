# HashGenerator - Global Resilient Unique Lexi-Sort Token (GRULST)

A Java library for generating unique, sortable hash tokens.

## Features

- Generates unique hash tokens in the format `aaaaa-bbbb-cccc-dddd`
- Tokens are lexicographically sortable by timestamp
- Supports distributed systems with node ID
- Includes sequence counter for uniqueness within the same millisecond

## Performance Benchmarks

This project includes JMH (Java Microbenchmark Harness) benchmarks to measure the performance of the HashGenerator.

### Running the Benchmarks

There are multiple ways to run the benchmarks:

#### Using Maven and the JAR file

1. Build the project with Maven:
   ```
   mvn clean package
   ```

2. Run the benchmarks:
   ```
   java -jar target/benchmarks.jar
   ```

3. For specific benchmarks only:
   ```
   java -jar target/benchmarks.jar HashGeneratorBenchmark.benchmarkStaticGenerateHash
   ```

4. For more options:
   ```
   java -jar target/benchmarks.jar -h
   ```

#### Using the BenchmarkRunner class

For convenience, you can also run the benchmarks directly from your IDE using the `BenchmarkRunner` class:

1. Open the project in your IDE
2. Run the `grulst.benchmark.BenchmarkRunner` class
3. Results will be saved to `jmh-result.json` in the project root directory

### Benchmark Configuration

The benchmarks are configured with the following parameters:

- **Mode**: Average Time - measures the average time it takes to execute the benchmark method
- **Time Unit**: Microseconds
- **Warmup**: 3 iterations, 1 second each
- **Measurement**: 5 iterations, 1 second each
- **Fork**: 1 JVM fork, 1 warmup fork

### Available Benchmarks

- `benchmarkStaticGenerateHash`: Measures the performance of the static `generateHash()` method
- `benchmarkDefaultGeneratorGenerate`: Measures the performance of the `generate()` method with default settings
- `benchmarkCustomNodeIdGeneratorGenerate`: Measures the performance of the `generate()` method with a custom node ID
- `benchmarkBuilderCreation`: Measures the performance of creating a HashGenerator instance using the builder pattern

## Usage

```java
// Using the static method (default settings)
String hash = HashGenerator.generateHash();

// Using the builder with default settings
HashGenerator generator1 = HashGenerator.builder().build();
String hash1 = generator1.generate();

// Using the builder with custom node ID
HashGenerator generator2 = HashGenerator.builder().nodeId(42).build();
String hash2 = generator2.generate();
```

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.
