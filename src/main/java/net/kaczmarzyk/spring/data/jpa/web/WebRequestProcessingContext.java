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

import net.kaczmarzyk.spring.data.jpa.utils.*;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static java.util.Objects.isNull;

/**
 * Provides information about Controller/method and WebRequest being processed.
 * It is a wrapper around low-level Spring classes, which provides easier access to e.g. path variables.
 * 
 * @author Tomasz Kaczmarzyk
 */
public class WebRequestProcessingContext implements ProcessingContext {

	private final MethodParameter methodParameter;
	private final NativeWebRequest webRequest;
	private String pathPattern;
	private BodyParams bodyParams;

	private Map<String, String> resolvedPathVariables;

	private QueryContext queryContext;
	
	public WebRequestProcessingContext(MethodParameter methodParameter, NativeWebRequest webRequest) {
		this.methodParameter = methodParameter;
		this.webRequest = webRequest;
		this.queryContext = new DefaultQueryContext();
	}

	@Override
	public Class<?> getParameterType() {
		return methodParameter.getParameterType();
	}

	@Override
	public Annotation[] getParameterAnnotations() {
		return methodParameter.getParameterAnnotations();
	}

	@Override
	public String[] getParameterValues(String webParamName) {
		return webRequest.getParameterValues(webParamName);
	}

	@Override
	public QueryContext queryContext() {
		return queryContext;
	}

	@Override
	public String getPathVariableValue(String pathVariableName) {
		if(resolvedPathVariables == null) {
			resolvedPathVariables = PathVariableResolver.resolvePathVariables(pathPattern(), actualWebPath());
		}
		String value = resolvedPathVariables.get(pathVariableName);
		if (value != null) {
			return value;
		} else {
			throw new InvalidPathVariableRequestedException(pathVariableName);
		}
	}

  public String getRequestBody() {
		try {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request == null) {
				throw new IllegalStateException("Request body not present");
			}
			return IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot read request body. Detail: " + ex.getMessage());
		}
	}

	@Override
	public String getRequestHeaderValue(String headerKey) {
		return webRequest.getHeader(headerKey);
	}

	public BodyParams getBodyParams() {
		if (isNull(bodyParams)) {
			String contentType = getRequestHeaderValue(CONTENT_TYPE);
			if (contentType.equals(APPLICATION_JSON_VALUE)) {
				this.bodyParams = JsonBodyParams.parse(getRequestBody());
			} else {
				throw new IllegalArgumentException("Content-type not supported, content-type=" + contentType);
			}
		}
		return bodyParams;
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
	
}
