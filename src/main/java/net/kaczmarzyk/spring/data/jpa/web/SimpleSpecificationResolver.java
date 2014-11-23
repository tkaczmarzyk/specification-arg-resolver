/**
 * Copyright 2014 the original author or authors.
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
import java.util.Collection;

import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


/**
 * @author Tomasz Kaczmarzyk
 */
class SimpleSpecificationResolver implements HandlerMethodArgumentResolver {

    @Override
    public Specification<?> resolveArgument(MethodParameter param, ModelAndViewContainer mav, NativeWebRequest req,
            WebDataBinderFactory binderFactory) throws Exception {
        
        Spec def = param.getParameterAnnotation(Spec.class);
        
        return buildSpecification(req, def);
    }

    @SuppressWarnings("unchecked")
    Specification<Object> buildSpecification(NativeWebRequest req, Spec def) {
        try {
            Collection<String> args = new ArrayList<String>();
            if (def.params().length != 0) {
                for (String webParam : def.params()) {
					String[] paramValues = req.getParameterValues(webParam);
					addParametersValuesToArgs(paramValues, args);
				}
            } else {
                String[] paramValues = req.getParameterValues(def.path());
                addParametersValuesToArgs(paramValues, args);
            }
            if (args.isEmpty()) {
                return null;
            } else {
                String[] argsArray = args.toArray(new String[args.size()]);
                
                Specification<Object> spec;
                if (def.config().length == 0) {
                    spec = def.spec().getConstructor(String.class, String[].class)
                            .newInstance(def.path(), argsArray);
                } else {
                    spec = def.spec().getConstructor(String.class, String[].class, String[].class)
                            .newInstance(def.path(), argsArray, def.config());
                }
                
                return spec;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

	private void addParametersValuesToArgs(String[] paramValues, Collection<String> args) {
		if (paramValues != null) {
			for (String paramValue : paramValues) {
				if (!StringUtils.isEmpty(paramValue)) {
					args.add(paramValue);
				}
			}
		}
	}
    
	boolean canBuildSpecification(NativeWebRequest req, Spec def) {
		if (def.params().length == 0) {
			return isNotNullAndContainsNoEmptyValues(req.getParameterValues(def.path()));
		} else {
			for (String param : def.params()) {
				if (!isNotNullAndContainsNoEmptyValues(req.getParameterValues(param))) {
					return false;
				}
			}
			return true;
		}
	}
    
	private boolean isNotNullAndContainsNoEmptyValues(String[] parameterValues) {
		if (parameterValues == null) {
			return false;
		} else {
			for (String value : parameterValues) {
				if (StringUtils.isEmpty(value)) {
					return false;
				}
			}
			return true;
		}
	}

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType() == Specification.class && param.hasParameterAnnotation(Spec.class);
    }

}