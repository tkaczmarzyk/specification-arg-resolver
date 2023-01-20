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
import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>Base class for benchmark tests requiring application components like services or repositories (e.g. CustomerRepository).</p>
 *
 * <p>Running {@code executeJmhRunner} test, methods with {@code @Benchmark} annotation from subclasses are executed.</p>
 *
 * <p>Jmh runner execution options can be specified by overriding {@code executionOptions} method.</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { Application.class })
abstract public class AbstractBenchmark {

	protected static EntityManager entityManager;

	/**
	 * <p>Entity manager has to be registered as a bean in the application to be properly injected.</p>
	 */
	@Autowired
	public void setEntityManager(EntityManager entityManager) {
		AbstractBenchmark.entityManager = entityManager;
	}

	/**
	 * <p>Any benchmark, by extending this class, inherits this single @Test method for JUnit to run.</p>
	 */
	@Test
	public void executeJmhRunner() throws RunnerException {
		new Runner(executionOptions()).run();
	}

	public abstract Options executionOptions();
}
