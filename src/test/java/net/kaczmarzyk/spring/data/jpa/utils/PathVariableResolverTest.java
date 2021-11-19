/**
 * Copyright 2014-2020 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class PathVariableResolverTest {

	@Test
	public void extractsPathVariable() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId}", "/customers/77");

		assertThat(resolvedVariables.get("customerId")).isEqualTo("77");
	}

	@Test
	public void extractPathVariableWithRegex() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId:[0-9]+}", "/customers/77");

		assertThat(resolvedVariables.get("customerId")).isEqualTo("77");
	}

	@Test
	public void extractsPathVariable_untilSlash() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId}/orders", "/customers/77/orders");

		assertThat(resolvedVariables.get("customerId")).isEqualTo("77");
	}

	@Test
	public void extractsPathVariableAmongMultiplePathVariables() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId}/orders/{orderId:[0-9]+}", "/customers/77/orders/66");

		assertThat(resolvedVariables.get("customerId")).isEqualTo("77");
		assertThat(resolvedVariables.get("orderId")).isEqualTo("66");
	}

	@Test
	public void returnsNullWhenPathVariableIsNotDefined() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId}/orders/{orderId}", "/customers/77/orders/66");

		assertThat(resolvedVariables.get("someUndefinedVar")).isNull();
	}

	@Test
	public void returnsNullWhenPathVariableIsNotPresentOnActualPath() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId}/orders/{orderId}", "/customers/77/orders");

		assertThat(resolvedVariables.get("orderId")).isNull();
	}
	
	@Test
	public void returnsNullWhenActualPathDoesNotMatchWithPathPattern() {
		Map<String, String> resolvedVariables = PathVariableResolver
				.resolvePathVariables("/customers/{customerId:[0-9]+}", "/customers/invalidCustomerIdFormat");
		
		assertThat(resolvedVariables.get("customerId")).isNull();
	}
}
