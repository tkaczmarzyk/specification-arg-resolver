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
package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
class ParameterAssertions extends AbstractAssert<ParameterAssertions, Parameter> {

	public ParameterAssertions(Parameter parameter) {
		super(parameter, ParameterAssertions.class);
	}

	public static ParameterAssertions assertThatParameter(Parameter parameter) {
		return new ParameterAssertions(parameter);
	}

	public ParameterAssertions hasParameterName(String parameterName) {
		isNotNull();
		assertThat(actual.getName())
			.isEqualTo(parameterName);
		return this;
	}

	public ParameterAssertions isRequired() {
		isNotNull();
		assertThat(actual.getRequired())
			.isTrue();
		return this;
	}

	public ParameterAssertions isNotRequired() {
		isNotNull();
		assertThat(actual.getRequired())
			.isFalse();
		return this;
	}

	public ParameterAssertions hasType(String type) {
		isNotNull();
		assertThat(actual.getSchema().getType())
			.isEqualTo(type);
		return this;
	}

	public ParameterAssertions hasSchema(Schema<?> schema) {
		isNotNull();
		assertThat(actual.getSchema())
			.isEqualTo(schema);
		return this;
	}

	public ParameterAssertions isLocatedIn(String parameterIn) {
		isNotNull();
		assertThat(actual.getIn())
			.isEqualTo(parameterIn);
		return this;
	}

}
