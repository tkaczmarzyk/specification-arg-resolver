/**
 * Copyright 2014-2020 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static javax.persistence.criteria.JoinType.INNER;
import static javax.persistence.criteria.JoinType.LEFT;
import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases:
 * TC-1. interface with @JoinFetch spec
 * TC-2. interface extending two interfaces with @JoinFetch spec
 *
 * @author Jakub Radlica
 */
public class AnnotatedJoinFetchSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @JoinFetch spec
	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "orders", joinType = LEFT)
	@Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
	private interface OrderedItemNameFilter<T> extends Specification<T> {
	}

	@And(value = {
			@Spec(path = "gender", params = "genderIn", spec = In.class),
			@Spec(path = "lastName", params = "lastName", spec = In.class)
	})
	private interface LastNameGenderFilterExtendedByOrderedItemNameFilter extends OrderedItemNameFilter<Customer> {
	}

	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "badges", joinType = INNER)
	@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class)
	private interface BadgeFilter extends Specification<Customer> {
	}

	// TC-2. interface extending two interfaces with @JoinFetch spec
	private interface SpecExtendedByTwoOtherJoinFetchFilters extends LastNameGenderFilterExtendedByOrderedItemNameFilter, BadgeFilter {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @JoinFetch spec
		public void annotatedInterface(OrderedItemNameFilter spec) {
		}

		// TC-2. interface extending two interfaces with @JoinFetch spec
		public void getCustomersBySpecExtendedByTwoOtherJoinFetchFiltersExtendedByParamSimpleSpec(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) SpecExtendedByTwoOtherJoinFetchFilters spec) {
		}
	}

	@Test // TC-1. interface with @Disjunction spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", OrderedItemNameFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Item-123").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		QueryContext queryCtx = new WebRequestQueryContext(req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(OrderedItemNameFilter.class);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new JoinFetch<>(queryCtx, new String[]{ "orders" }, LEFT, true),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "o.itemName", "Item-123"))
				);
	}

	@Test // TC-2. interface extending two interfaces with @JoinFetch spec
	public void createsSpecFromEmptyFilterExtendingTwoInterfacesWithJoinFetchFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersBySpecExtendedByTwoOtherJoinFetchFiltersExtendedByParamSimpleSpec",
				SpecExtendedByTwoOtherJoinFetchFilters.class
		);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Pizza")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("genderIn", "MALE")
				.withParameterValues("badgeType", "Beef Eater")
				.withParameterValues("nickName", "Hom").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		QueryContext queryCtx = new WebRequestQueryContext(req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(SpecExtendedByTwoOtherJoinFetchFilters.class);

		Collection<Specification<Object>> innerSpecs = innerSpecs(resolved);

		Assertions.assertThat(innerSpecs)
				.hasSize(6)
				.containsOnly(
						new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{ "orders" }, LEFT,true),
						new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{ "badges" }, INNER, true),
						new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "b.badgeType", new String[]{ "Beef Eater" }, converter)),
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(new In<>(ctx.queryContext(), "gender", new String[]{ "MALE" }, converter)),
								new EmptyResultOnTypeMismatch<>(new In<>(ctx.queryContext(), "lastName", new String[]{ "Simpson" }, converter))
						),
						new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o.itemName", new String[]{ "Pizza" }, converter)),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}
}
