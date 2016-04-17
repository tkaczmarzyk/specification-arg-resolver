/**
 * Copyright 2014-2016 the original author or authors.
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

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

public class SimpleSpecificationResolverSpecConstructorTest extends ResolverTestBase {

	SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();

	public static class SpecWith2ArgConstructor extends DummySpec {
		String path;
		String[] args;

		public SpecWith2ArgConstructor(String path, String[] args) {
			this.path = path;
			this.args = args;
		}
	}

	public static class SpecWith3ArgConstructor extends SpecWith2ArgConstructor {
		Converter converter;

		public SpecWith3ArgConstructor(String path, String[] args, Converter converter) {
			super(path, args);
			this.converter = converter;
		}
	}
	
	public static class SpecWithLegacy3ArgConstructor extends SpecWith2ArgConstructor {
		String[] config;

		public SpecWithLegacy3ArgConstructor(String path, String[] args, String[] config) {
			super(path, args);
			this.config = config;
		}
	}

	public static class SpecWith4ArgConstructor extends SpecWith3ArgConstructor {
		String[] config;

		public SpecWith4ArgConstructor(String path, String[] args, Converter converter, String[] config) {
			super(path, args, converter);
			this.config = config;
		}
	}

	@Test
	public void resolves2ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWith2argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		SpecWith2ArgConstructor resolved = (SpecWith2ArgConstructor) resolver.resolveArgument(param, null, req, null);

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
	}

	@Test
	public void resolves3ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWith3argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		SpecWith3ArgConstructor resolved = (SpecWith3ArgConstructor) resolver.resolveArgument(param, null, req, null);

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
		assertThat(resolved.converter).isEqualTo(Converter.withTypeMismatchBehaviour(OnTypeMismatch.EXCEPTION));
	}
	
	@Test
	public void resolvesLegacy3ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithLegacy3argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		SpecWithLegacy3ArgConstructor resolved = (SpecWithLegacy3ArgConstructor) resolver.resolveArgument(param, null, req, null);

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
		assertThat(resolved.config).isEqualTo(new String[] { "yyyyMMdd" });
	}

	@Test
	public void resolves4ArgsSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWith4argSpec"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

		SpecWith4ArgConstructor resolved = (SpecWith4ArgConstructor) resolver.resolveArgument(param, null, req, null);

		assertThat(resolved.path).isEqualTo("thePath");
		assertThat(resolved.args).isEqualTo(new String[] { "theValue" });
		assertThat(resolved.converter).isEqualTo(Converter.withDateFormat("yyyyMMdd", OnTypeMismatch.EXCEPTION));
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

		public void methodWith2argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWith2ArgConstructor.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void methodWith3argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWith3ArgConstructor.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}
		
		public void methodWithLegacy3argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWithLegacy3ArgConstructor.class, config = "yyyyMMdd", onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void methodWith4argSpec(
				@Spec(path = "thePath", params = "theParameter", spec = SpecWith4ArgConstructor.class, config = "yyyyMMdd", onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}
	}
}
