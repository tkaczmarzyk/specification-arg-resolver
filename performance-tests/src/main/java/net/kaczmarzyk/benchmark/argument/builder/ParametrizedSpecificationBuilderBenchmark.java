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
 * Each benchmark is passing various amount of arguments (specified in field with {@code Param} annotation) and uses interface with 15 specifications.
 *
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
@State(Scope.Benchmark)
public class ParametrizedSpecificationBuilderBenchmark extends SpecificationBuilderBenchmarkBase {

	// If you want to add more arguments, then you have to add them to specification interfaces and parameters map in SpecificationBuilderBenchmarkBase
	@Param({"0", "1", "3", "6", "9", "12", "15"})
	private int argumentsAmount;

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithMultipleParams(Blackhole blackhole) {
		FifteenParamsSpecification specification = paramsSpecification(FifteenParamsSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithMultiplePathVars(Blackhole blackhole) {
		FifteenPathVarsSpecification specification = pathVarsSpecification(FifteenPathVarsSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithMultipleHeaders(Blackhole blackhole) {
		FifteenHeadersSpecification specification = headersSpecification(FifteenHeadersSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureBuildingSpecWithMultipleJsonPaths(Blackhole blackhole) {
		FifteenJsonPathsSpecification specification = jsonPathsSpecification(FifteenJsonPathsSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}
}
