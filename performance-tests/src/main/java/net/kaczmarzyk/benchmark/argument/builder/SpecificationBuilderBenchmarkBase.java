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
package net.kaczmarzyk.benchmark.argument.builder;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder.specification;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.MissingPathVarPolicy.IGNORE;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
abstract class SpecificationBuilderBenchmarkBase {

	// Please remember to also add new parameter to your interface with defined specifications
	private static final SortedMap<String, String> PARAMETERS_WITH_VALUES = new TreeMap<>(ofEntries(
		entry("firstName", "Bart"),
		entry("lastName", "Simpson"),
		entry("age", "19"),
		entry("gender", "MALE"),
		entry("hairColor", "yellow"),
		entry("iris", "black"),
		entry("height", "5′9"),
		entry("race", "simpsonish"),
		entry("motherName", "Marge"),
		entry("fatherName", "Homer"),
		entry("vip", "false"),
		entry("city", "Springfield"),
		entry("criminalPast", "false"),
		entry("religion", "simpsonish"),
		entry("state", "Texas")
	));

	protected <T extends Specification<?>> T paramsSpecification(Class<T> specificationType, int parametersAmount) {

		return specificationWithArguments(
			specificationType,
			(entry, builder) -> builder.withParam(entry.getKey(), entry.getValue()),
			parametersAmount);
	}

	protected <T extends Specification<?>> T pathVarsSpecification(Class<T> specificationType, int pathVarsAmount) {

		return specificationWithArguments(
			specificationType,
			(entry, builder) -> builder.withPathVar(entry.getKey(), entry.getValue()),
			pathVarsAmount);
	}

	protected <T extends Specification<?>> T headersSpecification(Class<T> specificationType, int headersAmount) {

		return specificationWithArguments(
			specificationType,
			(entry, builder) -> builder.withHeader(entry.getKey(), entry.getValue()),
			headersAmount);
	}

	protected <T extends Specification<?>> T jsonPathsSpecification(Class<T> specificationType, int jsonPathsAmount) {

		return specificationWithArguments(
			specificationType,
			(entry, builder) -> builder.withJsonBodyParam(entry.getKey(), entry.getValue()),
			jsonPathsAmount);
	}

	/**
	 *
	 * @param specificationType - type of interface with defined specification
	 * @param argumentsAppender - Consumer that defines the way of passing arguments to SpecificationBuilder (withParam(...) or withHeader(...), etc.)
	 * @param argumentsAmount - amount of argument passed to specification (max 15)
	 * @param <T> - type of the expected Specification
	 * @return Specification of given type with defined arguments.
	 */
	private <T extends Specification<?>> T specificationWithArguments(Class<T> specificationType,
	                                                                  BiConsumer<Map.Entry<String, String>, SpecificationBuilder<T>> argumentsAppender,
	                                                                  int argumentsAmount) {

		SpecificationBuilder<T> specificationBuilder = specification(specificationType);
		Iterator<Map.Entry<String, String>> iterator = PARAMETERS_WITH_VALUES.entrySet().iterator();

		int includedParameters = 0;
		while (iterator.hasNext() && includedParameters < argumentsAmount) {
			Map.Entry<String, String> nextParamWithValue = iterator.next();
			argumentsAppender.accept(nextParamWithValue, specificationBuilder);
			includedParameters++;
		}

		return specificationBuilder.build();
	}

