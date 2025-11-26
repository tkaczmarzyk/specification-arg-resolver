/**
 * Copyright 2014-2025 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class WebRequestProcessingContextFallbackPathVariableResolverTest extends WebRequestProcessingContextPathVariableResolverTestBase {


	@ParameterizedTest
	@MethodSource("testControllers")
	public void throwsInvalidPathVariableRequestedExceptionWhenTheActualRequestPathDoesNotMatchWithEndpointPath(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/request-path-which-does-not-match");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);

		assertThatThrownBy(() -> getPathVariableFromContext(context, "customerId"))
				.isInstanceOf(InvalidPathVariableRequestedException.class);
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMappingUsingMethod_empty(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMappingUsingMethod_empty(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}

	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
}
