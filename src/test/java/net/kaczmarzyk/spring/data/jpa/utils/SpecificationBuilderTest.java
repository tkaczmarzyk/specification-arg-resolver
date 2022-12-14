/**
 * Copyright 2014-2022 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;


import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.ProcessingContext;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationFactory;
import net.kaczmarzyk.spring.data.jpa.web.StandaloneProcessingContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder.specification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Jakub Radlica
 * @author Kacper Leśniak (Tratif sp. z o.o.)
 */
public class SpecificationBuilderTest extends IntegrationTestBase {

	private Converter converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null);

	@And({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "lastName", path = "lastName", spec = Equal.class)
	})
	public interface CustomSpecification extends Specification<Customer> {
	}

	@Join(path = "orders", alias = "o")
	@Join(path = "o.tags", alias = "t", type = JoinType.INNER)
	@Or({
			@Spec(path = "o.itemName", pathVars = "orderIn", spec = In.class),
			@Spec(path = "t.name", headers = "tag", spec = Equal.class)
	})
	public interface CustomSpecificationWithPathVar extends Specification<Customer> {
	}

	@Join(path = "orders", alias = "o")
	@Join(path = "o.tags", alias = "t", type = JoinType.INNER)
	@Or({
			@Spec(path = "o.itemName", params = "orderIn", spec = In.class),
			@Spec(path = "t.name", headers = "tag", spec = Equal.class)
	})
	public interface CustomSpecificationWithParam extends Specification<Customer> {
	}

	@Join(path = "orders", alias = "o")
	@Join(path = "o.tags", alias = "t", type = JoinType.INNER)
	@Or({
			@Spec(path = "o.itemName", headers = "orderIn", spec = In.class),
			@Spec(path = "t.name", params = "tag", spec = Equal.class)
	})
	public interface CustomSpecificationWithHeader extends Specification<Customer> {
	}

	@Join(path = "orders", alias = "o")
	@Join(path = "o.tags", alias = "t", type = JoinType.INNER)
	@Or({
			@Spec(path = "o.itemName", jsonPaths = "orderIn", spec = In.class),
			@Spec(path = "t.name", jsonPaths = "tag", spec = Equal.class)
	})
	public interface CustomSpecificationWithJsonPath extends Specification<Customer> {
	}

	@Test
	public void shouldCreateSpecificationUsingBuilder() {
		Map<String, String[]> params = new HashMap<>();
		params.put("gender", new String[]{"MALE"});
		params.put("lastName", new String[]{"Simpson"});

		StandaloneProcessingContext ctx = new StandaloneProcessingContext(CustomSpecification.class, null, null, params, null, null);

		Specification<Customer> spec = specification(CustomSpecification.class)
				.withParam("gender", "MALE")
				.withParam("lastName", "Simpson")
				.build();

		assertThat(spec)
				.isInstanceOf(CustomSpecification.class);

		assertThat(innerSpecs(spec))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
				);

	}

	@Test
	public void shouldCreateSpecificationDependingOnPathVar() {
		customer("Homer", "Simpson")
				.orders("Pizza")
				.build(em);

		customer("Marge", "Simpson")
				.orders("Cake")
				.build(em);

		Specification<Customer> spec = specification(CustomSpecificationWithPathVar.class)
				.withPathVar("orderIn", "Pizza")
				.build();

		List<Customer> customers = customerRepo.findAll(spec);

		assertThat(customers.size()).isEqualTo(1);
		assertThat(customers.get(0).getFirstName()).isEqualTo("Homer");
	}

	@Test
	public void shouldCreateSpecificationDependingOnParam() {
		customer("Marge", "Simpson")
				.orders("Cake")
				.build(em);

		customer("Bart", "Simpson")
				.orders("Bread")
				.build(em);

		Specification<Customer> spec = specification(CustomSpecificationWithParam.class)
				.withParam("orderIn", "Cake")
				.build();

		List<Customer> customers = customerRepo.findAll(spec);

		assertThat(customers.size()).isEqualTo(1);
		assertThat(customers.get(0).getFirstName()).isEqualTo("Marge");
	}

	@Test
	public void shouldCreateSpecificationDependingOnHeader() {
		customer("Bart", "Simpson")
				.orders("Bread")
				.build(em);

		customer("Lisa", "Simpson")
				.orders("Butter")
				.build(em);

		Specification<Customer> spec = specification(CustomSpecificationWithHeader.class)
				.withHeader("orderIn", "Bread")
				.build();

		List<Customer> customers = customerRepo.findAll(spec);

		assertThat(customers.size()).isEqualTo(1);
		assertThat(customers.get(0).getFirstName()).isEqualTo("Bart");
	}

	@Test
	public void shouldCreateSpecificationDependingOnJsonPath() {
		customer("Marge", "Simpson")
				.orders("Cake")
				.build(em);

		customer("Bart", "Simpson")
				.orders("Bread")
				.build(em);

		Specification<Customer> spec = specification(CustomSpecificationWithJsonPath.class)
				.withJsonBodyParam("orderIn", "Cake")
				.build();

		List<Customer> customers = customerRepo.findAll(spec);

		assertThat(customers.size())
				.isEqualTo(1);
		assertThat(customers.get(0).getFirstName())
				.isEqualTo("Marge");
	}

	@Test
	public void shouldCreateSpecificationDependingOnFallbackValueWhenHeaderValueIsMissing() {
		customer("Bart", "Simpson")
				.orders("Bread")
				.build(em);

		customer("Lisa", "Simpson")
				.orders("Eggs")
				.build(em);

		Specification<Customer> spec = specification(CustomSpecificationWithHeader.class)
				.withArg("orderIn", "Eggs")
				.build();

		List<Customer> customers = customerRepo.findAll(spec);

		assertThat(customers.size())
				.isEqualTo(1);
		assertThat(customers.get(0).getFirstName())
				.isEqualTo("Lisa");
	}

	@Test
	public void shouldInvokeMethodToGenerateSpecificationDependingOnStandaloneProcessingContext() {
		SpecificationFactory specificationFactory = mock(SpecificationFactory.class);

		Specification<Customer> spec = specification(CustomSpecificationWithHeader.class)
				.withSpecificationFactory(specificationFactory)
				.withHeader("orderIn", "Pizza")
				.build();

		verify(specificationFactory, times(1)).createSpecificationDependingOn(any(StandaloneProcessingContext.class));
	}

	private Collection<Specification<Object>> innerSpecs(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Conjunction<Object> resolvedConjunction =
				ReflectionUtils.get(Proxy.getInvocationHandler(resolvedSpec), "arg$1");

		return ReflectionUtils.get(resolvedConjunction, "innerSpecs");
	}

	private Equal<Object> equal(ProcessingContext ctx, String path, String value) {
		return new Equal<>(ctx.queryContext(), path, new String[]{value}, converter);
	}
}
