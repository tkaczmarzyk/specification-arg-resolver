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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class WebRequestProcessingContextFallbackPathVariableResolverTest extends WebRequestProcessingContextPathVariableResolverTestBase {

	public WebRequestProcessingContextFallbackPathVariableResolverTest(Class<?> testController) {
		super(testController);
	}

	@Test
	public void throwsInvalidPathVariableRequestedExceptionWhenTheActualRequestPathDoesNotMatchWithEndpointPath() {
		thrown.expect(InvalidPathVariableRequestedException.class);

		MockWebRequest req = new MockWebRequest("/request-path-which-does-not-match");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMappingUsingMethod_empty() {
		MockWebRequest req = new MockWebRequest("/customers/888");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMappingUsingMethod_empty() {
		MockWebRequest req = new MockWebRequest("/customers/888");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMappingUsingMethod_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMappingUsingMethod_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", testController), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
}
