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

import net.kaczmarzyk.spring.data.jpa.utils.TypeUtil;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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
public class SpecificationArgumentResolver implements HandlerMethodArgumentResolver {

	private Map<Class<? extends Annotation>, SpecificationResolver<? extends Annotation>> resolversBySupportedType;

	public SpecificationArgumentResolver() {
		 this(null, null);
	}
	
	public SpecificationArgumentResolver(ConversionService conversionService) {
		this(conversionService, null);
	}
	
	public SpecificationArgumentResolver(AbstractApplicationContext applicationContext) {
		this(null, applicationContext);
	}
	
	public SpecificationArgumentResolver(ConversionService conversionService, AbstractApplicationContext abstractApplicationContext) {
		SimpleSpecificationResolver simpleSpecificationResolver = new SimpleSpecificationResolver(conversionService, abstractApplicationContext);
		
		resolversBySupportedType = Arrays.asList(
				simpleSpecificationResolver,
				new OrSpecificationResolver(simpleSpecificationResolver),
				new DisjunctionSpecificationResolver(simpleSpecificationResolver),
				new ConjunctionSpecificationResolver(simpleSpecificationResolver),
				new AndSpecificationResolver(simpleSpecificationResolver),
				new JoinSpecificationResolver(),
				new JoinsSpecificationResolver(),
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
	

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> paramType = parameter.getParameterType();

		return paramType.isInterface() && Specification.class.isAssignableFrom(paramType) && isAnnotated(parameter);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
	                              WebDataBinderFactory binderFactory) throws Exception {

		WebRequestProcessingContext context = new WebRequestProcessingContext(parameter, webRequest);

		List<Specification<Object>> specs = resolveSpec(context);

		if (specs.isEmpty()) {
			return null;
		}

		if (specs.size() == 1) {
			Specification<Object> firstSpecification = specs.iterator().next();

			if (Specification.class == parameter.getParameterType()) {
				return firstSpecification;
			} else {
				return EnhancerUtil.wrapWithInterfaceImplementation(parameter.getParameterType(), firstSpecification);
			}
		}

		Specification<Object> spec = new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(specs);

		return EnhancerUtil.wrapWithInterfaceImplementation(parameter.getParameterType(), spec);
	}

	private List<Specification<Object>> resolveSpec(WebRequestProcessingContext context) throws Exception {
		List<Specification<Object>> specAccumulator = new ArrayList<>();

		resolveSpecFromInterfaceAnnotations(context, specAccumulator);
		resolveSpecFromParameterAnnotations(context, specAccumulator);

		return specAccumulator;
	}

	private void resolveSpecFromParameterAnnotations(WebRequestProcessingContext context,
	                                                 List<Specification<Object>> accum) {
		forEachSupportedSpecificationDefinition(
				context.getParameterAnnotations(),
				specDefinition -> {
					Specification<Object> specification = buildSpecification(context, specDefinition);
					if (nonNull(specification)) {
						accum.add(specification);
					}
				}
		);
	}

	private void resolveSpecFromInterfaceAnnotations(WebRequestProcessingContext context,
	                                                 List<Specification<Object>> accumulator) {
		Collection<Class<?>> ifaceTree = TypeUtil.interfaceTree(context.getParameterType());

		for (Class<?> iface : ifaceTree) {
			forEachSupportedInterfaceSpecificationDefinition(iface,
					(specDefinition) -> {
						Specification<Object> specification = buildSpecification(context, specDefinition);
						if (nonNull(specification)) {
							accumulator.add(specification);
						}
					}
			);
		}
	}

	private Specification<Object> buildSpecification(WebRequestProcessingContext context, Annotation specDef) {
		SpecificationResolver resolver = resolversBySupportedType.get(specDef.annotationType());

		if (resolver == null) {
			throw new IllegalArgumentException(
					"Definition is not supported. " +
							"Specification resolver is not able to build specification from definition of type :" + specDef.annotationType()
			);
		}

		return resolver.buildSpecification(context, specDef);
	}

	private boolean isAnnotated(MethodParameter methodParameter) {
		for (Annotation annotation : methodParameter.getParameterAnnotations()) {
			for (Class<? extends Annotation> annotationType : resolversBySupportedType.keySet()) {
				if (annotationType.equals(annotation.annotationType())) {
					return true;
				}
			}
		}

		return isAnnotatedRecursively(methodParameter.getParameterType());
	}

	private final boolean isAnnotatedRecursively(Class<?> target) {
		if (target.getAnnotations().length != 0) {
			for (Class<? extends Annotation> annotationType : resolversBySupportedType.keySet()) {
				if (target.getAnnotation(annotationType) != null) {
					return true;
				}
			}
		}

		for (Class<?> targetInterface : target.getInterfaces()) {
			if (isAnnotatedRecursively(targetInterface)) {
				return true;
			}
		}

		return false;
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
		for (Class<? extends Annotation> annotationType : resolversBySupportedType.keySet()) {
			if (target.getAnnotations().length != 0) {
				Annotation potentialAnnotation = target.getAnnotation(annotationType);
				if (potentialAnnotation != null) {
					specificationBuilder.accept(potentialAnnotation);
				}
			}
		}
	}

}
