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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.PathVariableResolver;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Provides information about Controller/method and WebRequest being processed.
 * It is a wrapper around low-level Spring classes, which provides easier access to e.g. path variables.
 *
 * @author Tomasz Kaczmarzyk
 * @author Robert Dworak, Tratif sp. z o.o.
 */
public class WebRequestProcessingContext implements ProcessingContext {

	private final MethodParameter methodParameter;
	private final NativeWebRequest webRequest;
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
			throw new InvalidPathVariableRequestedException(pathVariableName);
		}
	}

	public String getRequestHeaderValue(String headerKey) {
		return webRequest.getHeader(headerKey);
	}

}
