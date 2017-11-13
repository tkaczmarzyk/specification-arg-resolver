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

import java.util.ArrayList;
import java.util.List;

import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
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
class ConjunctionSpecificationResolver implements HandlerMethodArgumentResolver {

    private SimpleSpecificationResolver specResolver = new SimpleSpecificationResolver();
    private OrSpecificationResolver orResolver = new OrSpecificationResolver();
    
    
    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType() == Specification.class && param.hasParameterAnnotation(Conjunction.class);
    }

    @Override
    public Specification<?> resolveArgument(MethodParameter param, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Conjunction def = param.getParameterAnnotation(Conjunction.class);
        
        return buildSpecification(webRequest, def);
    }

	Specification<Object> buildSpecification(NativeWebRequest webRequest, Conjunction def) {
		List<Specification<Object>> innerSpecs = new ArrayList<Specification<Object>>();
        for (Or innerOrDef : def.value()) {
        	Specification<Object> innerOr = orResolver.buildSpecification(webRequest, innerOrDef);
        	if (innerOr != null) {
        		innerSpecs.add(innerOr);
        	}
        }
        for (Spec innerDef : def.and()) {
        	Specification<Object> innerSpec = specResolver.buildSpecification(webRequest, innerDef);
        	if (innerSpec != null) {
        		innerSpecs.add(innerSpec);
        	}
        }
        
        return innerSpecs.isEmpty() ? null : new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(innerSpecs);
	}

}