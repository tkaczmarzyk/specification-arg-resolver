performance-tests
=================

A set of performance tests prepared for `specification-argument-resolver`.

Basic usage
-----------
There are two ways of executing performance tests:

### Building and running jar file of performance-tests ###
Building and executing `jar` file with all performance-tests. By default `jar` file name is `benchmark.jar` placed in `target` directory. Testing configuration is achieved with annotations over benchmark methods (e.g. warmup and measurement conditions, benchmark mode etc.).

Executing all tests with results saved in json format file (`-rf json`) with `jmh-result.json` name (`-rff`) failing on error (`-foe 1`).
```shell
java -jar target/benchmark.jar -rf json -rff jmh-result.json -foe 1
```

Executing tests from specific class:
```shell
java -jar target/benchmark.jar ClassNameToBeExecuted -rf json -rff jmh-result.json -foe 1
```

For additional command line configuration check:
```shell
java -jar target/benchmark.jar -h
```

### Running main method from Application class ###
Running `main` method from `Application` class - it will execute all tests from all classes (methods with `@Benchmark` annotation). It is possible to specify classes to be executed by specifying Options in `Application` main method:
```java
Options options = new OptionsBuilder()
        .include(CLASS_NAME_TO_EXECUTE.class.getSimpleName())
        .shouldFailOnError(true)
        .resultFormat(ResultFormatType.JSON)
        .build();

        new Runner(options).run();
```

Note: performance tests executed in IDE using main method and executing the same performance tests via `java -jar` command may return different results. It is recommended to run performance tests using `java -jar` command. Running tests via main method in IDE should be used only for development purposes.

