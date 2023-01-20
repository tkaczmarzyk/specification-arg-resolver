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

import net.kaczmarzyk.benchmark.model.Customer;
import net.kaczmarzyk.benchmark.model.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.kaczmarzyk.benchmark.model.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder.specification;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

/**
 * <p>Class measuring performance differences between native query and corresponding specification.</p>
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
@State(Scope.Benchmark)
public class SpecificationQueryExecutionBenchmarkTest extends AbstractBenchmark {

    private final static Integer MEASUREMENT_ITERATIONS = 5;
    private final static Integer WARMUP_ITERATIONS = 5;
    private final static Integer NUMBER_OF_HOMER_ENTRIES = 1;
    private final static Integer NUMBER_OF_MARGE_ENTRIES = 1;

    private static CustomerRepository customerRepo;

    private SimpleFirstNameSpecification simpleFirstNameSpec;

    @Autowired
    void setCustomerRepo(CustomerRepository customerRepo) {
        SpecificationQueryExecutionBenchmarkTest.customerRepo = customerRepo;
    }

    @Setup(Level.Trial)
    public void initData() {
        for (int i=0; i<NUMBER_OF_HOMER_ENTRIES; i++) {
            customerRepo.save(customer("Homer", "Simpson_" + i)
                    .build());
        }
        for (int i=0; i<NUMBER_OF_MARGE_ENTRIES; i++) {
            customerRepo.save(customer("Marge", "Simpson_" + i)
                    .build());
        }
        simpleFirstNameSpec = specification(SimpleFirstNameSpecification.class)
                .withParam("firstName", "Homer")
                .build();
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
        customerRepo.deleteAll();
    }

    @Benchmark
    public List<Customer> measureSpecWithParam() {
        return customerRepo.findAll(simpleFirstNameSpec);
    }

    @Benchmark
    public List<Customer> measureNativeQuery() {
        return customerRepo.findByCustomerName("Homer");
    }

    @Override
    public Options executionOptions() {
        return new OptionsBuilder()
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(WARMUP_ITERATIONS)
                .measurementIterations(MEASUREMENT_ITERATIONS)
                .forks(0)  // do not use forking or the benchmark methods will not see references stored within its class (e.g. injected CustomerRepository)
                .threads(1) // do not use multiple threads
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                .shouldFailOnError(true)
                .mode(AverageTime)
                .timeUnit(MILLISECONDS)
                .build();
    }

    @Spec(path="firstName", params = "firstName", spec=Equal.class)
    private interface SimpleFirstNameSpecification extends Specification<Customer> {
    }
}
