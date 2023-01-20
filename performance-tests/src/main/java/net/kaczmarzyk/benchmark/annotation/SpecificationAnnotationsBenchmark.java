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
package net.kaczmarzyk.benchmark.annotation;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.data.jpa.domain.Specification;

import java.util.concurrent.TimeUnit;

import static net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder.specification;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationAnnotationsBenchmark {

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecificationForSpecAnnotation(Blackhole blackhole) {
		SpecAnnotationSpecification specification = specification(SpecAnnotationSpecification.class)
			.withParam("age", "19")
			.build();

		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecificationForAndAnnotation(Blackhole blackhole) {
		AndAnnotationSpecification specification = specification(AndAnnotationSpecification.class)
			.withParam("age", "19")
			.withParam("city", "Springfield")
			.withParam("criminalPast", "false")
			.build();

		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecificationForOrAnnotation(Blackhole blackhole) {
		OrAnnotationSpecification specification = specification(OrAnnotationSpecification.class)
			.withParam("age", "19")
			.withParam("city", "Springfield")
			.withParam("criminalPast", "false")
			.build();

		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecificationForConjunctionAnnotation(Blackhole blackhole) {
		ConjunctionAnnotationSpecification specification = specification(ConjunctionAnnotationSpecification.class)
			.withParam("age", "19")
			.withParam("city", "Springfield")
			.withParam("criminalPast", "false")
			.build();

		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@Warmup(time = 500, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = TimeUnit.MILLISECONDS, iterations = 10)
	@Fork(5)
	public void measureBuildingSpecificationForDisjunctionAnnotation(Blackhole blackhole) {
		DisjunctionAnnotationSpecification specification = specification(DisjunctionAnnotationSpecification.class)
			.withParam("age", "19")
			.withParam("city", "Springfield")
			.withParam("criminalPast", "false")
			.build();

		blackhole.consume(specification);
	}

	@Spec(path = "age", params = "age", spec = Equal.class)
	private interface SpecAnnotationSpecification extends Specification<Object> {
	}

	@And({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
	})
	private interface AndAnnotationSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
	})
	private interface OrAnnotationSpecification extends Specification<Object> {
	}

	@Conjunction(value = @Or({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
	}), and = @Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class))
	private interface ConjunctionAnnotationSpecification extends Specification<Object> {
	}

	@Disjunction(value = @And({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
	}), or = @Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class))
	private interface DisjunctionAnnotationSpecification extends Specification<Object> {
	}

}
