/**
 * Copyright 2014-2019 the original author or authors.
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
import static org.mockito.Mockito.when;
import net.kaczmarzyk.spring.data.jpa.domain.Disjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;


/**
 * @author Tomasz Kaczmarzyk
 */
public class ConjunctionSpecificationResolverTest extends ResolverTestBase {

	ConjunctionSpecificationResolver resolver = new ConjunctionSpecificationResolver();
	
	public static class TestController {

        public void testMethod(
                @Conjunction({
                	@Or({ @Spec(path = "path1", spec = Like.class), @Spec(path = "path2", spec = Like.class) }),
                	@Or({ @Spec(path = "path3", spec = Like.class), @Spec(path = "path4", spec = Like.class) })
                }) Specification<Object> spec) {
        }

        public void testMethod2(
        		@Conjunction(
        				value = { @Or({
        					@Spec(path = "path3", spec = Like.class),
        					@Spec(path = "path4", spec = Like.class) })
        				},
        				and = { @Spec(path = "path1", spec = Like.class),
        						@Spec(path = "path2", spec = Like.class) }) Specification<Object> spec) {
        }
    }
	
	@Test
    public void resolvesWrapperOfOrs() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
        when(req.getParameterValues("path2")).thenReturn(new String[] { "value2" });
        when(req.getParameterValues("path3")).thenReturn(new String[] { "value3" });
        when(req.getParameterValues("path4")).thenReturn(new String[] { "value4" });

        Specification<?> result = resolver.resolveArgument(param, null, req, null);

        Specification<Object> or1 = new Disjunction<>(new Like<>(queryCtx, "path1", "value1"),
                new Like<>(queryCtx, "path2", "value2"));
        Specification<Object> or2 = new Disjunction<>(new Like<>(queryCtx, "path3", "value3"),
                new Like<>(queryCtx, "path4", "value4"));

        assertThat(result).isEqualTo(new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(or1, or2));
    }

    @Test
    public void resolvesWrapperOfSimpleSpecsAndOrs() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
        when(req.getParameterValues("path2")).thenReturn(new String[] { "value2" });
        when(req.getParameterValues("path3")).thenReturn(new String[] { "value3" });
        when(req.getParameterValues("path4")).thenReturn(new String[] { "value4" });

        Specification<?> result = resolver.resolveArgument(param, null, req, null);

        Specification<Object> or = new Disjunction<>(new Like<>(queryCtx, "path3", "value3"),
                new Like<>(queryCtx, "path4", "value4"));

        assertThat(result).isEqualTo(new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(or, new Like<>(queryCtx, "path1", "value1"),
                new Like<>(queryCtx, "path2", "value2")));
    }

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
