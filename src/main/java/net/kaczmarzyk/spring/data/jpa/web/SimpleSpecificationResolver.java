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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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

    Specification<Object> buildSpecification(NativeWebRequest req, Spec def) {
        try {
            Collection<String> args = resolveSpecArguments(req, def);
            if (args.isEmpty()) {
                return null;
            } else {
                String[] argsArray = args.toArray(new String[args.size()]);
                return newSpecification(def, argsArray);
            }
        } catch (NoSuchMethodException e) {
        	throw new IllegalStateException("Does the specification class expose at least one of supported constuctors?\nIt can be either 2-arg (String path, String[] httpParamValues) or 3-arg (String path, String[] httpParamValues, String[] config)", e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
	private Specification<Object> newSpecification(Spec def, String[] argsArray) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
    	
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

	private Collection<String> resolveSpecArguments(NativeWebRequest req, Spec specDef) {
		if (specDef.constVal().length != 0) {
			return Arrays.asList(specDef.constVal());
		} else {
			return resolveSpecArgumentsFromHttpParameters(req, specDef);
		}
	}

	private Collection<String> resolveSpecArgumentsFromHttpParameters(NativeWebRequest req, Spec specDef) {
		Collection<String> args = new ArrayList<String>();
		
		if (specDef.params().length != 0) {
		    for (String webParamName : specDef.params()) {
		        String[] httpParamValues = req.getParameterValues(webParamName);
		        addValuesToArgs(httpParamValues, args);
		    }
		} else {
		    String[] httpParamValues = req.getParameterValues(specDef.path());
		    addValuesToArgs(httpParamValues, args);
		}
		return args;
	}

    private void addValuesToArgs(String[] paramValues, Collection<String> args) {
        if (paramValues != null) {
            for (String paramValue : paramValues) {
                if (!StringUtils.isEmpty(paramValue)) {
                    args.add(paramValue);
                }
            }
        }
    }

    boolean canBuildSpecification(NativeWebRequest req, Spec def) {
    	if (isNotEmptyAndContainsNoEmptyValues(def.constVal())) {
    		return true;
    	}
    	
        if (def.params().length == 0) {
            return isNotEmptyAndContainsNoEmptyValues(req.getParameterValues(def.path()));
        } else {
            for (String param : def.params()) {
                if (!isNotEmptyAndContainsNoEmptyValues(req.getParameterValues(param))) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isNotEmptyAndContainsNoEmptyValues(String[] parameterValues) {
        if (parameterValues == null || parameterValues.length == 0) {
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