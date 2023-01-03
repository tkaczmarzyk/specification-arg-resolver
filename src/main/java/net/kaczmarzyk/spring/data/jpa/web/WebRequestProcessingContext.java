/**
 * Copyright 2014-2023 the original author or authors.
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

import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.parseMediaType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.BodyParams;
import net.kaczmarzyk.spring.data.jpa.utils.IOUtils;
import net.kaczmarzyk.spring.data.jpa.utils.JsonBodyParams;
import net.kaczmarzyk.spring.data.jpa.utils.PathVariableResolver;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 *
 * Provides information about Controller/method and WebRequest being processed.
 * It is a wrapper around low-level Spring classes, which provides easier access to e.g. path variables.
 *
 * @author Tomasz Kaczmarzyk
 */
public class WebRequestProcessingContext implements ProcessingContext {

	private final MethodParameter methodParameter;
	private final NativeWebRequest webRequest;
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
		if (resolvedPathVariables == null) {
			resolvedPathVariables = PathVariableResolver.resolvePathVariables(webRequest, methodParameter);
		}

		String value = resolvedPathVariables.get(pathVariableName);
		if (value != null) {
			return value;
		} else {
			return "";
		}
	}

<<<<<<< HEAD
	public String getRequestHeaderValue(String headerKey) {
		return webRequest.getHeader(headerKey);
	}

	@Override
	public String[] getBodyParamValues(String bodyParamName) {
		return getBodyParams().getParamValues(bodyParamName).toArray(new String[0]);
	}

	private BodyParams getBodyParams() {
		if (isNull(bodyParams)) {
			String contentType = getRequestHeaderValue(CONTENT_TYPE);
			MediaType mediaType = parseMediaType(contentType);
			if (APPLICATION_JSON.includes(mediaType)) {
				this.bodyParams = JsonBodyParams.parse(getRequestBody());
			} else {
				throw new IllegalArgumentException("Content-type not supported, content-type=" + contentType);
			}
=======
	private String pathPattern() {
		if (pathPattern != null) {
			return pathPattern;
		} else {
			Class<?> controllerClass = methodParameter.getContainingClass();
			if (controllerClass.getAnnotation(RequestMapping.class) != null) {
				RequestMapping controllerMapping = controllerClass.getAnnotation(RequestMapping.class);
				pathPattern = getPathFromValueOrPath(controllerMapping.value(), controllerMapping.path());
			}
			
			String methodPathPattern = null;
			
			if (methodParameter.hasMethodAnnotation(RequestMapping.class)) {
				RequestMapping methodMapping = methodParameter.getMethodAnnotation(RequestMapping.class);
				methodPathPattern = getPathFromValueOrPath(methodMapping.value(), methodMapping.path());
			} else if (methodParameter.hasMethodAnnotation(GetMapping.class)) {
				GetMapping methodMapping = methodParameter.getMethodAnnotation(GetMapping.class);
				methodPathPattern = getPathFromValueOrPath(methodMapping.value(), methodMapping.path());
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

	private String getPathFromValueOrPath(String[] value, String[] path) {
		if (value.length > 0) {
			return getPathMatchingRequestUrl(value);
		} else if (path.length > 0) {
			return getPathMatchingRequestUrl(path);
>>>>>>> 70ead54ebfbe10bdecf257ef152a66e17319b6f6
		}
		return bodyParams;
	}

<<<<<<< HEAD
	private String getRequestBody() {
		try {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request == null) {
				throw new IllegalStateException("Request body not present");
			}
			return IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot read request body", ex);
		}
=======
	private String getPathMatchingRequestUrl(String[] paths) {
		if (paths.length == 1) {
			return paths[0];
		}
		String actualPath = actualWebPath();
		int index = 0;
		int matches = 0;
		for (int i = 0; i < paths.length; i++) {
			int pairs = countPairs(paths[i], actualPath);
			if (pairs > matches) {
				index = i;
				matches = pairs;
			}
		}
		return paths[index];
	}

	private int countPairs(String s1, String s2) {
		int count = 0;
		int minLength = s1.length() < s2.length() ? s1.length() : s2.length();
		for (int i = 0; i < minLength; i++) {
			if (s1.charAt(i) == s2.charAt(i)) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	private String actualWebPath() {
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
		return request.getPathInfo() != null ? request.getPathInfo() : request.getRequestURI().substring(request.getContextPath().length());
>>>>>>> 70ead54ebfbe10bdecf257ef152a66e17319b6f6
	}
}
