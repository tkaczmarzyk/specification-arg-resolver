/**
 * Copyright 2014-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
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
class DisjunctionSpecificationResolver implements HandlerMethodArgumentResolver {

    private SimpleSpecificationResolver specResolver = new SimpleSpecificationResolver();
    private AndSpecificationResolver andResolver = new AndSpecificationResolver();
    
    
    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType() == Specification.class && param.hasParameterAnnotation(Disjunction.class);
    }

    @Override
    public Specification<?> resolveArgument(MethodParameter param, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    	Disjunction def = param.getParameterAnnotation(Disjunction.class);
        
        return buildSpecification(webRequest, def);
    }

	Specification<Object> buildSpecification(NativeWebRequest webRequest, Disjunction def) {
		List<Specification<Object>> innerSpecs = new ArrayList<Specification<Object>>();
		for (And innerAndDef : def.value()) {
        	Specification<Object> innerAnd = andResolver.buildSpecification(webRequest, innerAndDef);
        	if (innerAnd != null) {
        		innerSpecs.add(innerAnd);
        	}
        }
		for (Spec innerDef : def.or()) {
        	Specification<Object> innerSpec = specResolver.buildSpecification(webRequest, innerDef);
        	if (innerSpec != null) {
        		innerSpecs.add(innerSpec);
        	}
        }
        
        return innerSpecs.isEmpty() ? null : new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(innerSpecs);
	}

}