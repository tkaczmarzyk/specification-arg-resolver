/**
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.benchmark.argument.builder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

/**
 * This class measures the average time of building specification using {@code SpecificationBuilder} for all types of passing arguments (params, pathVars, headers, jsonPaths).
 * Each benchmark is passing three arguments and uses interface with three or fifteen specifications.
 *
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationBuilderBenchmark extends SpecificationBuilderBenchmarkBase {

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfThreeAvailableParams(Blackhole blackhole) {
		ThreeParamsSpecification result = paramsSpecification(ThreeParamsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfThreeAvailablePathVars(Blackhole blackhole) {
		ThreePathVarsSpecification result = pathVarsSpecification(ThreePathVarsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfThreeAvailableHeaders(Blackhole blackhole) {
		ThreeHeadersSpecification result = headersSpecification(ThreeHeadersSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfThreeAvailableJsonPaths(Blackhole blackhole) {
		ThreeJsonPathsSpecification result = jsonPathsSpecification(ThreeJsonPathsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfFifteenAvailableParams(Blackhole blackhole) {
		FifteenParamsSpecification result = paramsSpecification(FifteenParamsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfFifteenAvailablePathVars(Blackhole blackhole) {
		FifteenPathVarsSpecification result = pathVarsSpecification(FifteenPathVarsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfFifteenAvailableHeaders(Blackhole blackhole) {
		FifteenHeadersSpecification result = headersSpecification(FifteenHeadersSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithThreeOfFifteenAvailableJsonPaths(Blackhole blackhole) {
		FifteenJsonPathsSpecification result = jsonPathsSpecification(FifteenJsonPathsSpecification.class, 3);

		blackhole.consume(result);
	}
}
