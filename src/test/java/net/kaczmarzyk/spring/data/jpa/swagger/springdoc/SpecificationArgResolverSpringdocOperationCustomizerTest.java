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
package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static net.kaczmarzyk.spring.data.jpa.swagger.springdoc.OperationAssertions.assertThatOperation;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationArgResolverSpringdocOperationCustomizerTest {

	private static final String DEFAULT_PARAM_TYPE = "string";
	private static final String OBJECT_PARAM_TYPE = "object";

	private static final StringSchema STRING_SCHEMA = new StringSchema();

	private SpecificationArgResolverSpringdocOperationCustomizer springdocOperationCustomizer;

	@BeforeEach
	public void initializeCustomizer() {
		springdocOperationCustomizer = new SpecificationArgResolverSpringdocOperationCustomizer();
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParameters() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("disjunctionWithDifferentParamsLocationTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(3)
			.containsParameterAtIndex(0)
			.withQueryParameterName("queryParam")
			.withNotRequiredStatus()
			.withType(DEFAULT_PARAM_TYPE)
			.and()
			.containsParameterAtIndex(1)
			.withHeaderParameterName("headerParam")
			.withNotRequiredStatus()
			.withType(DEFAULT_PARAM_TYPE)
			.and()
			.containsParameterAtIndex(2)
			.withPathParameterName("pathVarParam")
			.withRequiredStatus()
			.withType(DEFAULT_PARAM_TYPE);
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecJsonPathsParameters() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("disjunctionWithJsonPathsParamsTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then

		// {
		//   "firstLevel": {
		//     "secondLevel": {
		//       "paramA": "string",
		//       "paramB": "string"
		//     },
		//     "paramC": "string"
		//   },
		//   "paramD": "string"
		// }
		Schema<Object> secondLevelSchema = new ObjectSchema();
		secondLevelSchema.addProperty("paramA", STRING_SCHEMA);
		secondLevelSchema.addProperty("paramB", STRING_SCHEMA);

		Schema<Object> firstLevelSchema = new ObjectSchema();
		firstLevelSchema.addProperty("secondLevel", secondLevelSchema);
		firstLevelSchema.addProperty("paramC", STRING_SCHEMA);

		Schema<Object> expectedSchema = new ObjectSchema();
		expectedSchema.addProperty("firstLevel", firstLevelSchema);
		expectedSchema.addProperty("paramD", STRING_SCHEMA);

		assertThatOperation(customizedOperation)
			.hasParametersCount(1)
			.containsParameterAtIndex(0)
			.withQueryParameterName("filterRequestBody")
			.withNotRequiredStatus()
			.withType(OBJECT_PARAM_TYPE)
			.withSchema(expectedSchema);
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithOnlyOneSpecJsonPathsParameter() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("specWithJsonPathParamTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then

		// {
		//   "specPath": "string"
		// }
		Schema<Object> expectedSchema = new ObjectSchema();
		expectedSchema.addProperty("specPath", STRING_SCHEMA);

		assertThatOperation(customizedOperation)
			.hasParametersCount(1)
			.containsParameterAtIndex(0)
			.withQueryParameterName("filterRequestBody")
			.withNotRequiredStatus()
			.withType(OBJECT_PARAM_TYPE)
			.withSchema(expectedSchema);
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForDisjunction() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("disjunctionTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(3)
			.containsParameterAtIndex(0)
			.withQueryParameterName("disjunctionFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("disjunctionSecondParam")
			.and()
			.containsParameterAtIndex(2)
			.withQueryParameterName("disjunctionThirdParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForConjunction() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("conjunctionTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(3)
			.containsParameterAtIndex(0)
			.withQueryParameterName("conjunctionFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("conjunctionSecondParam")
			.and()
			.containsParameterAtIndex(2)
			.withQueryParameterName("conjunctionThirdParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForAnd() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("andTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withQueryParameterName("andFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("andSecondParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForOr() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("orTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withQueryParameterName("orFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("orSecondParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForSpec() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("specTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(1)
			.containsParameterAtIndex(0)
			.withQueryParameterName("specParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithoutDuplicatingTheSameParameter() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("conjunctionWithDuplicatedParamTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withQueryParameterName("conjunctionDuplicatedFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("conjunctionSecondParam");
	}

	@Test
	public void shouldCorrectlyReadParametersFromCustomFilterWithAnnotations() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodAndParameterType("customFilterWithAnnotationsTestMethod", TestFilterWithAnnotations.class);
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withQueryParameterName("annotatedFilterFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("annotatedFilterSecondParam");
	}

	@Test
	public void shouldCorrectlyReadParametersFromCustomFilterWithoutAnnotations() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodAndParameterType("customFilterWithoutAnnotationsTestMethod", TestFilterWithoutAnnotations.class);
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withQueryParameterName("notAnnotatedFilterFirstParam")
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("notAnnotatedFilterSecondParam");
	}

	@Test
	public void shouldCorrectlyMarkParamAsRequired() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("andWithRequiredParamTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withQueryParameterName("andRequiredParam")
			.withRequiredStatus()
			.and()
			.containsParameterAtIndex(1)
			.withQueryParameterName("andNotRequiredParam")
			.withNotRequiredStatus();
	}

	@Test
	public void shouldCorrectlyMarkHeaderAsRequired() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("specRequiredHeaderTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(1)
			.containsParameterAtIndex(0)
			.withHeaderParameterName("specRequiredHeaderParam")
			.withRequiredStatus();
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithPathVarsParameters() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("andUsingPathVarsTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withPathParameterName("firstPathVarParam")
			.withRequiredStatus()
			.and()
			.containsParameterAtIndex(1)
			.withPathParameterName("secondPathVarParam")
			.withRequiredStatus();
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithHeaderParameters() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("andUsingHeadersTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(2)
			.containsParameterAtIndex(0)
			.withHeaderParameterName("firstHeaderParam")
			.withNotRequiredStatus()
			.and()
			.containsParameterAtIndex(1)
			.withHeaderParameterName("secondHeaderParam")
			.withNotRequiredStatus();
	}

	@Test
	public void shouldNotDuplicateParameters() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("specTestMethod");

		Operation operation = new Operation();

		Parameter specParameter = new Parameter();
		specParameter.setName("specParam");
		specParameter.setSchema(new StringSchema());
		specParameter.setRequired(false);
		specParameter.setIn("query");

		operation.addParametersItem(specParameter);

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		assertThatOperation(customizedOperation)
			.hasParametersCount(1)
			.containsParameterAtIndex(0)
			.withQueryParameterName("specParam");
	}

	private HandlerMethod handlerMethodForControllerMethodWithSpecification(String controllerMethodName) throws NoSuchMethodException {

		return handlerMethodForControllerMethodAndParameterType(controllerMethodName, Specification.class);
	}

	private HandlerMethod handlerMethodForControllerMethodAndParameterType(String controllerMethodName, Class<?> parameterType) throws NoSuchMethodException {
		TestController testController = new TestController();
		Method controllerMethod = testController.getClass().getMethod(controllerMethodName, parameterType);

		return new HandlerMethod(testController, controllerMethod);
	}

	private static class TestController {

		@RequestMapping(value = "/disjunction")
		public void disjunctionTestMethod(
			@Disjunction(
				or = @Spec(path = "", params = "disjunctionFirstParam", spec = Like.class),
				value = @And({
					@Spec(path = "", params = "disjunctionSecondParam", spec = Like.class),
					@Spec(path = "", params = "disjunctionThirdParam", spec = Like.class)
				})) Specification<Object> spec) {

		}

		@RequestMapping(value = "/conjunction")
		public void conjunctionTestMethod(
			@Conjunction(
				and = @Spec(path = "", params = "conjunctionFirstParam", spec = Like.class),
				value = @Or({
					@Spec(path = "", params = "conjunctionSecondParam", spec = Like.class),
					@Spec(path = "", params = "conjunctionThirdParam", spec = Like.class)
				})) Specification<Object> spec) {

		}

		@RequestMapping(value = "/and")
		public void andTestMethod(
			@And({
				@Spec(path = "", params = "andFirstParam", spec = Like.class),
				@Spec(path = "", params = "andSecondParam", spec = Like.class)
			}) Specification<Object> spec) {

		}

		@RequestMapping(value = "/or")
		public void orTestMethod(
			@Or({
				@Spec(path = "", params = "orFirstParam", spec = Like.class),
				@Spec(path = "", params = "orSecondParam", spec = Like.class)
			}) Specification<Object> spec) {

		}

		@RequestMapping(value = "/spec")
		public void specTestMethod(@Spec(path = "", params = "specParam", spec = Like.class) Specification<Object> spec) {

		}

		@RequestMapping(value = "/conjunction-duplicated-param")
		public void conjunctionWithDuplicatedParamTestMethod(
			@Conjunction(
				value = @Or({
					@Spec(path = "", params = "conjunctionDuplicatedFirstParam", spec = Like.class),
					@Spec(path = "", params = "conjunctionSecondParam", spec = Like.class)
				}),
				and = @Spec(path = "", params = "conjunctionDuplicatedFirstParam", spec = Like.class)) Specification<Object> spec) {

		}

		@RequestMapping(value = "/and-required-param", params = "andRequiredParam")
		public void andWithRequiredParamTestMethod(
			@And({
				@Spec(path = "", params = "andRequiredParam", spec = Like.class),
				@Spec(path = "", params = "andNotRequiredParam", spec = Like.class)
			}) Specification<Object> spec) {

		}

		@RequestMapping(value = "/custom-filter-with-annotations")
		public void customFilterWithAnnotationsTestMethod(TestFilterWithAnnotations filter) {

		}

		@RequestMapping(value = "/custom-filter-without-annotations")
		public void customFilterWithoutAnnotationsTestMethod(
			@Or({
				@Spec(path = "", params = "notAnnotatedFilterFirstParam", spec = Like.class),
				@Spec(path = "", params = "notAnnotatedFilterSecondParam", spec = Like.class)
			}) TestFilterWithoutAnnotations filter) {

		}

		@RequestMapping(value = "/and-pathVars")
		public void andUsingPathVarsTestMethod(
			@And({
				@Spec(path = "", pathVars = "firstPathVarParam", spec = Like.class),
				@Spec(path = "", pathVars = "secondPathVarParam", spec = Like.class)
			}) Specification<Object> spec) {

		}

		@RequestMapping(value = "/and-headers")
		public void andUsingHeadersTestMethod(
			@And({
				@Spec(path = "", headers = "firstHeaderParam", spec = Like.class),
				@Spec(path = "", headers = "secondHeaderParam", spec = Like.class)
			}) Specification<Object> spec) {

		}

		@RequestMapping(value = "/disjunction-different-params-locations")
		public void disjunctionWithDifferentParamsLocationTestMethod(
			@Disjunction(
				or = @Spec(path = "", params = "queryParam", spec = Like.class),
				value = @And({
					@Spec(path = "", headers = "headerParam", spec = Like.class),
					@Spec(path = "", pathVars = "pathVarParam", spec = Like.class)
				})) Specification<Object> spec) {

		}

		@RequestMapping(value = "/disjunction-json-paths")
		public void disjunctionWithJsonPathsParamsTestMethod(
			@Disjunction(
				or = @Spec(path = "", jsonPaths = "firstLevel.secondLevel.paramA", spec = Like.class),
				value = @And({
					@Spec(path = "", jsonPaths = "firstLevel.secondLevel.paramB", spec = Like.class),
					@Spec(path = "", jsonPaths = "firstLevel.paramC", spec = Like.class),
					@Spec(path = "", jsonPaths = "paramD", spec = Like.class)
				})) Specification<Object> spec) {

		}

		@RequestMapping(value = "/spec-json-path")
		public void specWithJsonPathParamTestMethod(
			@Spec(path = "", jsonPaths = "specPath", spec = Like.class) Specification<Object> spec) {

		}

		@RequestMapping(value = "/spec-required-header", headers = "specRequiredHeaderParam")
		public void specRequiredHeaderTestMethod(@Spec(path = "", headers = "specRequiredHeaderParam", spec = Like.class) Specification<Object> spec) {

		}

	}

	@Or({
		@Spec(path = "", params = "annotatedFilterFirstParam", spec = Like.class),
		@Spec(path = "", params = "annotatedFilterSecondParam", spec = Like.class)
	})
	private interface TestFilterWithAnnotations extends Specification<Object> {

	}

	private interface TestFilterWithoutAnnotations extends Specification<Object> {

	}

}
