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

import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SimpleSpecificationResolverConstValueTest extends ResolverTestBase {
	
	public SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();
	
	@Test
	public void returnsSpecificationWithConstValue() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithConstValue"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "constValue"));
	}
	
	@Test
	public void returnsSpecificationWithRawSpELConstValueIfSpecificationArgumentResolverIsMisconfigured() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithConstValueInSpEL"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "#{'Te'.concat('st')}"));
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
	
	public static class TestController {
		
		public void testMethodWithConstValue(@Spec(path = "thePath", spec = Equal.class, constVal = "constValue") Specification<Object> spec) {
		}
		
		public void testMethodWithConstValueInSpEL(@Spec(path = "thePath", spec = Equal.class, constVal = "#{'Te'.concat('st')}") Specification<Object> spec) {
		}
	}
	
	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
