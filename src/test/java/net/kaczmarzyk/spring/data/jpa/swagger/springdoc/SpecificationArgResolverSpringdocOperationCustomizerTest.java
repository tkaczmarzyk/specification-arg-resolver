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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
		HandlerMethod handlerMethod = handlerMethodForControllerMethod("disjunctionTestMethod");
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
		HandlerMethod handlerMethod = handlerMethodForControllerMethod("conjunctionTestMethod");
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
		HandlerMethod handlerMethod = handlerMethodForControllerMethod("andTestMethod");
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
		HandlerMethod handlerMethod = handlerMethodForControllerMethod("orTestMethod");
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

	private HandlerMethod handlerMethodForControllerMethod(String controllerMethodName) throws NoSuchMethodException {
		TestController testController = new TestController();
		Method disjunctionTestMethod = testController.getClass().getMethod(controllerMethodName, Specification.class);

		return new HandlerMethod(testController, disjunctionTestMethod);
	}

	@Controller
	private static class TestController {

		@RequestMapping(value = "/disjunction")
		@ResponseBody
		public void disjunctionTestMethod(
				@Disjunction(
						value = @And({
								@Spec(path = "disjunctionFirstParam", params = "disjunctionFirstParam", spec = Like.class),
								@Spec(path = "disjunctionSecondParam", params = "disjunctionSecondParam", spec = Like.class)
						}),
						or = @Spec(path = "disjunctionThirdParam", params = "disjunctionThirdParam", spec = Like.class)) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/conjunction")
		@ResponseBody
		public void conjunctionTestMethod(
				@Conjunction(
						value = @Or({
								@Spec(path = "conjunctionFirstParam", params = "conjunctionFirstParam", spec = Like.class),
								@Spec(path = "conjunctionSecondParam", params = "conjunctionSecondParam", spec = Like.class)
						}),
						and = @Spec(path = "conjunctionThirdParam", params = "conjunctionThirdParam", spec = Like.class)) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/and")
		@ResponseBody
		public void andTestMethod(
				@And({
						@Spec(path = "andFirstParam", params = "andFirstParam", spec = Like.class),
						@Spec(path = "andSecondParam", params = "andSecondParam", spec = Like.class)
				}) Specification<Customer> spec) {

		}

		@RequestMapping(value = "/or")
		@ResponseBody
		public void orTestMethod(
				@Or({
						@Spec(path = "orFirstParam", params = "orFirstParam", spec = Like.class),
						@Spec(path = "orSecondParam", params = "orSecondParam", spec = Like.class)
				}) Specification<Customer> spec) {

		}

	}

}
