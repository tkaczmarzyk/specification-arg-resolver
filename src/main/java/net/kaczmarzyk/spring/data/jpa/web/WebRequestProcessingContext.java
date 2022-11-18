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

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Provides information about Controller/method and WebRequest being processed.
 * It is a wrapper around low-level Spring classes, which provides easier access to e.g. path variables.
 * 
 * @author Tomasz Kaczmarzyk
 * @author Robert Dworak
 */
public class WebRequestProcessingContext {

	private final MethodParameter methodParameter;
	private final NativeWebRequest webRequest;

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
		if(resolvedPathVariables == null) {
			resolvedPathVariables = (Map) webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
		}

		if (resolvedPathVariables != null && resolvedPathVariables.get(pathVariableName) != null) {
			return resolvedPathVariables.get(pathVariableName);
		} else {
			throw new InvalidPathVariableRequestedException(pathVariableName);
		}
	}

	public String getRequestHeaderValue(String headerKey) {
		return webRequest.getHeader(headerKey);
	}

}
