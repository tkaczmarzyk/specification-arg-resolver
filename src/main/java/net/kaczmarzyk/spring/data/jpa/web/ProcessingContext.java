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

public interface ProcessingContext {

	Class<?> getParameterType();

	Annotation[] getParameterAnnotations();

	QueryContext queryContext();

	String getRequestHeaderValue(String headerKey);

	String[] getParameterValues(String webParamName);

	default String getPathVariableValue(String pathVariableName) throws InvalidPathVariableRequestedException {
		return getPathVariableValue(pathVariableName, MissingPathVarPolicy.EXCEPTION);
	}

	String getPathVariableValue(String pathVariableName, MissingPathVarPolicy missingPathVarPolicy);

	String[] getBodyParamValues(String bodyParamName);
}
