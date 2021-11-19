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
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.RepeatedJoinFetch;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static javax.persistence.criteria.JoinType.LEFT;
import static javax.persistence.criteria.JoinType.RIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Jakub Radlica
 */
public class RepeatedJoinFetchResolverTest extends ResolverTestBase {

	private final RepeatedJoinFetchResolver resolver = new RepeatedJoinFetchResolver();

	@Test
	public void resolvesRepeatedJoinFetch() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		QueryContext queryCtx = new WebRequestQueryContext(req);

		Specification<?> result = resolver.buildSpecification(ctx, param.getParameterAnnotation(RepeatedJoinFetch.class));

		assertThat(result).isEqualTo(
				new Conjunction<>(
						new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{"path1"}, LEFT, true),
						new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{"path2"}, RIGHT, false)
				)
		);
	}

	private static class TestController {

		public void testMethod(
				@JoinFetch(paths = {"path1"}, joinType = LEFT, distinct = true)
				@JoinFetch(paths = {"path2"}, joinType = RIGHT, distinct = false) Specification<Object> spec) {
		}
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
