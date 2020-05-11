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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Executable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Tomasz Kaczmarzyk
 */
public class WebRequestProcessingContextTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void throwsExceptionIfPathVariableDoesntExist() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestController.class), req);
		
		thrown.expect(InvalidPathVariableRequestedException.class);
		thrown.expectMessage("Requested path variable {notExisting} is not present in Controller request mapping annotations");
		
		context.getPathVariableValue("notExisting");
	}
	
	@Test
	public void resolvesPathVariableValue_requestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_requestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_getMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_getMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", TestControllerWithClassLevelRequestMappingWithValue.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelRequestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_empty", TestControllerWithClassLevelRequestMappingWithPath.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", TestControllerWithClassLevelRequestMappingWithValue.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequestMapingAndMethodLevelGetMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_empty", TestControllerWithClassLevelRequestMappingWithPath.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelRequestMapping_value_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", TestControllerWithClassLevelRequestMappingWithValue.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelRequestMapping_value_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestControllerWithClassLevelRequestMappingWithValue.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelRequestMapping_path_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", TestControllerWithClassLevelRequestMappingWithPath.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelRequestMapping_path_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestControllerWithClassLevelRequestMappingWithPath.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelGetMapping_value_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", TestControllerWithClassLevelRequestMappingWithValue.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelGetMapping_value_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", TestControllerWithClassLevelRequestMappingWithValue.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelGetMapping_path_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", TestControllerWithClassLevelRequestMappingWithPath.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableFromClassLevelRequesMappingAndMethodLevelGetMapping_path_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", TestControllerWithClassLevelRequestMappingWithPath.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	public static class TestController {

		@RequestMapping(path = "/customers/{customerId}/orders/{orderId}")
        public void testMethodUsingPathVariable_requestMapping_path(Specification<Object> spec) {
        }
    	
    	@RequestMapping(value = "/customers/{customerId}/orders/{orderId}")
        public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
        }
    	
    	@GetMapping(path = "/customers/{customerId}/orders/{orderId}")
        public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
        }
    	
    	@GetMapping(value = "/customers/{customerId}/orders/{orderId}")
        public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
        }
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
    	
    	@RequestMapping(value = "/orders/{orderId}")
        public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
        }
    	
    	@GetMapping(path = "/orders/{orderId}")
        public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
        }
    	
    	@GetMapping(value = "/orders/{orderId}")
        public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
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
    	
    	@RequestMapping(value = "/orders/{orderId}")
        public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
        }
    	
    	@GetMapping(path = "/orders/{orderId}")
        public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
        }
    	
    	@GetMapping(value = "/orders/{orderId}")
        public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
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
