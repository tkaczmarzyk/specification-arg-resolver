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

import java.util.Arrays;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


/**
 * @author Tomasz Kaczmarzyk
 */
class JoinFetchSpecificationResolver implements HandlerMethodArgumentResolver {

    private SpecificationArgumentResolver parentResolver;
    
    public JoinFetchSpecificationResolver(SpecificationArgumentResolver parent) {
        this.parentResolver = parent;
    }
    
    @Override
    public boolean supportsParameter(MethodParameter param) {
        Class<?> paramType = param.getParameterType();
        return paramType.isInterface() && Specification.class.isAssignableFrom(paramType) &&
                (param.hasParameterAnnotation(JoinFetch.class) || paramType.isAnnotationPresent(JoinFetch.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Specification<Object> fetchSpec = resolveFetchSpec(parameter);
        @SuppressWarnings("unchecked")
        Specification<Object> regularSpec = (Specification<Object>) parentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory, this);
        
        Specification<Object> spec = regularSpec == null ? fetchSpec : new Conjunction<Object>(Arrays.asList(fetchSpec, regularSpec));

        if (Specification.class == parameter.getParameterType()) {
            return spec;
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setInterfaces(new Class[] { parameter.getParameterType() });
            enhancer.setCallback(EnhancerUtil.delegateTo(spec));
            return enhancer.create();
        }
    }

    private Specification<Object> resolveFetchSpec(MethodParameter parameter) {
        JoinFetch fetchDef = getFetchDef(parameter);
        
        return new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(fetchDef.paths(), fetchDef.joinType());
    }

    private JoinFetch getFetchDef(MethodParameter parameter) {
        JoinFetch fetchDef = parameter.getParameterAnnotation(JoinFetch.class);
        if (fetchDef == null) {
            fetchDef = parameter.getParameterType().getAnnotation(JoinFetch.class);
        }
        return fetchDef;
    }

}
