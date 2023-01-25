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
package net.kaczmarzyk.benchmark.execution;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.kaczmarzyk.benchmark.execution.SpecificationProvider.JOIN_SPECIFICATIONS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
@State(Scope.Benchmark)
public class JoinSpecificationBenchmark extends ToPredicateExecutionBenchmarkBase {

	@Param({
		"joinSpec",
		"joinFetchSpec"
	})
	private String specName;

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureToPredicateSpecMethod(Blackhole blackhole) {
		blackhole.consume(JOIN_SPECIFICATIONS.get(specName).toPredicate(root, criteriaQuery, criteriaBuilder));
	}
}
