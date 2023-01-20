package net.kaczmarzyk.benchmark.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.kaczmarzyk.benchmark.specifications.SpecificationProvider.testSpecifications;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@State(Scope.Benchmark)
public class ToPredicateBenchmarkTest extends AbstractBenchmark {

	private final static Integer MEASUREMENT_ITERATIONS = 5;
	private final static Integer WARMUP_ITERATIONS = 5;

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
		"equalInstantSpec"
	})
	private String specName;

	@Setup(Level.Trial)
	public void initData() {
		System.out.println("Initializing test data!");
		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(Customer.class);
		root = criteriaQuery.from(Customer.class);
		specs = testSpecifications();
	}

	@Benchmark
	public void measureToPredicateSpecMethod(Blackhole blackhole) {
		blackhole.consume(specs.get(specName).toPredicate(root, criteriaQuery, criteriaBuilder));
	}

	@Override
	public Options executionOptions() {
		return new OptionsBuilder()
			.include("\\." + this.getClass().getSimpleName() + "\\.")
			.warmupIterations(WARMUP_ITERATIONS)
			.measurementIterations(MEASUREMENT_ITERATIONS)
			.forks(0)
			.threads(1)
			.shouldDoGC(true)
			.shouldFailOnError(true)
			.resultFormat(ResultFormatType.JSON)
			.shouldFailOnError(true)
			.mode(AverageTime)
			.timeUnit(NANOSECONDS)
			.build();
	}
}