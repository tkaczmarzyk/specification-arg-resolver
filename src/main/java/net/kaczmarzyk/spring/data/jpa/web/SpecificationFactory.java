/*
 * Copyright 2014-2025 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.utils.TypeUtil;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class SpecificationFactory {

	private Map<Class<? extends Annotation>, SpecificationResolver<? extends Annotation>> resolversBySupportedType;

	public SpecificationFactory(ConversionService conversionService, AbstractApplicationContext abstractApplicationContext, Locale locale) {
		SimpleSpecificationResolver simpleSpecificationResolver = new SimpleSpecificationResolver(conversionService, abstractApplicationContext, locale);

		resolversBySupportedType = Arrays.asList(
						simpleSpecificationResolver,
						new OrSpecificationResolver(simpleSpecificationResolver),
						new DisjunctionSpecificationResolver(simpleSpecificationResolver),
						new ConjunctionSpecificationResolver(simpleSpecificationResolver),
						new AndSpecificationResolver(simpleSpecificationResolver),
						new JoinSpecificationResolver(),
						new JoinFetchSpecificationResolver(),
						new RepeatedJoinFetchResolver(),
						new RepeatedJoinResolver()).stream()
				.collect(toMap(
						SpecificationResolver::getSupportedSpecificationDefinition,
						identity(),
						(u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
						LinkedHashMap::new
				));

	}

	public Specification<?> createSpecificationDependingOn(ProcessingContext context) {
		List<Specification<Object>> specs = resolveSpec(context);

		if (specs.isEmpty()) {
			specs = List.of(Specification.unrestricted());
		}

		Specification<Object> spec = specs.size() == 1 ? specs.iterator().next() : new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(specs);

		if (context.getParameterType().isAssignableFrom(spec.getClass())) {
			return spec;
		} else {
			return (Specification<?>) EnhancerUtil.wrapWithIfaceImplementation(context.getParameterType(), spec);
		}
	}

	private List<Specification<Object>> resolveSpec(ProcessingContext context) {
		List<Specification<Object>> specAccumulator = new ArrayList<>();

		resolveSpecFromInterfaceAnnotations(context, specAccumulator);
		resolveSpecFromParameterAnnotations(context, specAccumulator);

		return specAccumulator;
	}

	private void resolveSpecFromParameterAnnotations(ProcessingContext context,
													 List<Specification<Object>> accum) {
		forEachSupportedSpecificationDefinition(
				context.getParameterAnnotations(),
				specDefinition -> {
					Specification<Object> specification = buildSpecification(context, specDefinition);
					if (nonNull(specification) && !specification.equals(Specification.unrestricted())) {
						accum.add(specification);
					}
				}
		);
	}

	private void resolveSpecFromInterfaceAnnotations(ProcessingContext context,
													 List<Specification<Object>> accumulator) {
		Collection<Class<?>> ifaceTree = TypeUtil.interfaceTree(context.getParameterType());

		for (Class<?> iface : ifaceTree) {
			forEachSupportedInterfaceSpecificationDefinition(iface,
					(specDefinition) -> {
						Specification<Object> specification = buildSpecification(context, specDefinition);
						if (nonNull(specification) && !specification.equals(Specification.unrestricted())) {
							accumulator.add(specification);
						}
					}
			);
		}
	}

	private Specification<Object> buildSpecification(ProcessingContext context, Annotation specDef) {
		SpecificationResolver resolver = resolversBySupportedType.get(specDef.annotationType());

		if (resolver == null) {
			throw new IllegalArgumentException(
					"Definition is not supported. " +
							"Specification resolver is not able to build specification from definition of type :" + specDef.annotationType()
			);
		}

		return resolver.buildSpecification(context, specDef);
	}

	private void forEachSupportedSpecificationDefinition(Annotation[] parameterAnnotations, Consumer<Annotation> specificationBuilder) {
		for (Annotation annotation : parameterAnnotations) {
			for (Class<? extends Annotation> annotationType : resolversBySupportedType.keySet()) {
				if (annotationType.isAssignableFrom(annotation.getClass())) {
					specificationBuilder.accept(annotation);
				}
			}
		}
	}

	private void forEachSupportedInterfaceSpecificationDefinition(Class<?> target, Consumer<Annotation> specificationBuilder) {
		if (target.getAnnotations().length != 0) {
			for (Class<? extends Annotation> annotationType : resolversBySupportedType.keySet()) {
				Annotation potentialAnnotation = target.getAnnotation(annotationType);
				if (potentialAnnotation != null) {
					specificationBuilder.accept(potentialAnnotation);
				}
			}
		}
	}

	public Set<Class<? extends Annotation>> getResolversBySupportedType() {
		return resolversBySupportedType.keySet();
	}
}
