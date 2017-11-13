/**
 * Copyright 2014-2017 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author Tomasz Kaczmarzyk
 */
class AnnotatedSpecInterfaceArgumentResolver implements HandlerMethodArgumentResolver {

	private SimpleSpecificationResolver simpleResolver = new SimpleSpecificationResolver();
	private OrSpecificationResolver orResolver = new OrSpecificationResolver();
	private DisjunctionSpecificationResolver disjunctionResolver = new DisjunctionSpecificationResolver();
	private ConjunctionSpecificationResolver conjunctionResolver = new ConjunctionSpecificationResolver();
	private AndSpecificationResolver andResolver = new AndSpecificationResolver();
	
	private List<Class<? extends Annotation>> annotationTypes = Arrays.asList(Spec.class, Or.class, And.class, Conjunction.class, Disjunction.class);
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> paramType = parameter.getParameterType();
		return paramType.isInterface() && Specification.class.isAssignableFrom(paramType) && isAnnotated(paramType);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		
		Specification<Object> ifaceSpec = resolveSpecFromInterfaceAnnotations(webRequest, parameter);
		Specification<Object> paramSpec = resolveSpecFromParameterAnnotations(webRequest, parameter);
		
		if (ifaceSpec == null && paramSpec == null) {
			return null;
		}
		
		Specification<Object> spec = new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(ifaceSpec, paramSpec);
		
		return EnhancerUtil.wrapWithIfaceImplementation(parameter.getParameterType(), spec);
	}

	private Specification<Object> resolveSpecFromParameterAnnotations(NativeWebRequest webRequest, MethodParameter parameter) throws Exception {
		Object specDef = getAnnotation(parameter.getParameterAnnotations());
		return specDef != null ? buildSpecification(webRequest, specDef) : null;
	}

	private Specification<Object> resolveSpecFromInterfaceAnnotations(NativeWebRequest webRequest, MethodParameter param) {
		Object specDef = getAnnotation(param.getParameterType());
		return buildSpecification(webRequest, specDef);
	}

	private Specification<Object> buildSpecification(NativeWebRequest webRequest, Object specDef) {
		Specification<Object> spec;
		
		if (specDef instanceof Spec) {
			spec = simpleResolver.buildSpecification(webRequest, (Spec) specDef);
		} else if (specDef instanceof Or) {
			spec = orResolver.buildSpecification(webRequest, (Or) specDef);
		} else if (specDef instanceof Disjunction) {
			spec = disjunctionResolver.buildSpecification(webRequest, (Disjunction) specDef);
		} else if (specDef instanceof Conjunction) {
			spec = conjunctionResolver.buildSpecification(webRequest, (Conjunction) specDef);
		} else if (specDef instanceof And) {
			spec = andResolver.buildSpecification(webRequest, (And) specDef);
		} else {
			throw new IllegalStateException();
		}
		return spec;
	}

	private final boolean isAnnotated(Class<?> target) {
		return getAnnotation(target) != null;
	}
	
	private Object getAnnotation(Annotation[] parameterAnnotations) {
		for (Annotation annotation: parameterAnnotations) {
			for (Class<? extends Annotation> annotationType : annotationTypes) {
				if (annotationType.isAssignableFrom(annotation.getClass())) {
					return annotation;
				}
			}
		}
		return null;
	}
	
	private final Object getAnnotation(Class<?> target) {
		for (Class<? extends Annotation> annotationType : annotationTypes) {
			Annotation potentialAnnotation = target.getAnnotation(annotationType);
			if (potentialAnnotation != null) {
				return potentialAnnotation;
			}
		}
		return null;
	}
}
