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

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
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
 * TC-1. interface with repeated @Join spec
 * TC-2. interface extending interface with repeated @Join spec and extending interface with single @Join
 * TC-3. interface extending two interfaces with repeated @Join
 *
 * @author Jakub Radlica
 */
public class AnnotatedRepeatedJoinSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with repeated @Join spec
	@Join(path = "orders", alias = "o", type = LEFT)
	@Join(path = "badges", alias = "b")
	@And({
			@Spec(path = "o.itemName", params = "itemName", spec = Equal.class),
			@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class)
	})
	private interface OrderedItemNameBadgeFilter<T> extends Specification<T> {
	}

	// TC-2. interface extending interface with repeated @Join spec and extending interface with single @Join
	@Join(path = "discounts", alias = "d", type = RIGHT)
	@Spec(path = "d.discountType", params = "discountType", spec = Equal.class)
	private interface SpecWithSingleJoin<T> extends Specification<T> {
	}

	private interface SpecExtendingTwoInterfaces<T> extends OrderedItemNameBadgeFilter<T>, SpecWithSingleJoin<T> {
	}

	// TC-3. interface extending two interfaces with repeated @Join
	@Join(path = "orders2", alias = "o2", type = LEFT, distinct = false)
	@Join(path = "badges2", alias = "b2", type = RIGHT)
	@And({
			@Spec(path = "o2.itemName2", params = "itemName2", spec = Equal.class),
			@Spec(path = "b2.badgeType2", params = "badgeType2", spec = Equal.class)
	})
	private interface OrderedItemNameBadgeFilter2<T> extends Specification<T> {
	}

	private interface SpecExtendingTwoInterfacesWithRepeatedJoin<T> extends OrderedItemNameBadgeFilter<T>, OrderedItemNameBadgeFilter2<T> {

	}


	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with repeated @Join spec
		public void annotatedInterface(OrderedItemNameBadgeFilter<Object> spec) {
		}

		// TC-2. interface extending interface with repeated @Join spec and extending interface with single @Join
		public void specExtendingTwoInterfaces(SpecExtendingTwoInterfaces<Object> spec) {
		}

		// TC-3. interface extending two interfaces with repeated @Join
		public void specExtendingTwoInterfacesWithRepeatedJoin(SpecExtendingTwoInterfacesWithRepeatedJoin<Object> spec) {
		}

	}

	@Test // TC-1. interface with repeated @Join spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", OrderedItemNameBadgeFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Duff Beer")
				.withParameterValues("badgeType", "Hard Drinker")
				.build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o.itemName", new String[]{ "Duff Beer" }, defaultConverter)),
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "b.badgeType", new String[]{ "Hard Drinker" }, defaultConverter))
						),
						new Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", LEFT, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "badges", "b", INNER, true)
						)
				);
	}

	@Test // TC-2. interface extending interface with repeated @Join spec and extending interface with single @Join
	public void createsSpecFromInterfaceExtendingInterfaceWithRepeatedJoinAnnotationAndExtendingInterfaceWithSingleJoinAnnotation() throws Exception {
		MethodParameter param = methodParameter("specExtendingTwoInterfaces", SpecExtendingTwoInterfaces.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Duff Beer")
				.withParameterValues("badgeType", "Hard Drinker")
				.withParameterValues("discountType", "Gold customer")
				.build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(innerSpecs(resolved))
				.hasSize(4)
				.containsExactlyInAnyOrder(
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o.itemName", new String[]{ "Duff Beer" }, defaultConverter)),
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "b.badgeType", new String[]{ "Hard Drinker" }, defaultConverter))
						),
						new Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", LEFT, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "badges", "b", INNER, true)
						),
						new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "discounts", "d", RIGHT, true),
						new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "d.discountType", new String[]{ "Gold customer" }, defaultConverter))
				);
	}

	@Test // TC-3. interface extending two interfaces with repeated @Join
	public void createsSpecFromInterfaceExtendingTwoInterfacesWithRepeatedJoin() throws Exception {
		MethodParameter param = methodParameter("specExtendingTwoInterfacesWithRepeatedJoin", SpecExtendingTwoInterfacesWithRepeatedJoin.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("itemName", "Duff Beer")
				.withParameterValues("badgeType", "Hard Drinker")
				.withParameterValues("itemName2", "Duff Beer2")
				.withParameterValues("badgeType2", "Hard Drinker2")
				.build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(innerSpecs(resolved))
				.hasSize(4)
				.containsExactlyInAnyOrder(
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o.itemName", new String[]{ "Duff Beer" }, defaultConverter)),
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "b.badgeType", new String[]{ "Hard Drinker" }, defaultConverter))
						),
						new Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", LEFT, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "badges", "b", INNER, true)
						),
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "o2.itemName2", new String[]{ "Duff Beer2" }, defaultConverter)),
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "b2.badgeType2", new String[]{ "Hard Drinker2" }, defaultConverter))
						),
						new Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders2", "o2", LEFT, false),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "badges2", "b2", RIGHT, true)
						)
				);
	}

}
