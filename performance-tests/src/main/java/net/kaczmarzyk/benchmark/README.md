performance-tests
=================

A set of performance tests prepared for `specification-argument-resolver`.

Basic usage
-----------
There are two ways of executing performance tests:

### Running main method from Application class ###
Running `main` method from `Application` class - it will execute all tests from all classes (methods with `@Benchmark` annotation). Testing configuration is achieved with annotations over benchmark methods (e.g. warmup and measurement conditions, benchmark mode etc.). It is possible to specify classes to be executed by specifying additional `include` option in `OptionsBuilder` in main method from `Application` class:
```java
Options options = new OptionsBuilder()
        .include(CLASS_NAME_TO_EXECUTE.class.getSimpleName())
        .shouldFailOnError(true)
        .resultFormat(ResultFormatType.JSON)
        .build();
```
### Building and running jar file of performance-tests ###
It is possible to build and execute `jar` file with all performance-tests. By default `jar` file name is `benchmark.jar` placed in `target` directory.

Executing all tests:
```shell
java -jar target/benchmark.jar
```


