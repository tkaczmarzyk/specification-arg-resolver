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
package net.kaczmarzyk.spring.data.jpa.web;

<<<<<<< HEAD
import net.kaczmarzyk.utils.ReflectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
=======
import org.junit.Test;
>>>>>>> 70ead54ebfbe10bdecf257ef152a66e17319b6f6
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Executable;
import java.util.HashMap;

import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.setPathVariablesInRequestAttributes;
import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.pathVariables;
import static net.kaczmarzyk.spring.data.jpa.web.utils.RequestAttributesWithPathVariablesUtil.entry;
import static org.assertj.core.api.Assertions.assertThat;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

=======
>>>>>>> 70ead54ebfbe10bdecf257ef152a66e17319b6f6
/**
 * @author Tomasz Kaczmarzyk
 */
public class WebRequestProcessingContextTest {
	
	@Test
	public void returnEmptyValueIfPathVariableDoesntExist() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("notExisting")).isEqualTo("");
	}

	@Test
	public void resolvesPathVariableValue_requestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValue_requestMapping_multi_paths_first() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_multi_paths", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("employeeId")).isEqualTo("");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_requestMapping_multi_paths_Second() {
		MockWebRequest req = new MockWebRequest("/employees/777/orders/99");
		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_multi_paths", TestController.class), req);
		
		assertThat(context.getPathVariableValue("employeeId")).isEqualTo("777");
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValueWithRegexp_requestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_requestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValueWithRegexp_requestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_getMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValueWithRegexp_getMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValue_getMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}
	
	@Test
	public void resolvesPathVariableValueWithRegexp_getMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", TestController.class), req);
		
		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}


	@Test
	public void resolvesPathVariableValueUsingFallbackMethod_requestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_path", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueWithRegexpUsingFallbackMethod_requestMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_path", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueUsingFallbackMethod_requestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_requestMapping_value", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueWithRegexpUsingFallbackMethod_requestMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_requestMapping_value", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueUsingFallbackMethod_getMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_path", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueWithRegexpUsingFallbackMethod_getMapping_path() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_path", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueUsingFallbackMethod_getMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariable_getMapping_value", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test
	public void resolvesPathVariableValueWithRegexpUsingFallbackMethod_getMapping_value() {
		MockWebRequest req = new MockWebRequest("/customers/888/orders/99");
		setPathVariablesInRequestAttributes(req, pathVariables(entry("customerId", "888"), entry("orderId", "99")));

		WebRequestProcessingContext context = new WebRequestProcessingContext(
				testMethodParameter("testMethodUsingPathVariableWithRegexp_getMapping_value", TestController.class), req);

		assertThat(context.getPathVariableValue("customerId")).isEqualTo("888");
		assertThat(context.getPathVariableValue("orderId")).isEqualTo("99");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionWhenContentTypeIsDifferentThanJson() {
		NativeWebRequest req = mock(NativeWebRequest.class);

		when(req.getHeader(CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_PDF.toString());

		WebRequestProcessingContext context = new WebRequestProcessingContext(null, req);

		context.getBodyParamValues("example");
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionWhenNativeRequestIsNull() {
		NativeWebRequest req = mock(NativeWebRequest.class);

		when(req.getHeader(CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_JSON_VALUE);
		when(req.getNativeRequest(any())).thenReturn(null);

		WebRequestProcessingContext context = new WebRequestProcessingContext(null, req);

		context.getBodyParamValues("example");
	}

	@Test(expected = RuntimeException.class)
	public void shouldThrowRuntimeExceptionWhenReadingInvalidRequestBody() throws IOException {
		NativeWebRequest req = mock(NativeWebRequest.class);
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

		when(req.getHeader(CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_JSON_VALUE);
		when(req.getNativeRequest(any())).thenReturn(httpServletRequest);
		when(httpServletRequest.getInputStream()).thenThrow(new IOException());

		WebRequestProcessingContext context = new WebRequestProcessingContext(null, req);

		context.getBodyParamValues("example");
	}
	
	public static class TestController {
		
		@RequestMapping(path = "/customers/{customerId}/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_path(Specification<Object> spec) {
		}

		@RequestMapping(path = {"/customers/{customerId}/orders/{orderId}", "/employees/{employeeId}/orders/{orderId}"})
		public void testMethodUsingPathVariable_requestMapping_multi_paths(Specification<Object> spec) {
		}
		
		@RequestMapping(path = "/customers/{customerId:.*}/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_path(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/customers/{customerId}/orders/{orderId}")
		public void testMethodUsingPathVariable_requestMapping_value(Specification<Object> spec) {
		}
		
		@RequestMapping(value = "/customers/{customerId:[0-9]+}/orders/{orderId:.*}")
		public void testMethodUsingPathVariableWithRegexp_requestMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/customers/{customerId}/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(path = "/customers/{customerId:.*}/orders/{orderId:[0-9]+}")
		public void testMethodUsingPathVariableWithRegexp_getMapping_path(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/customers/{customerId}/orders/{orderId}")
		public void testMethodUsingPathVariable_getMapping_value(Specification<Object> spec) {
		}
		
		@GetMapping(value = "/customers/{customerId:[0-9]+}/orders/{orderId:.*}")
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
