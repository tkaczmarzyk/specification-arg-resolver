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
import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.JoinType;
import java.util.Collection;

import static javax.persistence.criteria.JoinType.LEFT;
import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test cases:
 * TC-1. interface with @Join spec
 * TC-2. interface extending two interfaces with @Join spec
 *
 * @author Jakub Radlica
 */
public class AnnotatedJoinSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @Join spec
	@Join(path = "orders", alias = "o", type = LEFT)
	@Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
	private interface OrderedItemNameFilter<T> extends Specification<T> {
	}

	@And(value = {
			@Spec(path = "gender", params = "genderIn", spec = In.class),
			@Spec(path = "lastName", params = "lastName", spec = In.class)
	})
	private interface LastNameGenderFilterExtendedByOrderedItemNameFilter extends OrderedItemNameFilter<Customer> {
	}

	@Join(path = "badges", alias = "b")
	@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class)
	private interface BadgeFilter extends Specification<Customer> {
	}

	// TC-2. interface extending two interfaces with @Join spec
	private interface SpecExtendedByTwoOtherJoinSpecsFilter extends LastNameGenderFilterExtendedByOrderedItemNameFilter, BadgeFilter {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @Join spec
		public void annotatedInterface(OrderedItemNameFilter spec) {
		}

		// TC-2. interface extending two interfaces with @Join spec
		public void getCustomersBySpecExtendedByTwoOtherJoinSpecsFilterExtendedByParamSimpleSpec(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) SpecExtendedByTwoOtherJoinSpecsFilter spec) {
		}
	}

	@Test // TC-1. interface with @Join spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", OrderedItemNameFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Item-123").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(OrderedItemNameFilter.class);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", LEFT, true),
						new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o.itemName", new String[]{"Item-123"}, converter))
				);
	}

	@Test // TC-2. interface extending two interfaces with @Join spec
	public void createsSpecFromEmptyFilterExtendingTwoInterfacesWithJoinFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersBySpecExtendedByTwoOtherJoinSpecsFilterExtendedByParamSimpleSpec",
				SpecExtendedByTwoOtherJoinSpecsFilter.class
		);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Pizza")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("genderIn", "MALE")
				.withParameterValues("badgeType", "Beef Eater")
				.withParameterValues("nickName", "Hom").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(SpecExtendedByTwoOtherJoinSpecsFilter.class);

		Collection<Specification<Object>> innerSpecs = innerSpecs(resolved);

		Assertions.assertThat(innerSpecs)
				.hasSize(6)
				.containsOnly(
						new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "badges", "b", JoinType.INNER, true),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "b.badgeType", "Beef Eater")),
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(in(ctx, "gender", "MALE")),
								new EmptyResultOnTypeMismatch<>(in(ctx, "lastName", "Simpson"))
						),
						new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", LEFT, true),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "o.itemName", "Pizza")),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}
}
