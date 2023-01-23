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

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import net.kaczmarzyk.benchmark.Application;
import net.kaczmarzyk.benchmark.model.Customer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.kaczmarzyk.benchmark.execution.SpecificationProvider.testSpecifications;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

/**
 * <p>This class measures execution of `toPredicate` methods for various specifications.</p>
 *
 * <p>In {@code specName} {@code @Param} annotation the names of specifications to execute should be stored. The names have to have corresponding specification entry in {@code specs} map.</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
@State(Scope.Benchmark)
public class ToPredicateExecutionBenchmarkTest {

	private ConfigurableApplicationContext applicationContext;
	private EntityManager entityManager;
	private CriteriaBuilder criteriaBuilder;
	private CriteriaQuery<Customer> criteriaQuery;
	private Root<Customer> root;

	private Map<String, Specification<Customer>> specs;

	@Param({
			"equalStringSpec",
			"equalLocalDateTimeSpec",
			"equalLocalDateSpec",
			"equalDateSpec",
			"equalCalendarSpec",
			"equalOffsetDateTimeSpec",
			"equalInstantSpec",
			"equalTimestampSpec",
			"equalEnumSpec",
			"equalBooleanPrimitiveTypeSpec",
			"equalBooleanObjectSpec",
			"equalIntPrimitiveTypeSpec",
			"equalIntegerObjectSpec",
			"equalLongPrimitiveTypeSpec",
			"equalLongObjectSpec",
			"equalFloatPrimitiveTypeSpec",
			"equalDoubleObjectSpec",
			"equalBigDecimalSpec",
			"equalUUIDSpec",
			"equalCharPrimitiveTypeSpec",
			"equalCharacterObjectSpec"
	})
	private String specName;

	@Setup(Level.Trial)
	public void setupTestComponentsAndData() {
		applicationContext = new AnnotationConfigApplicationContext(Application.class);
		entityManager = applicationContext.getBean(EntityManager.class);

		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(Customer.class);
		root = criteriaQuery.from(Customer.class);
		specs = testSpecifications();
	}

	@TearDown(Level.Trial)
	public void closeApplicationContext() {
		applicationContext.close();
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 50, timeUnit = MILLISECONDS, iterations = 10)
	@Measurement(time = 50, timeUnit = MILLISECONDS, iterations = 10)
	@Fork(2)
	public void measureToPredicateSpecMethod(Blackhole blackhole) {
		blackhole.consume(specs.get(specName).toPredicate(root, criteriaQuery, criteriaBuilder));
	}
}