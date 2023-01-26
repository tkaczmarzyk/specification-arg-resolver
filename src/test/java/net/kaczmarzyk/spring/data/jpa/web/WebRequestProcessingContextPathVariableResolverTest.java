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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class WebRequestProcessingContextPathVariableResolverTest extends WebRequestProcessingContextPathVariableResolverTestBase {

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMapping_empty(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMapping_empty(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", testController), req);
		
		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
}
