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

import java.util.ArrayList;
import java.util.List;

import net.kaczmarzyk.spring.data.jpa.domain.Disjunction;
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
class OrSpecificationResolver implements HandlerMethodArgumentResolver {

    private SimpleSpecificationResolver specResolver = new SimpleSpecificationResolver();
    
    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType() == Specification.class && param.hasParameterAnnotation(Or.class);
    }

    @Override
    public Specification<?> resolveArgument(MethodParameter param, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Or def = param.getParameterAnnotation(Or.class);
        WebRequestProcessingContext context = new WebRequestProcessingContext(param, webRequest);
        
        return buildSpecification(context, def);
    }

    Specification<Object> buildSpecification(WebRequestProcessingContext context, Or def) {
        List<Specification<Object>> innerSpecs = new ArrayList<Specification<Object>>();
        for (Spec innerDef : def.value()) {
            Specification<Object> innerSpec = specResolver.buildSpecification(context, innerDef);
            if (innerSpec != null) {
                innerSpecs.add(innerSpec);
            }
        }
        
        return innerSpecs.isEmpty() ? null : new Disjunction<Object>(innerSpecs);
    }

}
