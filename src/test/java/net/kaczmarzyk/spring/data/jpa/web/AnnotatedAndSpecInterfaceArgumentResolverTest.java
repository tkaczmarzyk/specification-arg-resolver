package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases:
 * TC-1. interface with @And spec
 * TC-2. interface extended by two interfaces with @And spec
 */
public class AnnotatedAndSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @And spec
	@And({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "lastName", path = "lastName", spec = Equal.class)
	})
	private interface GenderLastNameAndSpec extends Specification<Customer> {
	}

	@And({
			@Spec(params = "firstName", path = "firstName", spec = Equal.class)
	})
	private interface FirstNameAndSpec extends Specification<Customer> {

	}

	// TC-2. interface extended by two interfaces with and spec
	private interface EmptyFilterExtendedByTwoInterfacesWithAndFilter extends GenderLastNameAndSpec, FirstNameAndSpec {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @And spec
		public void annotatedInterface(GenderLastNameAndSpec genderLastNameAndSpec) {
		}

		// TC-2. interface extended by two interfaces with and spec
		public void getCustomersByEmptyFilterExtendedByTwoInterfacesWithAndFilterAndSimpleSpecParam(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) EmptyFilterExtendedByTwoInterfacesWithAndFilter spec) {
		}
	}

	@Test // TC-1. interface with @And spec
	public void createsSpecFromSimpleAnnotatedInterface() throws Exception {
		MethodParameter param = methodParameter("annotatedInterface", GenderLastNameAndSpec.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(GenderLastNameAndSpec.class);

		assertThat(innerSpecs(resolved))
				.hasSize(2)
				.containsExactly(
						new EmptyResultOnTypeMismatch<>(equal(ctx, "gender", "MALE")),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "lastName", "Simpson"))
				);
	}

	@Test // TC-2. interface extended by two interfaces with @Or spec
	public void createsSpecFromEmptyFilterExtendedByTwoInterfacesWithAndFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersByEmptyFilterExtendedByTwoInterfacesWithAndFilterAndSimpleSpecParam",
				EmptyFilterExtendedByTwoInterfacesWithAndFilter.class
		);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("gender", "MALE")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("firstName", "Homer")
				.withParameterValues("nickName", "Hom").build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(EmptyFilterExtendedByTwoInterfacesWithAndFilter.class);

		Collection<Specification<Object>> innerSpecs = innerSpecs(resolved);

		assertThat(innerSpecs)
				.hasSize(3)
				.containsOnly(
						new Conjunction<>(new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "firstName", new String[]{ "Homer" }, converter))),
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "gender", new String[]{ "MALE" }, converter)),
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "lastName", new String[]{ "Simpson" }, converter))
						),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}

}
