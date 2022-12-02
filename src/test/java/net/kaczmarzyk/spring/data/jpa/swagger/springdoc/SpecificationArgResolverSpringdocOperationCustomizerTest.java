package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationArgResolverSpringdocOperationCustomizerTest {

	private SpecificationArgResolverSpringdocOperationCustomizer springdocOperationCustomizer;

	@BeforeEach
	public void initializeCustomizer() {
		springdocOperationCustomizer = new SpecificationArgResolverSpringdocOperationCustomizer();
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForDisjunction() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("disjunctionTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("disjunctionFirstParam", "disjunctionSecondParam", "disjunctionThirdParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForConjunction() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("conjunctionTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("conjunctionFirstParam", "conjunctionSecondParam", "conjunctionThirdParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForAnd() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("andTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("andFirstParam", "andSecondParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForOr() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("orTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("orFirstParam", "orSecondParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithSpecParametersForSpec() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("specTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("specParam");
	}

	@Test
	public void shouldCorrectlyEnrichOperationWithoutDuplicatingTheSameParameter() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("conjunctionWithDuplicatedParamTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());


		Assertions.assertThat(parameterNames)
				.containsOnly("conjunctionDuplicatedFirstParam", "conjunctionSecondParam");
	}

	@Test
	public void shouldCorrectlyReadParametersFromCustomFilterWithAnnotations() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodAndParameterType("customFilterWithAnnotationsTestMethod", TestFilterWithAnnotations.class);
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("annotatedFilterFirstParam", "annotatedFilterSecondParam");
	}

	@Test
	public void shouldCorrectlyReadParametersFromCustomFilterWithoutAnnotations() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodAndParameterType("customFilterWithoutAnnotationsTestMethod", TestFilterWithoutAnnotations.class);
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<String> parameterNames = customizedOperation.getParameters().stream()
				.map(Parameter::getName)
				.collect(toList());

		Assertions.assertThat(parameterNames)
				.containsOnly("notAnnotatedFilterFirstParam", "notAnnotatedFilterSecondParam");
	}

	@Test
	public void shouldCorrectlyMarkParameterAsRequired() throws NoSuchMethodException {
		// given
		HandlerMethod handlerMethod = handlerMethodForControllerMethodWithSpecification("andWithRequiredParamTestMethod");
		Operation operation = new Operation();

		// when
		Operation customizedOperation = springdocOperationCustomizer.customize(operation, handlerMethod);

		// then
		List<Parameter> parameters = customizedOperation.getParameters();

		Assertions.assertThat(parameters)
				.hasSize(2);

		Assertions.assertThat(parameters.get(0).getName())
				.isEqualTo("andRequiredParam");
		Assertions.assertThat(parameters.get(0).getRequired())
				.isTrue();

		Assertions.assertThat(parameters.get(1).getName())
				.isEqualTo("andNotRequiredParam");
		Assertions.assertThat(parameters.get(1).getRequired())
				.isFalse();
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
						value = @And({
								@Spec(path = "", params = "disjunctionFirstParam", spec = Like.class),
								@Spec(path = "", params = "disjunctionSecondParam", spec = Like.class)
						}),
						or = @Spec(path = "", params = "disjunctionThirdParam", spec = Like.class)) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/conjunction")
		public void conjunctionTestMethod(
				@Conjunction(
						value = @Or({
								@Spec(path = "", params = "conjunctionFirstParam", spec = Like.class),
								@Spec(path = "", params = "conjunctionSecondParam", spec = Like.class)
						}),
						and = @Spec(path = "", params = "conjunctionThirdParam", spec = Like.class)) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/and")
		public void andTestMethod(
				@And({
						@Spec(path = "", params = "andFirstParam", spec = Like.class),
						@Spec(path = "", params = "andSecondParam", spec = Like.class)
				}) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/or")
		public void orTestMethod(
				@Or({
						@Spec(path = "", params = "orFirstParam", spec = Like.class),
						@Spec(path = "", params = "orSecondParam", spec = Like.class)
				}) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/spec")
		public void specTestMethod(@Spec(path = "", params = "specParam", spec = Like.class) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/conjunction-duplicated-param")
		public void conjunctionWithDuplicatedParamTestMethod(
				@Conjunction(
						value = @Or({
								@Spec(path = "", params = "conjunctionDuplicatedFirstParam", spec = Like.class),
								@Spec(path = "", params = "conjunctionSecondParam", spec = Like.class)
						}),
						and = @Spec(path = "", params = "conjunctionDuplicatedFirstParam", spec = Like.class)) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/and-required-param", params = "andRequiredParam")
		public void andWithRequiredParamTestMethod(
				@And({
						@Spec(path = "", params = "andRequiredParam", spec = Like.class),
						@Spec(path = "", params = "andNotRequiredParam", spec = Like.class)
				}) Specification<Customer> spec) {

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

	}

	@Or({
			@Spec(path = "", params = "annotatedFilterFirstParam", spec = Like.class),
			@Spec(path = "", params = "annotatedFilterSecondParam", spec = Like.class)
	})
	private interface TestFilterWithAnnotations extends Specification<Customer> {

	}

	private interface TestFilterWithoutAnnotations extends Specification<Customer> {

	}

}
