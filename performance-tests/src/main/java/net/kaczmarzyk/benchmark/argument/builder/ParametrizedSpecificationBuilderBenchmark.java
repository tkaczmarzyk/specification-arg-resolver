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

import static net.kaczmarzyk.spring.data.jpa.web.annotation.MissingPathVarPolicy.IGNORE;

/**
 * This class measures the average time of building specification using {@code SpecificationBuilder} for all types of passing arguments (params, pathVars, headers, jsonPaths).
 * Each benchmark is passing various amount of arguments (specified in field with {@code Param} annotation) and uses interface with 15 specifications.
 *
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
@State(Scope.Benchmark)
public class ParametrizedSpecificationBuilderBenchmark extends SpecificationBuilderBenchmarkBase {

	@Param({"0", "1", "3", "6", "9", "12", "15"})
	private int argumentsAmount;

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithMultipleParams(Blackhole blackhole) {
		MultipleParamsSpecification specification = paramsSpecification(MultipleParamsSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithMultiplePathVars(Blackhole blackhole) {
		MultiplePathVarsSpecification specification = pathVarsSpecification(MultiplePathVarsSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithMultipleHeaders(Blackhole blackhole) {
		MultipleHeadersSpecification specification = headersSpecification(MultipleHeadersSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecWithMultipleJsonPaths(Blackhole blackhole) {
		MultipleJsonPathsSpecification specification = jsonPathsSpecification(MultipleJsonPathsSpecification.class, argumentsAmount);
		blackhole.consume(specification);
	}

	@Or({
		@Spec(path = "lastName", params = "lastName", spec = Equal.class),
		@Spec(path = "firstName", params = "firstName", spec = Equal.class),
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "gender", params = "gender", spec = Equal.class),
		@Spec(path = "hairColor", params = "hairColor", spec = Equal.class),
		@Spec(path = "iris", params = "iris", spec = Equal.class),
		@Spec(path = "height", params = "height", spec = Equal.class),
		@Spec(path = "race", params = "race", spec = Equal.class),
		@Spec(path = "motherName", params = "motherName", spec = Equal.class),
		@Spec(path = "fatherName", params = "fatherName", spec = Equal.class),
		@Spec(path = "vip", params = "vip", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
		@Spec(path = "religion", params = "religion", spec = Equal.class),
		@Spec(path = "state", params = "state", spec = Equal.class),
	})
	private interface MultipleParamsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", pathVars = "lastName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "firstName", pathVars = "firstName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "age", pathVars = "age", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "gender", pathVars = "gender", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "hairColor", pathVars = "hairColor", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "iris", pathVars = "iris", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "height", pathVars = "height", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "race", pathVars = "race", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "motherName", pathVars = "motherName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "fatherName", pathVars = "fatherName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "vip", pathVars = "vip", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "city", pathVars = "city", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "criminalPast", pathVars = "criminalPast", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "religion", pathVars = "religion", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "state", pathVars = "state", spec = Equal.class, missingPathVarPolicy = IGNORE),
	})
	private interface MultiplePathVarsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", headers = "lastName", spec = Equal.class),
		@Spec(path = "firstName", headers = "firstName", spec = Equal.class),
		@Spec(path = "age", headers = "age", spec = Equal.class),
		@Spec(path = "gender", headers = "gender", spec = Equal.class),
		@Spec(path = "hairColor", headers = "hairColor", spec = Equal.class),
		@Spec(path = "iris", headers = "iris", spec = Equal.class),
		@Spec(path = "height", headers = "height", spec = Equal.class),
		@Spec(path = "race", headers = "race", spec = Equal.class),
		@Spec(path = "motherName", headers = "motherName", spec = Equal.class),
		@Spec(path = "fatherName", headers = "fatherName", spec = Equal.class),
		@Spec(path = "vip", headers = "vip", spec = Equal.class),
		@Spec(path = "city", headers = "city", spec = Equal.class),
		@Spec(path = "criminalPast", headers = "criminalPast", spec = Equal.class),
		@Spec(path = "religion", headers = "religion", spec = Equal.class),
		@Spec(path = "state", headers = "state", spec = Equal.class),
	})
	private interface MultipleHeadersSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", jsonPaths = "lastName", spec = Equal.class),
		@Spec(path = "firstName", jsonPaths = "firstName", spec = Equal.class),
		@Spec(path = "age", jsonPaths = "age", spec = Equal.class),
		@Spec(path = "gender", jsonPaths = "gender", spec = Equal.class),
		@Spec(path = "hairColor", jsonPaths = "hairColor", spec = Equal.class),
		@Spec(path = "iris", jsonPaths = "iris", spec = Equal.class),
		@Spec(path = "height", jsonPaths = "height", spec = Equal.class),
		@Spec(path = "race", jsonPaths = "race", spec = Equal.class),
		@Spec(path = "motherName", jsonPaths = "motherName", spec = Equal.class),
		@Spec(path = "fatherName", jsonPaths = "fatherName", spec = Equal.class),
		@Spec(path = "vip", jsonPaths = "vip", spec = Equal.class),
		@Spec(path = "city", jsonPaths = "city", spec = Equal.class),
		@Spec(path = "criminalPast", jsonPaths = "criminalPast", spec = Equal.class),
		@Spec(path = "religion", jsonPaths = "religion", spec = Equal.class),
		@Spec(path = "state", jsonPaths = "state", spec = Equal.class),
	})
	private interface MultipleJsonPathsSpecification extends Specification<Object> {
	}
}
