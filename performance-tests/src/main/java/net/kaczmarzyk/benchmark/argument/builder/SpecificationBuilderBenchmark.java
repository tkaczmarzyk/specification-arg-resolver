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

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.data.jpa.domain.Specification;

import java.util.concurrent.TimeUnit;

/**
 * This class measures time of building specification using {@code SpecificationBuilder} for all types of passing arguments (params, pathVars, headers, jsonPaths).
 * Each benchmark is passing three arguments and uses interface with exact three specifications.
 *
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationBuilderBenchmark extends SpecificationBuilderBenchmarkBase {

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithThreeParams(Blackhole blackhole) {
		ThreeParamsSpecification result = paramsSpecification(ThreeParamsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithThreePathVars(Blackhole blackhole) {
		ThreePathVarsSpecification result = pathVarsSpecification(ThreePathVarsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithThreeHeaders(Blackhole blackhole) {
		ThreeHeadersSpecification result = headersSpecification(ThreeHeadersSpecification.class, 3);

		blackhole.consume(result);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithThreeJsonPaths(Blackhole blackhole) {
		ThreeJsonPathsSpecification result = jsonPathsSpecification(ThreeJsonPathsSpecification.class, 3);

		blackhole.consume(result);
	}

	@Or({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
	})
	private interface ThreeParamsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", pathVars = "age", spec = Equal.class),
		@Spec(path = "city", pathVars = "city", spec = Equal.class),
		@Spec(path = "criminalPast", pathVars = "criminalPast", spec = Equal.class),
	})
	private interface ThreePathVarsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", headers = "age", spec = Equal.class),
		@Spec(path = "city", headers = "city", spec = Equal.class),
		@Spec(path = "criminalPast", headers = "criminalPast", spec = Equal.class),
	})
	private interface ThreeHeadersSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", jsonPaths = "age", spec = Equal.class),
		@Spec(path = "city", jsonPaths = "city", spec = Equal.class),
		@Spec(path = "criminalPast", jsonPaths = "criminalPast", spec = Equal.class),
	})
	private interface ThreeJsonPathsSpecification extends Specification<Object> {
	}
}