	@Or({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
	})
	protected interface ThreeParamsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", pathVars = "age", spec = Equal.class),
		@Spec(path = "city", pathVars = "city", spec = Equal.class),
		@Spec(path = "criminalPast", pathVars = "criminalPast", spec = Equal.class),
	})
	protected interface ThreePathVarsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", headers = "age", spec = Equal.class),
		@Spec(path = "city", headers = "city", spec = Equal.class),
		@Spec(path = "criminalPast", headers = "criminalPast", spec = Equal.class),
	})
	protected interface ThreeHeadersSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", jsonPaths = "age", spec = Equal.class),
		@Spec(path = "city", jsonPaths = "city", spec = Equal.class),
		@Spec(path = "criminalPast", jsonPaths = "criminalPast", spec = Equal.class),
	})
	protected interface ThreeJsonPathsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", params = "lastName", spec = Equal.class),
		@Spec(path = "firstName", params = "firstName", spec = Equal.class),
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "gender", params = "gender", spec = Equal.class),
		@Spec(path = "hairColor", params = "hairColor", spec = Equal.class),
		@Spec(path = "iris", params = "iris", spec = Equal.class),
		@Spec(path = "height", params = "height", spec = Equal.class),
		@Spec(path = "race", params = "race", spec = Equal.class),
		@Spec(path = "motherName", params = "motherName", spec = Equal.class),
		@Spec(path = "fatherName", params = "fatherName", spec = Equal.class),
		@Spec(path = "vip", params = "vip", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
		@Spec(path = "religion", params = "religion", spec = Equal.class),
		@Spec(path = "state", params = "state", spec = Equal.class),
	})
	protected interface FifteenParamsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", pathVars = "lastName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "firstName", pathVars = "firstName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "age", pathVars = "age", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "gender", pathVars = "gender", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "hairColor", pathVars = "hairColor", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "iris", pathVars = "iris", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "height", pathVars = "height", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "race", pathVars = "race", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "motherName", pathVars = "motherName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "fatherName", pathVars = "fatherName", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "vip", pathVars = "vip", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "city", pathVars = "city", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "criminalPast", pathVars = "criminalPast", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "religion", pathVars = "religion", spec = Equal.class, missingPathVarPolicy = IGNORE),
		@Spec(path = "state", pathVars = "state", spec = Equal.class, missingPathVarPolicy = IGNORE),
	})
	protected interface FifteenPathVarsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", headers = "lastName", spec = Equal.class),
		@Spec(path = "firstName", headers = "firstName", spec = Equal.class),
		@Spec(path = "age", headers = "age", spec = Equal.class),
		@Spec(path = "gender", headers = "gender", spec = Equal.class),
		@Spec(path = "hairColor", headers = "hairColor", spec = Equal.class),
		@Spec(path = "iris", headers = "iris", spec = Equal.class),
		@Spec(path = "height", headers = "height", spec = Equal.class),
		@Spec(path = "race", headers = "race", spec = Equal.class),
		@Spec(path = "motherName", headers = "motherName", spec = Equal.class),
		@Spec(path = "fatherName", headers = "fatherName", spec = Equal.class),
		@Spec(path = "vip", headers = "vip", spec = Equal.class),
		@Spec(path = "city", headers = "city", spec = Equal.class),
		@Spec(path = "criminalPast", headers = "criminalPast", spec = Equal.class),
		@Spec(path = "religion", headers = "religion", spec = Equal.class),
		@Spec(path = "state", headers = "state", spec = Equal.class),
	})
	protected interface FifteenHeadersSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "lastName", jsonPaths = "lastName", spec = Equal.class),
		@Spec(path = "firstName", jsonPaths = "firstName", spec = Equal.class),
		@Spec(path = "age", jsonPaths = "age", spec = Equal.class),
		@Spec(path = "gender", jsonPaths = "gender", spec = Equal.class),
		@Spec(path = "hairColor", jsonPaths = "hairColor", spec = Equal.class),
		@Spec(path = "iris", jsonPaths = "iris", spec = Equal.class),
		@Spec(path = "height", jsonPaths = "height", spec = Equal.class),
		@Spec(path = "race", jsonPaths = "race", spec = Equal.class),
		@Spec(path = "motherName", jsonPaths = "motherName", spec = Equal.class),
		@Spec(path = "fatherName", jsonPaths = "fatherName", spec = Equal.class),
		@Spec(path = "vip", jsonPaths = "vip", spec = Equal.class),
		@Spec(path = "city", jsonPaths = "city", spec = Equal.class),
		@Spec(path = "criminalPast", jsonPaths = "criminalPast", spec = Equal.class),
		@Spec(path = "religion", jsonPaths = "religion", spec = Equal.class),
		@Spec(path = "state", jsonPaths = "state", spec = Equal.class),
	})
	protected interface FifteenJsonPathsSpecification extends Specification<Object> {
	}
}
