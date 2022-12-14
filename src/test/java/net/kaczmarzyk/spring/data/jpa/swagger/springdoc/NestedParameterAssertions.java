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
package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.assertj.core.api.AbstractAssert;

import static net.kaczmarzyk.spring.data.jpa.swagger.springdoc.ParameterAssertions.assertThatParameter;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
class NestedParameterAssertions extends AbstractAssert<NestedParameterAssertions, Parameter> {

	private static final String QUERY = "query";
	private static final String PATH = "path";
	private static final String HEADER = "header";

	private final OperationAssertions parent;

	public NestedParameterAssertions(Parameter parameter, OperationAssertions parent) {
		super(parameter, NestedParameterAssertions.class);
		this.parent = parent;
	}

	public static NestedParameterAssertions assertThatNestedParameter(Parameter parameter, OperationAssertions parent) {
		return new NestedParameterAssertions(parameter, parent);
	}

	public NestedParameterAssertions withQueryParameterName(String parameterName) {
		assertThatParameter(actual).isLocatedIn(QUERY);
		assertThatParameter(actual).hasParameterName(parameterName);
		return this;
	}

	public NestedParameterAssertions withPathParameterName(String parameterName) {
		assertThatParameter(actual).isLocatedIn(PATH);
		assertThatParameter(actual).hasParameterName(parameterName);
		return this;
	}

	public NestedParameterAssertions withHeaderParameterName(String parameterName) {
		assertThatParameter(actual).isLocatedIn(HEADER);
		assertThatParameter(actual).hasParameterName(parameterName);
		return this;
	}

	public NestedParameterAssertions withRequiredStatus() {
		assertThatParameter(actual).isRequired();
		return this;
	}

	public NestedParameterAssertions withNotRequiredStatus() {
		assertThatParameter(actual).isNotRequired();
		return this;
	}

	public NestedParameterAssertions withType(String type) {
		assertThatParameter(actual).hasType(type);
		return this;
	}

	public NestedParameterAssertions withSchema(Schema<?> schema) {
		assertThatParameter(actual).hasSchema(schema);
		return this;
	}

	public OperationAssertions and() {
		return parent;
	}

}
