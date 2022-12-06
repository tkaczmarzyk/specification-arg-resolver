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

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.assertj.core.api.AbstractAssert;

import static net.kaczmarzyk.spring.data.jpa.swagger.springdoc.NestedParameterAssertions.assertThatNestedParameter;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class OperationAssertions extends AbstractAssert<OperationAssertions, Operation> {

	protected OperationAssertions(Operation operation) {
		super(operation, OperationAssertions.class);
	}

	public static OperationAssertions assertThatOperation(Operation operation) {
		return new OperationAssertions(operation);
	}

	public OperationAssertions hasParametersCount(int count) {
		isNotNull();
		assertThat(actual.getParameters())
			.hasSize(count);
		return this;
	}

	public NestedParameterAssertions containsParameterAtIndex(int index) {
		isNotNull();
		Parameter parameter = actual.getParameters().get(index);
		return assertThatNestedParameter(parameter, this);
	}
}
