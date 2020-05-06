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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import net.kaczmarzyk.spring.data.jpa.domain.ZeroArgSpecification;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


/**
 * @author Tomasz Kaczmarzyk
 */
class SimpleSpecificationResolver implements HandlerMethodArgumentResolver {

    @Override
    public Specification<?> resolveArgument(MethodParameter param, ModelAndViewContainer mav, NativeWebRequest req,
            WebDataBinderFactory binderFactory) throws Exception {

        Spec def = param.getParameterAnnotation(Spec.class);
        WebRequestProcessingContext context = new WebRequestProcessingContext(param, req);

        return buildSpecification(context, def);
    }

    Specification<Object> buildSpecification(WebRequestProcessingContext context, Spec def) {
        try {
            Collection<String> args = resolveSpecArguments(context, def);
            if (args.isEmpty() && !isZeroArgSpec(def)) {
                return null;
            } else {
                String[] argsArray = args.toArray(new String[args.size()]);
                Specification<Object> spec = newSpecification(def, argsArray, context);
                return def.onTypeMismatch().wrap(spec);
            }
        } catch (NoSuchMethodException e) {
        	throw new IllegalStateException("Does the specification class expose at least one of the supported constuctors?\n"
        			+ "It can be either:\n"
        			+ "  3-arg (QueryContext queryCtx, String path, String[] args)\n"
        			+ "  4-arg (QueryContext queryCtx, String path, String[] args, Converter converter)\n"
        			+ "  5-arg (QueryContext queryCtx, String path, String[] args, Converter converter, String[] config)", e);
        } catch (RuntimeException e) {
        	throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

	private boolean isZeroArgSpec(Spec def) {
		return ZeroArgSpecification.class.isAssignableFrom(def.spec());
	}

	@SuppressWarnings("unchecked")
	private Specification<Object> newSpecification(Spec def, String[] argsArray, WebRequestProcessingContext context) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		QueryContext queryCtx = context.queryContext();
		Converter converter = resolveConverter(def);

		Specification<Object> spec;
		if (def.config().length == 0) {
			try {
	    		spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class)
			            .newInstance(queryCtx, def.path(), argsArray);
	    	} catch (NoSuchMethodException e2) {
	    		spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class, Converter.class)
			            .newInstance(queryCtx, def.path(), argsArray, converter);
	    	}
		} else {
		    try {
		    	spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class, Converter.class, String[].class)
			            .newInstance(queryCtx, def.path(), argsArray, converter, def.config());
		    } catch (NoSuchMethodException e) {
		    	try {
		    		spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class, Converter.class)
				            .newInstance(queryCtx, def.path(), argsArray, converter);
		    	} catch (NoSuchMethodException e2) {
		    		// legacy constructor support, to retain backward-compatibility
		    		spec = def.spec().getConstructor(String.class, String[].class, String[].class)
				            .newInstance(def.path(), argsArray, def.config());
		    	}
		    }
		}
		return spec;
	}

	private Converter resolveConverter(Spec def) {
		if (def.config().length == 0) {
			return Converter.withTypeMismatchBehaviour(def.onTypeMismatch());
		}
		if (def.config().length == 1) {
			String dateFormat = def.config()[0];
			return Converter.withDateFormat(dateFormat, def.onTypeMismatch());
		}
		throw new IllegalStateException("config should contain only one value -- a date format"); // TODO support other config values as well
	}

	private Collection<String> resolveSpecArguments(WebRequestProcessingContext context, Spec specDef) {
		if (specDef.constVal().length != 0) {
			return Arrays.asList(specDef.constVal());
		} else if (specDef.pathVars().length != 0){
			return resolveSpecArgumentsFromPathVariables(context, specDef);
		} else {
		    Collection<String> resolved = resolveSpecArgumentsFromHttpParameters(context, specDef);
		    if (resolved.isEmpty() && specDef.defaultVal().length != 0) {
		        Arrays.stream(specDef.defaultVal()).forEach(resolved::add);
		    }
		    return resolved;
		}
	}

	private Collection<String> resolveSpecArgumentsFromPathVariables(WebRequestProcessingContext context, Spec specDef) {
		Collection<String> args = new ArrayList<>();
		for (String pathVar : specDef.pathVars()) {
			args.add(context.getPathVariableValue(pathVar));
		}
		return args;
	}

	private Collection<String> resolveSpecArgumentsFromHttpParameters(WebRequestProcessingContext context, Spec specDef) {
		Collection<String> args = new ArrayList<String>();

		if (specDef.params().length != 0) {
			String paramsSeparator = specDef.paramSeparator();
		    for (String webParamName : specDef.params()) {
		        String[] httpParamValues = context.getParameterValues(webParamName);
			    if(paramsSeparator.isEmpty()) {
				    addValuesToArgs(httpParamValues, args);
			    } else {
			    	for(String paramValue: httpParamValues) {
			    		String[] separatedParamValues = paramValue.split(paramsSeparator);
					    addValuesToArgs(separatedParamValues, args);
				    }
			    }
		    }
		} else {
		    String[] httpParamValues = context.getParameterValues(specDef.path());
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

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType() == Specification.class && param.hasParameterAnnotation(Spec.class);
    }

}