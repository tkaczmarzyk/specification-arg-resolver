package net.kaczmarzyk.benchmark.specifications;

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

	public abstract Options executionOptions();

	/**
	 * <p>Any benchmark, by extending this class, inherits this single @Test method for JUnit to run.</p>
	 */
	@Test
	public void executeJmhRunner() throws RunnerException {
		new Runner(executionOptions()).run();
	}
}
