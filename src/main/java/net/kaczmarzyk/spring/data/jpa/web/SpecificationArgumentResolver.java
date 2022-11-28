/**
 * Copyright 2014-2022 the original author or authors.
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

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.annotation.Annotation;


/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class SpecificationArgumentResolver implements HandlerMethodArgumentResolver {

	private SpecificationFactory specificationFactory;

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
		this.specificationFactory = new SpecificationFactory(conversionService, abstractApplicationContext);
	}
	

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> paramType = parameter.getParameterType();

		return paramType.isInterface() && Specification.class.isAssignableFrom(paramType) && isAnnotated(parameter);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
	                              WebDataBinderFactory binderFactory) throws Exception {

		ProcessingContext context = new WebRequestProcessingContext(parameter, webRequest);

		return specificationFactory.createSpecificationDependingOn(context);
	}

	private boolean isAnnotated(MethodParameter methodParameter) {
		for (Annotation annotation : methodParameter.getParameterAnnotations()) {
			for (Class<? extends Annotation> annotationType : specificationFactory.getResolversBySupportedType()) {
				if (annotationType.equals(annotation.annotationType())) {
					return true;
				}
			}
		}

		return isAnnotatedRecursively(methodParameter.getParameterType());
	}

	private final boolean isAnnotatedRecursively(Class<?> target) {
		if (target.getAnnotations().length != 0) {
			for (Class<? extends Annotation> annotationType : specificationFactory.getResolversBySupportedType()) {
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

}
