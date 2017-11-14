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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import static net.kaczmarzyk.spring.data.jpa.web.MethodParameterUtil.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Joins;

/**
 * @author Tomasz Kaczmarzyk
 */
class JoinsSpecificationResolver implements RecursiveHandlerMethodArgumentResolver {

	private SpecificationArgumentResolver parent;

	public JoinsSpecificationResolver(SpecificationArgumentResolver parent) {
		this.parent = parent;
	}
	
	@Override
	public boolean supportsParameter(MethodParameter param) {
		Class<?> paramType = param.getParameterType();
        return paramType.isInterface() && Specification.class.isAssignableFrom(paramType)
        		&& isAnnotatedWith(Joins.class, param);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return resolveArgument(parameter, mavContainer, webRequest, binderFactory, new ArrayList<HandlerMethodArgumentResolver>());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory,
			List<HandlerMethodArgumentResolver> recursiveCallers) throws Exception {
		
		recursiveCallers.add(this);
    	
    	Specification<Object> joins = resolveJoins(parameter, webRequest);
        @SuppressWarnings("unchecked")
        Specification<Object> regularSpec = (Specification<Object>) parent.resolveArgument(parameter, mavContainer, webRequest, binderFactory, recursiveCallers);
        
        Specification<Object> spec = regularSpec == null ? joins : new Conjunction<Object>(Arrays.asList(joins, regularSpec));

        if (Specification.class == parameter.getParameterType()) {
            return spec;
        } else {
            return EnhancerUtil.wrapWithIfaceImplementation(parameter.getParameterType(), spec);
        }
	}

	private Specification<Object> resolveJoins(MethodParameter parameter, NativeWebRequest webRequest) {
		Collection<Specification<Object>> joins = new ArrayList<>();
		joins.addAll(resolveJoinFetches(parameter));
		joins.addAll(resolveRegularJoins(parameter, webRequest));
		return new Conjunction<>(joins);
	}

	private Collection<? extends Specification<Object>> resolveRegularJoins(MethodParameter parameter, NativeWebRequest webRequest) {
		Joins joinsDef = getAnnotation(Joins.class, parameter);
		Collection<Specification<Object>> joins = new ArrayList<>();
		for (Join joinDef : joinsDef.value()) {
			joins.add(newJoin(joinDef, webRequest));
		}
		return joins;
	}

	private net.kaczmarzyk.spring.data.jpa.domain.Join<Object> newJoin(Join joinDef, NativeWebRequest request) {
		return new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(new WebRequestQueryContext(request), joinDef.path(), joinDef.alias(), joinDef.type(), joinDef.distinct());
	}

	private Collection<? extends Specification<Object>> resolveJoinFetches(MethodParameter parameter) {
		Joins joinsDef = getAnnotation(Joins.class, parameter);
		Collection<Specification<Object>> joins = new ArrayList<>();
		for (JoinFetch fetchDef : joinsDef.fetch()) {
			joins.add(newJoinFetch(fetchDef));
		}
		return joins;
	}

	private net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object> newJoinFetch(JoinFetch fetchDef) {
		return new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(fetchDef.paths(), fetchDef.joinType());
	}

}
