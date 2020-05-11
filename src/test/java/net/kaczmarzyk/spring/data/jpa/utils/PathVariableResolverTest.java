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

import org.junit.Test;

public class PathVariableResolverTest {

	@Test
	public void extractsPathVariable() {
		PathVariableResolver resolver = PathVariableResolver
				.forPathPatternAndActualPath("/customers/{customerId}", "/customers/77");

		assertThat(resolver.resolveValue("customerId")).isEqualTo("77");
	}

	@Test
	public void extractsPathVariable_untilSlash() {
		PathVariableResolver resolver = PathVariableResolver
				.forPathPatternAndActualPath("/customers/{customerId}/orders", "/customers/77/orders");

		assertThat(resolver.resolveValue("customerId")).isEqualTo("77");
	}

	@Test
	public void extractsPathVariableAmongMultiplePathVariables() {
		PathVariableResolver resolver = PathVariableResolver
				.forPathPatternAndActualPath("/customers/{customerId}/orders/{orderId}", "/customers/77/orders/66");

		assertThat(resolver.resolveValue("customerId")).isEqualTo("77");
		assertThat(resolver.resolveValue("orderId")).isEqualTo("66");
	}

	@Test
	public void returnsNullWhenPathVariableIsNotDefined() {
		PathVariableResolver resolver = PathVariableResolver
				.forPathPatternAndActualPath("/customers/{customerId}/orders/{orderId}", "/customers/77/orders/66");

		assertThat(resolver.resolveValue("someUndefinedVar")).isNull();
	}

	@Test
	public void returnsNullWhenPathVariableIsNotPresentOnActualPath() {
		PathVariableResolver resolver = PathVariableResolver
				.forPathPatternAndActualPath("/customers/{customerId}/orders/{orderId}", "/customers/77/orders");

		assertThat(resolver.resolveValue("orderId")).isNull();
	}
}
