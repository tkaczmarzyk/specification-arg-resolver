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

import org.junit.jupiter.api.Test;

import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.pathVariables;
import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.setPathVariablesInRequestAttributes;
import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.entry;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class WebRequestProcessingContextPathVariableResolverTest extends WebRequestProcessingContextPathVariableResolverTestBase {

	public WebRequestProcessingContextPathVariableResolverTest(Class<?> testController) {
		super(testController);
	}

	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMapping_empty() {
		MockWebRequest req = new MockWebRequest("/customers/888");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMapping_empty() {
		MockWebRequest req = new MockWebRequest("/customers/888");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", testController), req);

		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", testController), req);
		
		assertThat(getPathVariableFromContext(context, "customerId")).isEqualTo("888");
		assertThat(getPathVariableFromContext(context, "orderId")).isEqualTo("99");
	}
}
