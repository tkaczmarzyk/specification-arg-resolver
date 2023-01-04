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

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.MissingPathVarPolicy;

import java.lang.annotation.Annotation;
import java.util.Map;

import static java.util.Objects.isNull;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.MissingPathVarPolicy.EXCEPTION;

/**
 * The purpose of this class is to handle non-web specification building.
 *
 * @author Jakub Radlica
 * @author Kacper Leśniak (Tratif sp. z o.o.)
 * 
 * @see net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder
 */
public class StandaloneProcessingContext implements ProcessingContext {

	private Class<?> specInterface;

	private Map<String, String[]> fallbackArgumentValues;
	private Map<String, String> pathVariableArgs;
	private Map<String, String[]> parameterArgs;
	private Map<String, String> headerArgs;
	private Map<String, String[]> bodyParams;

	private QueryContext queryContext;

	public StandaloneProcessingContext(Class<?> specInterface,
									   Map<String, String[]> fallbackArgumentValues,
									   Map<String, String> pathVariableArgs,
									   Map<String, String[]> parameterArgs,
									   Map<String, String> headerArgs,
									   Map<String, String[]> bodyParams) {
		this.specInterface = specInterface;
		this.fallbackArgumentValues = fallbackArgumentValues;
		this.pathVariableArgs = pathVariableArgs;
		this.parameterArgs = parameterArgs;
		this.headerArgs = headerArgs;
		this.bodyParams = bodyParams;
		this.queryContext = new DefaultQueryContext();
	}

	@Override
	public Class<?> getParameterType() {
		return specInterface;
	}

	/**
	 * This class supports only specifications defined as interface.
	 */
	@Override
	public Annotation[] getParameterAnnotations() {
		return new Annotation[]{};
	}

	@Override
	public QueryContext queryContext() {
		return queryContext;
	}

	@Override
	public String getRequestHeaderValue(String headerKey) {
		return headerArgs.containsKey(headerKey) ? headerArgs.get(headerKey) : getFallbackValue(headerKey);
	}

	@Override
	public String[] getParameterValues(String webParamName) {
		return parameterArgs.containsKey(webParamName) ? parameterArgs.get(webParamName) : getFallbackValues(webParamName);
	}

	@Override
	public String getPathVariableValue(String pathVariableName, MissingPathVarPolicy missingPathVarPolicy) {

		String pathVariableValue = pathVariableArgs.getOrDefault(
			pathVariableName,
			getFallbackValue(pathVariableName));

		if (isNull(pathVariableValue) && missingPathVarPolicy == EXCEPTION) {
			throw new InvalidPathVariableRequestedException(pathVariableName);
		}

		return pathVariableValue;
	}

	@Override
	public String[] getBodyParamValues(String bodyParamName) {
		return bodyParams.containsKey(bodyParamName) ? bodyParams.get(bodyParamName) : getFallbackValues(bodyParamName);
	}

	private String[] getFallbackValues(String argName) {
		return fallbackArgumentValues.get(argName);
	}

	private String getFallbackValue(String argName) {
		if (!fallbackArgumentValues.containsKey(argName) || fallbackArgumentValues.get(argName).length == 0) {
			return null;
		}
		return fallbackArgumentValues.get(argName)[0];
	}
}
