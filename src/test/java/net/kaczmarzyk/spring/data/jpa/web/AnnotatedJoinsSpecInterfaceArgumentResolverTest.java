package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.JoinType;
import java.util.Collection;

import static javax.persistence.criteria.JoinType.INNER;
import static javax.persistence.criteria.JoinType.LEFT;
import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases:
 * TC-1. interface with @Joins spec
 * TC-2. interface extended by two interfaces with @Joins spec
 */
public class AnnotatedJoinsSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	// TC-1. interface with @Joins spec
	@Joins({
			@net.kaczmarzyk.spring.data.jpa.web.annotation.Join(path = "orders", alias = "o"),
			@net.kaczmarzyk.spring.data.jpa.web.annotation.Join(path = "orders2", alias = "o2", type = LEFT)
	})
	@Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
	private interface OrderedItemNameFilter<T> extends Specification<T> {
	}

	@And(value = {
			@Spec(path = "gender", params = "genderIn", spec = In.class),
			@Spec(path = "lastName", params = "lastName", spec = In.class)
	})
	private interface LastNameGenderFilterExtendedByOrderedItemNameFilter extends OrderedItemNameFilter<Customer> {
	}

	@Joins({
			@net.kaczmarzyk.spring.data.jpa.web.annotation.Join(path = "badges", alias = "b")
	})
	@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class)
	private interface BadgeFilter extends Specification<Customer> {
	}

	// TC-2. interface extended by two interfaces with @Joins spec
	private interface SpecExtendedByTwoOtherInterfacesWithJoinsFilter extends LastNameGenderFilterExtendedByOrderedItemNameFilter, BadgeFilter {
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	static class TestController {
		// TC-1. interface with @Joins spec
		public void annotatedInterface(OrderedItemNameFilter spec) {
		}

		// TC-2. interface extended by two interfaces with @Joins spec
		public void getCustomersBySpecExtendedByTwoOtherInterfacesWithJoinsFilterExtendedByParamSimpleSpec(
				@Spec(params = "nickName", path = "nickName", spec = Like.class) SpecExtendedByTwoOtherInterfacesWithJoinsFilter spec) {
		}
	}

	@Test // TC-1. interface with @Joins spec
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
						new Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", INNER, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders2", "o2", LEFT, true)
						),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "o.itemName", "Item-123"))
				);
	}

	@Test // TC-2. interface extended by two interfaces with @Joins spec
	public void createsSpecFromEmptyFilterExtendedByTwoInterfacesWithJoinsFilterAndSimpleSpecParam() throws Exception {
		MethodParameter param = methodParameter(
				"getCustomersBySpecExtendedByTwoOtherInterfacesWithJoinsFilterExtendedByParamSimpleSpec",
				SpecExtendedByTwoOtherInterfacesWithJoinsFilter.class
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
				.isInstanceOf(SpecExtendedByTwoOtherInterfacesWithJoinsFilter.class);

		Conjunction<Object> resolvedConjunction =
				ReflectionUtils.get(ReflectionUtils.get(resolved, "CGLIB$CALLBACK_0"), "val$targetSpec");

		Collection<Specification<Object>> innerSpecs = ReflectionUtils.get(resolvedConjunction, "innerSpecs");

		Assertions.assertThat(innerSpecs)
				.hasSize(6)
				.containsOnly(
						new Conjunction<>(new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "badges", "b", JoinType.INNER, true)),
						new EmptyResultOnTypeMismatch<>(equal(ctx,"b.badgeType", "Beef Eater")),
						new Conjunction<>(
								new EmptyResultOnTypeMismatch<>(in(ctx, "gender", "MALE")),
								new EmptyResultOnTypeMismatch<>(in(ctx, "lastName", "Simpson"))
						),
						new Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders", "o", JoinType.INNER, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "orders2", "o2", LEFT, true)
						),
						new EmptyResultOnTypeMismatch<>(equal(ctx, "o.itemName", "Pizza")),
						new Like<>(ctx.queryContext(), "nickName", "Hom")
				);
	}
}
