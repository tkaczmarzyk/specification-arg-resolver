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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
@State(Scope.Benchmark)
public class ToPredicateExecutionBenchmark {

	private ConfigurableApplicationContext applicationContext;
	protected CriteriaBuilder criteriaBuilder;
	protected CriteriaQuery<Customer> criteriaQuery;
	protected Root<Customer> root;

	@Setup(Level.Trial)
	public void setupTestComponentsAndData() {
		applicationContext = new AnnotationConfigApplicationContext(Application.class);
		EntityManager entityManager = applicationContext.getBean(EntityManager.class);

		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(Customer.class);
		root = criteriaQuery.from(Customer.class);
	}

	@TearDown(Level.Trial)
	public void closeApplicationContext() {
		applicationContext.close();
	}

}