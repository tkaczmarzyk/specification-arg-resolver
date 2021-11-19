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

import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.WithoutTypeConversion;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SimpleSpecificationResolverOnTypeMismatchTest extends ResolverTestBase {

	SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();

	@Test
	public void usesEmptyResultSpecWrapperWhenSpecified() throws Exception {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithOnTypeMismatchConfig"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("thePath")).thenReturn(new String[]{ "theValue" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved)
				.isInstanceOf(EmptyResultOnTypeMismatch.class);

		assertThat(((EmptyResultOnTypeMismatch<?>) resolved).getWrappedSpec()).isEqualTo(new Equal<>(ctx.queryContext(), "thePath", new String[]{ "theValue" }, defaultConverter));
	}

	@Test
	public void doesNotWrapWithEmptyResultSpecWhenSpecificationDoesntRequireDataConversion() throws Exception {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithSpecWithoutDataConversion"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("thePath")).thenReturn(new String[]{ "theValue" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved)
				.isNotInstanceOf(EmptyResultOnTypeMismatch.class)
				.isInstanceOf(SpecWithoutDataConversion.class);
	}

	public static class SpecWithoutDataConversion implements Specification<Object>, WithoutTypeConversion {

		public SpecWithoutDataConversion(QueryContext queryCtx, String path, String[] args) {
		}

		@Override
		public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			return null;
		}
	}

	public static class TestController {

		public void testMethodWithSpecWithoutDataConversion(@Spec(path = "thePath", spec = SpecWithoutDataConversion.class, onTypeMismatch = OnTypeMismatch.EMPTY_RESULT) Specification<Object> spec) {
		}

		;

		public void testMethodWithOnTypeMismatchConfig(@Spec(path = "thePath", spec = Equal.class, onTypeMismatch = OnTypeMismatch.EMPTY_RESULT) Specification<Object> spec) {
		}
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
