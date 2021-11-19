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
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
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
public class DisjunctionSpecificationResolverTest extends ResolverTestBase {

	DisjunctionSpecificationResolver resolver = new DisjunctionSpecificationResolver(new SimpleSpecificationResolver());

	public static class TestController {

		public void testMethodWithoutExpectedAnnotation(
				@Or({ @Spec(path = "path1", spec = Like.class), @Spec(path = "path2", spec = Like.class) }) Specification<Object> spec) {}

		public void testMethodWithUnexpectedType(
				@Disjunction({
                	@And({ @Spec(path = "path1", spec = Like.class), @Spec(path = "path2", spec = Like.class) }),
                	@And({ @Spec(path = "path3", spec = Like.class), @Spec(path = "path4", spec = Like.class) })
                }) Object nonSpec) {}

        public void testMethod(
                @Disjunction({
                	@And({ @Spec(path = "path1", spec = Like.class), @Spec(path = "path2", spec = Like.class) }),
                	@And({ @Spec(path = "path3", spec = Like.class), @Spec(path = "path4", spec = Like.class) })
                }) Specification<Object> spec) {
        }

        public void testMethod2(
        		@Disjunction(
        				value = { @And({
        					@Spec(path = "path3", spec = Like.class),
        					@Spec(path = "path4", spec = Like.class) })
        				},
        				or = { @Spec(path = "path1", spec = Like.class),
        						@Spec(path = "path2", spec = Like.class) }) Specification<Object> spec) {
        }
    }

	@Test
    public void resolvesWrapperOfAnds() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
        when(req.getParameterValues("path2")).thenReturn(new String[] { "value2" });
        when(req.getParameterValues("path3")).thenReturn(new String[] { "value3" });
        when(req.getParameterValues("path4")).thenReturn(new String[] { "value4" });

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> result = resolver.buildSpecification(ctx, param.getParameterAnnotation(net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction.class));

        Specification<Object> and1 = new Conjunction<>(new Like<>(ctx.queryContext(), "path1", "value1"),
                new Like<>(ctx.queryContext(), "path2", "value2"));
        Specification<Object> and2 = new Conjunction<>(new Like<>(ctx.queryContext(), "path3", "value3"),
                new Like<>(ctx.queryContext(), "path4", "value4"));

        assertThat(result).isEqualTo(new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(and1, and2));
    }

    @Test
    public void resolvesWrapperOfSimpleSpecsAndAnds() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
        when(req.getParameterValues("path2")).thenReturn(new String[] { "value2" });
        when(req.getParameterValues("path3")).thenReturn(new String[] { "value3" });
        when(req.getParameterValues("path4")).thenReturn(new String[] { "value4" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> result = resolver.buildSpecification(ctx, param.getParameterAnnotation(net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction.class));

        Specification<Object> and = new Conjunction<>(new Like<>(queryCtx, "path3", "value3"),
                new Like<>(queryCtx, "path4", "value4"));

        assertThat(result).isEqualTo(new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(and, new Like<>(queryCtx, "path1", "value1"),
                new Like<>(queryCtx, "path2", "value2")));
    }

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
