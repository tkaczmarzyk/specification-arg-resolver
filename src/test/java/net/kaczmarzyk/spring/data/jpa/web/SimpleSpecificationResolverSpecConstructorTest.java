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


import net.kaczmarzyk.spring.data.jpa.utils.Converter;
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

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Tomasz Kaczmarzyk
 */
public class SimpleSpecificationResolverSpecConstructorTest extends ResolverTestBase {

	SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();

	public static class SpecWith3ArgConstructor extends DummySpec {
		QueryContext queryCtx;
		String path;
		String[] args;

		public SpecWith3ArgConstructor(QueryContext queryCtx, String path, String[] args) {
			this.queryCtx = queryCtx;
			this.path = path;
			this.args = args;
		}
	}

	public static class SpecWith4ArgConstructor extends SpecWith3ArgConstructor {
		Converter converter;

		public SpecWith4ArgConstructor(QueryContext queryCtx, String path, String[] args, Converter converter) {
			super(queryCtx, path, args);
			this.converter = converter;
		}
	}

	public static class SpecWithLegacy3ArgConstructor extends DummySpec {
		String path;
		String[] args;
		String[] config;

		public SpecWithLegacy3ArgConstructor(String path, String[] args, String[] config) {
			this.path = path;
			this.args = args;
			this.config = config;
		}
	}

	public static class SpecWith5ArgConstructor extends SpecWith4ArgConstructor {
		String[] config;

		public SpecWith5ArgConstructor(QueryContext queryContext, String path, String[] args, Converter converter, String[] config) {
			super(queryContext, path, args, converter);
			this.config = config;
		}
	}

	@Test
	public void resolves3ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forExecutable(testMethod("methodWith3argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		SpecWith3ArgConstructor resolved = (SpecWith3ArgConstructor) resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
	}

	@Test
	public void resolves4ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forExecutable(testMethod("methodWith4argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		SpecWith4ArgConstructor resolved = (SpecWith4ArgConstructor) resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
		assertThat(resolved.converter).isEqualTo(Converter.withTypeMismatchBehaviour(OnTypeMismatch.EXCEPTION, null));
	}

	@Test
	public void resolvesLegacy3ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forExecutable(testMethod("methodWithLegacy3argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		SpecWithLegacy3ArgConstructor resolved = (SpecWithLegacy3ArgConstructor) resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
		assertThat(resolved.config).isEqualTo(new String[] { "yyyyMMdd" });
	}

	@Test
	public void resolves5ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forExecutable(testMethod("methodWith5argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		SpecWith5ArgConstructor resolved = (SpecWith5ArgConstructor) resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
		assertThat(resolved.converter).isEqualTo(Converter.withDateFormat("yyyyMMdd", OnTypeMismatch.EXCEPTION, null));
		assertThat(resolved.config).isEqualTo(new String[] { "yyyyMMdd" });
	}

	public static class DummySpec implements Specification<Object> {
		@Override
		public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			return null;
		}
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}

	public static class TestController {

		public void methodWith3argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWith3ArgConstructor.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void methodWith4argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWith4ArgConstructor.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void methodWithLegacy3argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWithLegacy3ArgConstructor.class, config = "yyyyMMdd", onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void methodWith5argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWith5ArgConstructor.class, config = "yyyyMMdd", onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}
	}
}
