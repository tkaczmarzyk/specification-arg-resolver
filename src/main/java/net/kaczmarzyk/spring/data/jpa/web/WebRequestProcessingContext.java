/**
 * Copyright 2014-2022 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.Map;

import net.kaczmarzyk.spring.data.jpa.utils.PathVariableResolver;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Provides information about Controller/method and WebRequest being processed.
 * It is a wrapper around low-level Spring classes, which provides easier access to e.g. path variables.
 * </p>
 * <p>
 * Main extracting method uses {@link org.springframework.web.servlet.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE}
 * that states in its javadocs that this attribute isn't supported in all cases, for these scenarios
 * path variables are extracted using a fallback approach (which has some limitations, eg. it does not support global prefixes).
 * </p>
 * <p>
 * Fallback approach takes only controllers mapping path and tries to match it against request URI path
 * and as it requires both paths to be exactly the same it fails and throws exception.
 * Failing example for fallback would be adding global prefix by using
 * {@link org.springframework.web.servlet.config.annotation.PathMatchConfigurer PathMatchConfigurer}'s
 * method 'addPathPrefix'
 * </p>
 * <p>
 * Let's state we have REST controller with URI <br>
 * {@code /user/{userId} }
 * <br>
 * By adding global prefix "/api/v1" <br>
 * {@code
 * public void configurePathMatch(PathMatchConfigurer configurer) {
 *   configurer.addPathPrefix("/api/v1", c -> c.isAnnotationPresent(RestController.class));
 * }
 * }
 * <br>
 * Request URI changes to <br>
 * {@code /api/v1/user/{userId}} <br>
 * but fallback method still tries to match it against controller URI without prefix<br>
 * {@code /user/{userId}} <br>
 * which results in exception <br>
 * {@code org.springframework.web.util.NestedServletException: Request processing failed; nested exception is net.kaczmarzyk.spring.data.jpa.web.InvalidPathVariableRequestedException:
 * Requested path variable {userId} is not present in Controller request mapping annotations}
 * </p>
 *
 * @author Tomasz Kaczmarzyk
 * @author Robert Dworak, Tratif sp. z o.o.
 */
public class WebRequestProcessingContext {

	private final MethodParameter methodParameter;
	private final NativeWebRequest webRequest;
	private String pathPattern;

	private Map<String, String> resolvedPathVariables;
	
	public WebRequestProcessingContext(MethodParameter methodParameter, NativeWebRequest webRequest) {
		this.methodParameter = methodParameter;
		this.webRequest = webRequest;
	}

	public Class<?> getParameterType() {
		return methodParameter.getParameterType();
	}

	public Annotation[] getParameterAnnotations() {
		return methodParameter.getParameterAnnotations();
	}

	public String[] getParameterValues(String webParamName) {
		return webRequest.getParameterValues(webParamName);
	}

	public QueryContext queryContext() {
		return new WebRequestQueryContext(webRequest);
	}

	public String getPathVariableValue(String pathVariableName) {
		if (resolvedPathVariables == null) {
			resolvedPathVariables = (Map) webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
		}
		//if first method returns null then try alternative path variables resolving method
		if (resolvedPathVariables == null) {
			resolvedPathVariables = PathVariableResolver.resolvePathVariables(pathPattern(), actualWebPath());
		}

		String value = resolvedPathVariables.get(pathVariableName);
		if (value != null) {
			return value;
		} else {
			throw new InvalidPathVariableRequestedException(pathVariableName);
		}
	}

	private String pathPattern() {
		if (pathPattern != null) {
			return pathPattern;
		} else {
			Class<?> controllerClass = methodParameter.getContainingClass();
			if (controllerClass.getAnnotation(RequestMapping.class) != null) {
				RequestMapping controllerMapping = controllerClass.getAnnotation(RequestMapping.class);
				pathPattern = firstOf(controllerMapping.value(), controllerMapping.path());
			}

			String methodPathPattern = null;

			if (methodParameter.hasMethodAnnotation(RequestMapping.class)) {
				RequestMapping methodMapping = methodParameter.getMethodAnnotation(RequestMapping.class);
				methodPathPattern = firstOf(methodMapping.value(), methodMapping.path());
			} else if (methodParameter.hasMethodAnnotation(GetMapping.class)) {
				GetMapping methodMapping = methodParameter.getMethodAnnotation(GetMapping.class);
				methodPathPattern = firstOf(methodMapping.value(), methodMapping.path());
			}

			if (methodPathPattern != null) {
				pathPattern = pathPattern != null ? pathPattern + methodPathPattern : methodPathPattern;
			}
		}
		if (pathPattern == null) {
			throw new IllegalStateException("path pattern could not be resolved (searched for @RequestMapping or @GetMapping)");
		}
		return pathPattern;
	}

	private String firstOf(String[] array1, String[] array2) {
		if (array1.length > 0) {
			return array1[0];
		} else if (array2.length > 0) {
			return array2[0];
		}
		return null;
	}

	private String actualWebPath() {
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
		return request.getPathInfo() != null ? request.getPathInfo() : request.getRequestURI().substring(request.getContextPath().length());
	}

	public String getRequestHeaderValue(String headerKey) {
		return webRequest.getHeader(headerKey);
	}

}
