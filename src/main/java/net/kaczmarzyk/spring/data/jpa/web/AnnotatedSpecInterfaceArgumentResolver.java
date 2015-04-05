/**
 * Copyright 2014-2015 the original author or authors.
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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
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
	private DisjunctionSpecificationResolver disjunctionResolver = new DisjunctionSpecificationResolver();
	private ConjunctionSpecificationResolver conjunctionResolver = new ConjunctionSpecificationResolver();
	
	private List<Class<? extends Annotation>> annotationTypes = Arrays.asList(Spec.class, Or.class, And.class);
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> paramType = parameter.getParameterType();
		return paramType.isInterface() && Specification.class.isAssignableFrom(paramType) && isAnnotated(paramType);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		
		Object specDef = getAnnotation(parameter.getParameterType());
		
		Specification<Object> spec;
		
		if (specDef instanceof Spec) {
			spec = simpleResolver.buildSpecification(webRequest, (Spec) specDef);
		} else if (specDef instanceof Or) {
			spec = disjunctionResolver.buildSpecification(webRequest, (Or) specDef);
		} else if (specDef instanceof And) {
			spec = conjunctionResolver.buildSpecification(webRequest, (And) specDef);
		} else {
			throw new IllegalStateException();
		}
		
		if (spec == null) {
			return null;
		}
		
		Enhancer enhancer = new Enhancer();
		enhancer.setInterfaces(new Class[] { parameter.getParameterType() });
		enhancer.setCallback(proxyFor(spec));
		
		return enhancer.create();
	}

	private final boolean isAnnotated(Class<?> target) {
		return getAnnotation(target) != null;
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
	
	private Callback proxyFor(final Specification<Object> targetSpec) {
		return new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (method.getName().equals("toPredicate")) {
					return proxy.invoke(targetSpec, args);
				}
				return proxy.invoke(obj, args);
			}
		};
	}

}
