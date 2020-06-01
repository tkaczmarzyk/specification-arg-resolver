package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
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
 * TC-2. interface extended by two interfaces with @Join spec
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

	// TC-2. interface extended by two interfaces with @Join spec
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

		// TC-2. interface extended by two interfaces with @Join spec
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
				.containsExactly(
						new EmptyResultOnTypeMismatch<>(equal(ctx, "o.itemName", "Item-123")),
						new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", LEFT, true)
				);
	}

	@Test // TC-2. interface extended by two interfaces with @Join spec
	public void createsSpecFromEmptyFilterExtendedByTwoInterfacesWithJoinFilterAndSimpleSpecParam() throws Exception {
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
