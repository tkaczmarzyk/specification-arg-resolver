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
import static org.mockito.Mockito.mock;

import javax.persistence.criteria.JoinType;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;


public class JoinSpecificationResolverTest extends ResolverTestBase {

	SpecificationArgumentResolver resolver = new SpecificationArgumentResolver();

    @Test
    public void resolvesJoin() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved).isEqualTo(
        		new net.kaczmarzyk.spring.data.jpa.domain.Join<>(new WebRequestQueryContext(req), "orders", "o", JoinType.RIGHT, false));
    }
    
    public static class TestController {

        public void testMethod1(@Join(path = "orders", alias = "o", type = JoinType.RIGHT, distinct = false) Specification<Object> spec) {
        }
    }

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
