package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases:
 * TC-1. interface with @Or spec
 * TC-2. interface extended by two interfaces with @Or spec
 */
public class AnnotatedOrSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @Or spec
	@Or({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "lastName", path = "lastName", spec = Equal.class)
	})
	private interface GenderAndLastNameFilter extends Specification<Customer> {
	}

	@Or({
			@Spec(params = "firstName", path = "firstName", spec = Equal.class)
	})
	private interface FirstNameFilter extends Specification<Customer> {

	}

	// TC-2. interface extended by two interfaces with @Or spec
	private interface SpecExtendedByTwoOtherOrSpecs extends GenderAndLastNameFilter, FirstNameFilter {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @Or spec
		public void annotatedInterface(GenderAndLastNameFilter spec) {
		}

		// TC-2. interface extended by two interfaces with @Or spec
		public void getCustomersByEmptyFilterExtendedByTwoInterfacesWithOrFilterAndSimpleSpecParam(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) SpecExtendedByTwoOtherOrSpecs spec) {
		}
	}

	@Test // TC-1. interface with @Or spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", GenderAndLastNameFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(GenderAndLastNameFilter.class);

		assertThat(extractInnerSpecsFromDisjunction(resolved))
				.hasSize(2)
				.containsExactly(
						new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
				);
	}

	@Test // TC-2. interface extended by two interfaces with @Or spec
	public void createsSpecFromEmptyFilterExtendedByTwoInterfacesWithOrFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersByEmptyFilterExtendedByTwoInterfacesWithOrFilterAndSimpleSpecParam",
				SpecExtendedByTwoOtherOrSpecs.class
		);
		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("firstName", "Homer")
				.withParameterValues("nickName", "Hom").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(SpecExtendedByTwoOtherOrSpecs.class);

		Collection<Specification<Object>> innerSpecs = innerSpecs(resolved);

		Assertions.assertThat(innerSpecs)
				.hasSize(3)
				.containsOnly(
						new Disjunction<>(new EmptyResultOnTypeMismatch<>(equal(ctx, "firstName", "Homer"))),
						new Disjunction<>(
								new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
								new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
						),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}

}
