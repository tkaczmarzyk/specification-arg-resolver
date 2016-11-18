/**
 * Copyright 2014-2016 the original author or authors.
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

import static java.util.stream.Collectors.joining;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
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
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


/**
 * @author Tomasz Kaczmarzyk
 */
class SimpleSpecificationResolver implements HandlerMethodArgumentResolver {

    private final Converter converter;
    
    SimpleSpecificationResolver(Converter converter) {
      super();
      this.converter = converter;
    }
  
    @Override
    public Specification<?> resolveArgument(MethodParameter param, ModelAndViewContainer mav, NativeWebRequest req,
            WebDataBinderFactory binderFactory) throws Exception {

        Spec def = param.getParameterAnnotation(Spec.class);

        return buildSpecification(req, def);
    }

    Specification<Object> buildSpecification(NativeWebRequest req, Spec def) {
        try {
            Collection<String> args = resolveSpecArguments(req, def);
            if (args.isEmpty() && !isZeroArgSpec(def)) {
                if (def.required()) {
                    String param = (def.params().length > 0) ? Arrays.stream(def.params()).collect(joining(", ")) : def.path();
                    throw new IllegalArgumentException(String.format("Required http parameter '%s' is not present", param));
                }
                return null;
            } else {
                String[] argsArray = args.toArray(new String[args.size()]);
                Specification<Object> spec = newSpecification(def, argsArray);
                return def.onTypeMismatch().wrap(spec);
            }
        } catch (NoSuchMethodException e) {
        	throw new IllegalStateException("Does the specification class expose at least one of the supported constuctors?\n"
        			+ "It can be either:\n"
        			+ "  2-arg (String path, String[] args)\n"
        			+ "  3-arg (String path, String[] args, Converter converter)\n"
        			+ "  4-arg (String path, String[] args, Converter converter, String[] config)", e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isZeroArgSpec(Spec def) {
		return ZeroArgSpecification.class.isAssignableFrom(def.spec());
	}

	@SuppressWarnings("unchecked")
	private Specification<Object> newSpecification(Spec def, String[] argsArray) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		Converter converter = resolveConverter(def);
		
		Specification<Object> spec;
		if (def.config().length == 0) {
	    	try {
	    		spec = def.spec().getConstructor(String.class, String[].class)
			            .newInstance(def.path(), argsArray);
	    	} catch (NoSuchMethodException e) {
	    	    try {
	    	      spec = def.spec().getConstructor(String.class, String[].class, Converter.class)
			            .newInstance(def.path(), argsArray, converter);
	    	    } catch (NoSuchMethodException e2) {
	                spec = def.spec().getConstructor(String.class, String[].class, Converter.class, OnTypeMismatch.class)
	                        .newInstance(def.path(), argsArray, converter, def.onTypeMismatch());
	            }
	    	}
		} else {
		    try {
		    	spec = def.spec().getConstructor(String.class, String[].class, Converter.class, String[].class)
			            .newInstance(def.path(), argsArray, converter, def.config());
		    } catch (NoSuchMethodException e) {
		    	try {
		    		spec = def.spec().getConstructor(String.class, String[].class, Converter.class)
				            .newInstance(def.path(), argsArray, converter);
		    	} catch (NoSuchMethodException e2) {
		    		// legacy constructor support, to retain backward-compatibility
		    	    try {
		    	      spec = def.spec().getConstructor(String.class, String[].class, String[].class)
				            .newInstance(def.path(), argsArray, def.config());
		    	    } catch (NoSuchMethodException e3) {
		    	        spec = def.spec().getConstructor(String.class, String[].class, Converter.class, OnTypeMismatch.class)
                          .newInstance(def.path(), argsArray, converter, def.onTypeMismatch());
		    	    }
		    	}
		    }
		}
		return spec;
	}

	private Converter resolveConverter(Spec def) {
  	    if (def.config().length == 0) {
  	      return converter;
  	    }
		if (def.config().length == 1) {
			String dateFormat = def.config()[0];
			Converter cloned = converter.clone();
			cloned.getObjectMapper().setDateFormat(new SimpleDateFormat(dateFormat));
			return cloned;
		}
		throw new IllegalStateException("config should contain only one value -- a date format"); // TODO support other config values as well
	}

	private Collection<String> resolveSpecArguments(NativeWebRequest req, Spec specDef) {
		if (specDef.constVal().length != 0) {
			return Arrays.asList(specDef.constVal());
		} else {
		    Collection<String> resolved = resolveSpecArgumentsFromHttpParameters(req, specDef);
		    if (resolved.isEmpty() && specDef.defaultVal().length != 0) {
		        Arrays.stream(specDef.defaultVal()).forEach(resolved::add);
		    }
		    return resolved;
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

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType() == Specification.class && param.hasParameterAnnotation(Spec.class);
    }

}