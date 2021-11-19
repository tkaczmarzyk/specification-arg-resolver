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
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test cases:
 * TC-1. interface with @Conjunction spec
 * TC-2. interface extending two interfaces with @Conjunction spec
 *
 * @author Jakub Radlica
 */
public class AnnotatedConjunctionSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @Conjunction spec
	@net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction(value = @Or({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "lastName", path = "lastName", spec = Equal.class)
	}),
			and = { @Spec(params = "registrationDate", paramSeparator = ',', path = "registrationDate", spec = In.class) }
	)
	private interface GenderOrLastNameAndRegistrationDateFilter extends Specification<Customer> {
	}

	@net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction(value = @Or({
			@Spec(params = "firstName", path = "firstName", spec = Equal.class)
	}))
	private interface FirstNameFilter extends Specification<Customer> {

	}

	// TC-2. interface extending two interfaces with @Conjunction spec
	private interface EmptyFilterExtendingTwoInterfacesWithConjunctionFilter extends GenderOrLastNameAndRegistrationDateFilter, FirstNameFilter {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @Conjunction spec
		public void annotatedInterface(GenderOrLastNameAndRegistrationDateFilter spec) {
		}

		// TC-2. interface extending two interfaces with @Conjunction spec
		public void getCustomersByEmptyFilterExtendingTwoInterfacesWithConjunctionFilter(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) EmptyFilterExtendingTwoInterfacesWithConjunctionFilter spec) {
		}
	}

	@Test // TC-1. interface with @Conjunction spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", GenderOrLastNameAndRegistrationDateFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("registrationDate", "2014-03-20").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(GenderOrLastNameAndRegistrationDateFilter.class);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new Disjunction<>(
								new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
								new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
						),
						new EmptyResultOnTypeMismatch<>(in(ctx, "registrationDate", "2014-03-20"))
				);
	}

	@Test // TC-2. interface extending two interfaces with @Conjunction spec
	public void createsSpecFromEmptyFilterExtendingTwoInterfacesWithConjunctionFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersByEmptyFilterExtendingTwoInterfacesWithConjunctionFilter",
				EmptyFilterExtendingTwoInterfacesWithConjunctionFilter.class
		);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("firstName", "Homer")
				.withParameterValues("registrationDate", "2014-03-25,2014-03-20")
				.withParameterValues("nickName", "Hom").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(EmptyFilterExtendingTwoInterfacesWithConjunctionFilter.class);

		Collection<Specification<Object>> innerSpecs = innerSpecs(resolved);

		assertThat(innerSpecs)
				.hasSize(3)
				.containsOnly(
						new Conjunction<>(
								new Disjunction<>(
										new EmptyResultOnTypeMismatch<>(equal( ctx, "gender", "MALE")),
										new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
								),
								new EmptyResultOnTypeMismatch<>(in(ctx, "registrationDate", "2014-03-25", "2014-03-20"))
						),
						new Conjunction<>(new Disjunction<>(new EmptyResultOnTypeMismatch<>(equal(ctx, "firstName", "Homer")))),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}

}
