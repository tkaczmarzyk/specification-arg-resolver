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
package net.kaczmarzyk.spring.data.jpa.web;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Executable;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class WebRequestProcessingContextPathVariableResolverTest {

	public static Stream<Class<?>> testControllers() {
		return Stream.of(
				TestControllerWithClassLevelRequestMappingWithValue.class,
				TestControllerWithClassLevelRequestMappingWithValueAndPathVarWithRegexp.class,
				TestControllerWithClassLevelRequestMappingWithPath.class,
				TestControllerWithClassLevelRequestMappingWithPathAndPathVarWithRegexp.class
		);
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMapping_empty(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMapping_empty(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelRequestMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelRequestMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMapping_value(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableFromClassLevelRequestMappingAndMethodLevelGetMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@ParameterizedTest
	@MethodSource("testControllers")
	public void resolvesPathVariableWithRegexpFromClassLevelRequestMappingAndMethodLevelGetMapping_path(Class<?> testController) {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", testController), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@RequestMapping("/customers/{customerId}")
	public static class TestControllerWithClassLevelRequestMappingWithValue {
		
		@RequestMapping
		public void testMethodUsingPathVariable_requestMapping_empty(Specification<Object> spec) {
		}
		
		@GetMapping
		public void testMethodUsingPathVariable_getMapping_empty(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId:.*}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_value(Specification<Object> spec) {
		}
	}
	
	@RequestMapping("/customers/{customerId:.*}")
	public static class TestControllerWithClassLevelRequestMappingWithValueAndPathVarWithRegexp {
		
		@RequestMapping
		public void testMethodUsingPathVariable_requestMapping_empty(Specification<Object> spec) {
		}
		
		@GetMapping
		public void testMethodUsingPathVariable_getMapping_empty(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId:.*}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_value(Specification<Object> spec) {
		}
	}
	
	@RequestMapping(path = "/customers/{customerId}")
	public static class TestControllerWithClassLevelRequestMappingWithPath {
		
		@RequestMapping
		public void testMethodUsingPathVariable_requestMapping_empty(Specification<Object> spec) {
		}
		
		@GetMapping
		public void testMethodUsingPathVariable_getMapping_empty(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId:.*}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_value(Specification<Object> spec) {
		}
	}
	
	@RequestMapping(path = "/customers/{customerId:[0-9]+}")
	public static class TestControllerWithClassLevelRequestMappingWithPathAndPathVarWithRegexp {
		
		@RequestMapping
		public void testMethodUsingPathVariable_requestMapping_empty(Specification<Object> spec) {
		}
		
		@GetMapping
		public void testMethodUsingPathVariable_getMapping_empty(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/orders/{orderId:.*}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_value(Specification<Object> spec) {
		}
	}
	
	private MethodParameter testMethodParameter(String methodName, Class<?> controllerClass) {
		return MethodParameter.forExecutable(testMethod(methodName, controllerClass, Specification.class), 0);
	}
	
	private Executable testMethod(String methodName, Class<?> controllerClass, Class<?> specClass) {
		try {
			return controllerClass.getMethod(methodName, specClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
