package net.kaczmarzyk.spring.data.jpa.web;

import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;

public interface SpecificationResolver<T extends Annotation> {

	default <K extends Annotation> boolean supports(K specDefinition) {
		return getSupportedSpecificationDefinition().equals(specDefinition.annotationType());
	}

	Class<? extends Annotation> getSupportedSpecificationDefinition();

	Specification<Object> buildSpecification(WebRequestProcessingContext context, T def);

}
