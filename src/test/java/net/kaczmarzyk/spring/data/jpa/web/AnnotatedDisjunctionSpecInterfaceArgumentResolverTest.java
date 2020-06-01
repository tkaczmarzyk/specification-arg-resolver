package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases:
 * TC-1. interface with @Disjunction spec
 * TC-2. interface extended by two interfaces with @Disjunction spec
 */
public class AnnotatedDisjunctionSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @Disjunction spec
	@net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction(
			value = @And({
					@Spec(params = "gender", path = "gender", spec = Equal.class),
					@Spec(params = "lastName", path = "lastName", spec = Equal.class)
			}),
			or = {
					@Spec(params = "registrationDate", paramSeparator = ',', path = "registrationDate", spec = In.class)
			})
	private interface GenderAndLastNameOrRegistrationDateFilter extends Specification<Customer> {
	}

	// TC-2. interface extended by two interfaces with @Disjunction spec
	@net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction(value = @And({
			@Spec(params = "firstName", path = "firstName", spec = Equal.class)
	}))
	private interface FirstNameFilter extends Specification<Customer> {

	}

	private interface SpecExtendedByTwoOtherDisjunctionFilters extends GenderAndLastNameOrRegistrationDateFilter, FirstNameFilter {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @Disjunction spec
		public void annotatedInterface(GenderAndLastNameOrRegistrationDateFilter spec) {
		}

		// TC-2. interface extended by two interfaces with @Disjunction spec
		public void getCustomersBySpecExtendedByTwoOtherDisjunctionFiltersExtendedByParamSimpleSpec(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) SpecExtendedByTwoOtherDisjunctionFilters spec) {
		}
	}

	@Test // TC-1. interface with @Disjunction spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", GenderAndLastNameOrRegistrationDateFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("registrationDate", "2014-03-20").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(GenderAndLastNameOrRegistrationDateFilter.class);

		assertThat(extractInnerSpecsFromDisjunction(resolved))
				.hasSize(2)
				.containsExactly(
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
								new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
						),
						new EmptyResultOnTypeMismatch<>(in(ctx, "registrationDate", "2014-03-20"))
				);
	}

	@Test // TC-2. interface extended by two interfaces with @Disjunction spec
	public void createsSpecFromEmptyFilterExtendedByTwoInterfacesWithDisjunctionFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersBySpecExtendedByTwoOtherDisjunctionFiltersExtendedByParamSimpleSpec",
				SpecExtendedByTwoOtherDisjunctionFilters.class
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
				.isInstanceOf(SpecExtendedByTwoOtherDisjunctionFilters.class);

		Collection<Specification<Object>> innerSpecs = innerSpecs(resolved);

		Assertions.assertThat(innerSpecs)
				.hasSize(3)
				.containsOnly(
						new Disjunction<>(
								new Conjunction<>(
										new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
										new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName","Simpson"))
								),
								new EmptyResultOnTypeMismatch<>(in(ctx,"registrationDate", "2014-03-25", "2014-03-20"))
						),
						new Disjunction<>(new Conjunction<>(new EmptyResultOnTypeMismatch<>(equal(ctx, "firstName", "Homer")))),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}

}
