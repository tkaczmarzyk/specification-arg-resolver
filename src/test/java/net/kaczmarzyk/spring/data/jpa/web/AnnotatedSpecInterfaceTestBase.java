package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public abstract class AnnotatedSpecInterfaceTestBase extends ResolverTestBase {

	protected Converter converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT);
	protected SpecificationArgumentResolver specificationArgumentResolver = new SpecificationArgumentResolver();

	protected Collection<Specification<Object>> innerSpecs(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Conjunction<Object> resolvedConjunction =
				ReflectionUtils.get(ReflectionUtils.get(resolvedSpec, "CGLIB$CALLBACK_0"), "val$targetSpec");

		return ReflectionUtils.get(resolvedConjunction, "innerSpecs");
	}

	protected Collection<Specification<Object>> extractInnerSpecsFromDisjunction(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Disjunction<Object> resolvedDisjunction =
				ReflectionUtils.get(ReflectionUtils.get(resolvedSpec, "CGLIB$CALLBACK_0"), "val$targetSpec");

		return ReflectionUtils.get(resolvedDisjunction, "innerSpecs");
	}

	public Equal<Object> equal(WebRequestProcessingContext ctx, String path, String value) {
		return new Equal<>(ctx.queryContext(), path, new String[]{value}, converter);
	}

	public In<Object> in(WebRequestProcessingContext ctx, String path, String... values) {
		return new In<>(ctx.queryContext(), path, values, converter);
	}

}
