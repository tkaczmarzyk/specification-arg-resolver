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

import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kacper Leśniak
 */
public class StandaloneProcessingContextTest {

	private StandaloneProcessingContext context;

	@Before
	public void setupContext() {
		Map<String, String[]> args = new HashMap<>();
		Map<String, String[]> params = new HashMap<>();
		Map<String, String> pathVars = new HashMap<>();
		Map<String, String> headers = new HashMap<>();

		args.put("fallback", new String[]{"example"});
		params.put("param", new String[]{"example"});
		pathVars.put("pathVar", "example");
		headers.put("header", "example");

		context = new StandaloneProcessingContext(
				null,
				args,
				pathVars,
				params,
				headers);
	}

	@Test
	public void shouldSearchValueInFallbackMapWhenValueIsNotPresentInArgumentSpecificMap() {
		String fallbackValueForPathVariable = context.getPathVariableValue("fallback");
		String[] fallbackValueForParams = context.getParameterValues("fallback");
		String fallbackValueForHeader = context.getRequestHeaderValue("fallback");

		assertThat(fallbackValueForPathVariable).isEqualTo("example");
		assertThat(fallbackValueForParams).isEqualTo(new String[]{"example"});
		assertThat(fallbackValueForHeader).isEqualTo("example");
	}

	@Test
	public void shouldReturnPathVariableValue() {
		assertThat(context.getPathVariableValue("pathVar")).isEqualTo("example");
	}

	@Test
	public void shouldThrowInvalidPathVariableRequestedExceptionWhenPathVariableDoesNotExist() {
		assertThrows(InvalidPathVariableRequestedException.class, () -> context.getPathVariableValue("notExisting"));
	}

	@Test
	public void shouldReturnParamVariableValue() {
		assertThat(context.getParameterValues("param")).contains("example");
	}

	@Test
	public void shouldReturnNullWhenParamVariableDoesNotExist() {
		assertThat(context.getParameterValues("notExisting")).isNull();
	}

	@Test
	public void shouldReturnHeaderVariableValue() {
		assertThat(context.getRequestHeaderValue("header")).isEqualTo("example");
	}

	@Test
	public void shouldReturnNullWhenHeaderVariableDoesNotExist() {
		assertThat(context.getRequestHeaderValue("notExisting")).isNull();
	}

}