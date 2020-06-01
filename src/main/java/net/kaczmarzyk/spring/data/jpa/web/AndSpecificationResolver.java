package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomasz Kaczmarzyk
 */
public class AndSpecificationResolver implements SpecificationResolver<And> {

	private SimpleSpecificationResolver specResolver = new SimpleSpecificationResolver();

	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return And.class;
	}

	@Override
	public Specification<Object> buildSpecification(WebRequestProcessingContext context, And def) {
		List<Specification<Object>> innerSpecs = new ArrayList<Specification<Object>>();
		for (Spec innerDef : def.value()) {
			Specification<Object> innerSpec = specResolver.buildSpecification(context, innerDef);
			if (innerSpec != null) {
				innerSpecs.add(innerSpec);
			}
		}

		return innerSpecs.isEmpty() ? null : new Conjunction<>(innerSpecs);
	}

}
