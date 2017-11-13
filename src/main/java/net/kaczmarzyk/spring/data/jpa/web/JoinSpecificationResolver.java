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

import static net.kaczmarzyk.spring.data.jpa.web.MethodParameterUtil.isAnnotatedWith;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;

/**
 *
 * @author Tomasz Kaczmarzyk
 */
class JoinSpecificationResolver implements HandlerMethodArgumentResolver {

	private SpecificationArgumentResolver parentResolver;
	
	public JoinSpecificationResolver(SpecificationArgumentResolver parentResolver) {
		this.parentResolver = parentResolver;
	}
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest request, WebDataBinderFactory binder)
			throws Exception {
		
		Specification<Object> joinSpec = resolveJoin(parameter, request);
		
        @SuppressWarnings("unchecked")
        Specification<Object> regularSpec = (Specification<Object>) parentResolver.resolveArgument(parameter, mavContainer, request, binder, this);
        
        Specification<Object> spec = regularSpec == null ? joinSpec : new Conjunction<Object>(Arrays.asList(joinSpec, regularSpec));

        if (Specification.class == parameter.getParameterType()) {
            return spec;
        } else {
            return EnhancerUtil.wrapWithIfaceImplementation(parameter.getParameterType(), spec);
        }
	}

	private Specification<Object> resolveJoin(MethodParameter parameter, NativeWebRequest request) {
		if (isAnnotatedWith(Join.class, parameter)) {
    		Join fetchDef = getJoinDef(Join.class, parameter);
    		return newJoin(fetchDef, request);
    	} else {
    		throw new IllegalArgumentException("@Join was expected!");
    	}
	}
	
	private net.kaczmarzyk.spring.data.jpa.domain.Join<Object> newJoin(Join joinDef, NativeWebRequest request) {
		return new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(new WebRequestQueryContext(request), joinDef.on(), joinDef.alias(), joinDef.type(), joinDef.distinct());
	}

    private <A extends Annotation> A getJoinDef(Class<A> annotation, MethodParameter parameter) { // TODO duplicated code (JoinFetchSpecificationResolver)
        A joinDef = parameter.getParameterAnnotation(annotation);
        if (joinDef == null) {
            joinDef = parameter.getParameterType().getAnnotation(annotation);
        }
        return joinDef;
    }

	@Override
	public boolean supportsParameter(MethodParameter param) {
		Class<?> paramType = param.getParameterType();
        return paramType.isInterface()
        		&& Specification.class.isAssignableFrom(paramType)
        		&& isAnnotatedWith(Join.class, param);
	}

}
