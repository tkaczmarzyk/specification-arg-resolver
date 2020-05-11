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

import static net.kaczmarzyk.spring.data.jpa.web.MethodParameterUtil.getAnnotation;
import static net.kaczmarzyk.spring.data.jpa.web.MethodParameterUtil.isAnnotatedWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;


/**
 * @author Tomasz Kaczmarzyk
 */
class JoinFetchSpecificationResolver implements RecursiveHandlerMethodArgumentResolver {

    private SpecificationArgumentResolver parentResolver;
    
    public JoinFetchSpecificationResolver(SpecificationArgumentResolver parent) {
        this.parentResolver = parent;
    }
    
    @Override
    public boolean supportsParameter(MethodParameter param) {
        Class<?> paramType = param.getParameterType();
        return paramType.isInterface() && Specification.class.isAssignableFrom(paramType)
        		&& isAnnotatedWith(JoinFetch.class, param);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return resolveArgument(parameter, mavContainer, webRequest, binderFactory, new ArrayList<HandlerMethodArgumentResolver>());
    }
    
    @Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory,
			List<HandlerMethodArgumentResolver> recursiveCallers) throws Exception {
    	
    	recursiveCallers.add(this);
    	
    	Specification<Object> fetchSpec = resolveFetchSpec(parameter);
        @SuppressWarnings("unchecked")
        Specification<Object> regularSpec = (Specification<Object>) parentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory, recursiveCallers);
        
        Specification<Object> spec = regularSpec == null ? fetchSpec : new Conjunction<Object>(Arrays.asList(fetchSpec, regularSpec));

        if (Specification.class == parameter.getParameterType()) {
            return spec;
        } else {
            return EnhancerUtil.wrapWithIfaceImplementation(parameter.getParameterType(), spec);
        }
	}

    private Specification<Object> resolveFetchSpec(MethodParameter parameter) {
    	if (isAnnotatedWith(JoinFetch.class, parameter)) {
    		JoinFetch fetchDef = getAnnotation(JoinFetch.class, parameter);
    		return newJoinFetch(fetchDef);
    	} else {
    		throw new IllegalArgumentException("@JoinFetch expected!");
    	}
    }

	private net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object> newJoinFetch(JoinFetch fetchDef) {
		return new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(fetchDef.paths(), fetchDef.joinType());
	}

}
