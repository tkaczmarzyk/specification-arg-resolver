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

import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithSARConfiguredWithApplicationContext;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Jakub Radlica
 */
public class SimpleSpecificationResolverDefaultValueIntegrationTest extends IntegrationTestBaseWithSARConfiguredWithApplicationContext {
	
	private Converter defaultConverter = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);
	
	@Autowired
	AbstractApplicationContext abstractApplicationContext;
	
	SimpleSpecificationResolver resolver;
	
	@BeforeEach
	public void initializeResolver() {
		this.resolver = new SimpleSpecificationResolver(null, abstractApplicationContext);
	}
	
	@Test
	public void returnsSpecificationWithDefaultValue() {
		MethodParameter param = methodParameter("testMethodWithDefaultValue", Specification.class);
		
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "defaultValue"));
	}
	
	@Test
	public void returnsSpecificationWithDefaultValueInSpEL() {
		MethodParameter param = methodParameter("testMethodWithDefaultValueInSpEL", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "defaultPropertyValue"));
	}

	@Test
	public void returnsSpecificationWithRawDefaultValueIfValueInSpELAttributeIsSetToFalse() {
		MethodParameter param = methodParameter("testMethodWithRawSpELDefaultValue", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "#{'${SpEL-support.defaultVal.value}'.concat('ue')}"));
	}
	
	@Test
	public void throwsIllegalArgumentExceptionWhenTryingToResolveDefaultValueWithInvalidSpELSyntax() {
		MethodParameter param = methodParameter("testMethodWithDefaultValueInInvalidSpELSyntax", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		ThrowableAssertions.assertThrows(
				IllegalArgumentException.class,
				() -> resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class)),
				"Invalid SpEL expression: '#{${SpEL-support.defaultVal.value}.concat('test')}'"
		);
	}
	
	private EmptyResultOnTypeMismatch<?> equalWithPathAndExpectedValue(WebRequestProcessingContext ctx, String path, String expectedValue) {
		return new EmptyResultOnTypeMismatch<>(
				new Equal<>(
						ctx.queryContext(),
						path,
						new String[]{ expectedValue },
						defaultConverter
				)
		);
	}
	
	 private static class TestController {
		
		 public void testMethodWithDefaultValue(@Spec(path = "thePath", params = "path", spec = Equal.class, defaultVal = "defaultValue") Specification<Object> spec) {
		 }
		 
		public void testMethodWithDefaultValueInSpEL(
				@Spec(path = "thePath", spec = Equal.class, defaultVal = "#{'${SpEL-support.defaultVal.value}'.concat('ue')}", valueInSpEL = true) Specification<Object> spec) {
		}

		 public void testMethodWithRawSpELDefaultValue(
				 @Spec(path = "thePath", spec = Equal.class, defaultVal = "#{'${SpEL-support.defaultVal.value}'.concat('ue')}", valueInSpEL = false) Specification<Object> spec) {
		 }

		 public void testMethodWithDefaultValueInInvalidSpELSyntax(
				 @Spec(path = "thePath", spec = Equal.class, defaultVal = "#{${SpEL-support.defaultVal.value}.concat('test')}", valueInSpEL = true) Specification<Object> spec) {
		 }
	}
	
	protected MethodParameter methodParameter(String methodName, Class<?> specClass) {
		try {
			return MethodParameter.forExecutable(
					TestController.class.getMethod(methodName, specClass),
					0
			);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
