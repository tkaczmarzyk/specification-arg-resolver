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
import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static javax.persistence.criteria.JoinType.*;
import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
/**
 * Test cases:
 * TC-1. interface with repeated @JoinFetch spec
 * TC-2. interface extending interface with repeated @JoinFetch spec and extending interface with single @JoinFetch
 * TC-3. interface extending two interfaces with repeated @JoinFetch
 *
 * @author Jakub Radlica
 */
public class AnnotatedRepeatedJoinFetchSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with repeated @JoinFetch spec
	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "orders", joinType = LEFT)
	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "badges", joinType = INNER)
	private interface OrderedItemNameBadgeFilter<T> extends Specification<T> {
	}

	// TC-2. interface extending interface with repeated @JoinFetch spec and extending interface with single @JoinFetch
	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "orders2", joinType = LEFT)
	@Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
	private interface OrderedItemNameFilter<T> extends Specification<T> {

	}

	private interface SpecExtendingTwoInterfaces<T> extends
			OrderedItemNameBadgeFilter<T>, OrderedItemNameFilter<T> {

	}

	// TC-3. interface extending two interfaces with repeated @JoinFetch
	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "orders2", joinType = RIGHT, distinct = false)
	@net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch(paths = "badges2", joinType = RIGHT)
	private interface OrderedItemNameBadgeFilter2<T> extends Specification<T> {
	}

	private interface SpecExtendingTwoInterfacesWithRepeatedJoinFetch<T> extends OrderedItemNameBadgeFilter<T>, OrderedItemNameBadgeFilter2<T> {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with repeated @JoinFetch spec
		public void annotatedInterface(OrderedItemNameBadgeFilter<Customer> spec) {
		}

		// TC-2. interface extending interface with repeated @JoinFetch spec and extending interface with single @JoinFetch
		public void getCustomersBySpecExtendingInterfaceWithRepeatedJoinFetchAndExtendingInterfaceWithSingleJoinFetch(SpecExtendingTwoInterfaces<Customer> spec) {
		}

		// TC-3. interface extending two interfaces with repeated @JoinFetch
		public void getCustomersBySpecExtendingTwoInterfacesWithRepeatedJoinFetch(SpecExtendingTwoInterfacesWithRepeatedJoinFetch<Customer> spec) {

		}
	}

	@Test // TC-1. interface with repeated @JoinFetch spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", OrderedItemNameBadgeFilter.class);

		NativeWebRequest req = nativeWebRequest().build();
		QueryContext queryCtx = new WebRequestQueryContext(req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new JoinFetch<>(queryCtx, new String[]{ "orders" }, LEFT, true),
						new JoinFetch<>(queryCtx, new String[]{ "badges" }, INNER, true)
				);
	}

	@Test // TC-2. interface extending interface with repeated @JoinFetch spec and extending interface with single @JoinFetch
	public void createsSpecFromInterfaceExtendingTwoInterfaces() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersBySpecExtendingInterfaceWithRepeatedJoinFetchAndExtendingInterfaceWithSingleJoinFetch",
				SpecExtendingTwoInterfaces.class
		);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Duff Beer")
				.build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		QueryContext queryCtx = new WebRequestQueryContext(req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(innerSpecs(resolved))
				.hasSize(3)
				.containsExactlyInAnyOrder(
						new Conjunction<>(
								new JoinFetch<>(queryCtx, new String[]{ "orders" }, LEFT, true),
								new JoinFetch<>(queryCtx, new String[]{ "badges" }, INNER, true)
						),
						new JoinFetch<>(queryCtx, new String[]{ "orders2" }, LEFT, true),
						new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o.itemName", new String[]{ "Duff Beer" }, defaultConverter))
				);
	}

	@Test // TC-3. interface extending two interfaces with repeated @JoinFetch
	public void createsSpecFromInterfaceExtendingTwoInterfacesWithRepeatedJoinFetch() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersBySpecExtendingTwoInterfacesWithRepeatedJoinFetch",
				SpecExtendingTwoInterfacesWithRepeatedJoinFetch.class
		);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Duff Beer")
				.build();
		QueryContext queryCtx = new WebRequestQueryContext(req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new Conjunction<>(
								new JoinFetch<>(queryCtx, new String[]{ "orders" }, LEFT, true),
								new JoinFetch<>(queryCtx, new String[]{ "badges" }, INNER, true)
						),
						new Conjunction<>(
								new JoinFetch<>(queryCtx, new String[]{ "orders2" }, RIGHT, false),
								new JoinFetch<>(queryCtx, new String[]{ "badges2" }, RIGHT, true)
						)
				);
	}
}
