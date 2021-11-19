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

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Tomasz Kaczmarzyk
 */
public class AndSpecificationResolverTest extends ResolverTestBase {

	AndSpecificationResolver resolver = new AndSpecificationResolver(new SimpleSpecificationResolver());

	@Test
	public void resolvesWrapperOfInnerSpecs() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("path1")).thenReturn(new String[]{"value1"});
		when(req.getParameterValues("path2")).thenReturn(new String[]{"value2"});

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> result = resolver.buildSpecification(ctx, param.getParameterAnnotation(And.class));

		assertThat(result).isEqualTo(new Conjunction<>(new Like<>(ctx.queryContext(), "path1", "value1"),
				new Like<>(ctx.queryContext(), "path2", "value2")));
	}

	@Test
	public void skipsMissingInnerSpec() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("path1")).thenReturn(new String[]{"value1"});

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> result = resolver.buildSpecification(ctx, param.getParameterAnnotation(And.class));

		assertThat(result).isEqualTo(new Conjunction<>(new Like<>(ctx.queryContext(), "path1", "value1")));
	}

	@Test
	public void returnsNullIfNoInnerSpecCanBeResolved() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> result = resolver.buildSpecification(ctx, param.getParameterAnnotation(And.class));

		assertThat(result).isNull();
	}

	public static class TestController {

		public void testMethod(
				@And({@Spec(path = "path1", spec = Like.class), @Spec(path = "path2", spec = Like.class)}) Specification<Object> spec) {
		}
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
