/**
 * Copyright 2014-2022 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyGenerationStrategyTest extends ResolverTestBase {

    SpecificationArgumentResolver resolver = new SpecificationArgumentResolver();

    @Test
    public void proxyShouldNotBeGeneratedWhenMethodParameterIsAnnotatedWithSingleSpecification() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_singleInnerSpec"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new DefaultQueryContext();
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThatSpecIsNotProxy(resolved);

        assertThat(resolved)
                .isEqualTo(new Like<>(queryCtx, "path1", new String[] { "value1" }));
    }

    @Test
    public void proxyShouldNotBeGeneratedWhenMethodParameterIsAnnotatedWithMultipleSpecifications() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_multipleInnerSpecs"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new DefaultQueryContext();
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThatSpecIsNotProxy(resolved);

        assertThat(innerSpecs(resolved))
                .hasSize(2)
                .contains(new net.kaczmarzyk.spring.data.jpa.domain.Join<>(queryCtx, "join1", "alias1", JoinType.LEFT, true))
                .contains(new Like<>(queryCtx, "path1", new String[]{"value1"}));
    }

    @Test
    public void proxyShouldNotBeGeneratedWhenMethodParameterIsAnnotatedWithConjunctionWithMultipleInnerSpecs() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_conjunctionWithMultipleInnerSpecs"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new DefaultQueryContext();
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
        when(req.getParameterValues("path2")).thenReturn(new String[] { "value2" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThatSpecIsNotProxy(resolved);

        List<Specification<?>> resolvedDisjunction = ReflectionUtils.get(resolved, "innerSpecs");

        assertThat(innerSpecs(resolvedDisjunction.get(0)))
                .hasSize(2)
                .contains(
                        new Like<>(queryCtx, "path1", new String[] { "value1" }),
                        new Like<>(queryCtx, "path2", new String[] { "value2" })
                );
    }

    @Test
    public void proxyShouldBeGeneratedWhenMethodParameterIsInterface_isAnnotatedWithMultipleInnerSpecs() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_interfaceSpecificationAsMethodParameter", CustomSpecification.class), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new DefaultQueryContext();
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
        when(req.getParameterValues("path2")).thenReturn(new String[] { "value2" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThatSpecIsProxy(resolved);

        assertThat(proxiedInnerSpecs(resolved))
                .hasSize(2)
                .contains(new net.kaczmarzyk.spring.data.jpa.domain.Join<>(queryCtx, "join1", "alias1", JoinType.LEFT, true))
                .contains(new Like<>(queryCtx, "path1", new String[]{ "value1" }));
    }

    @Override
    protected Class<?> controllerClass() {
        return TestController.class;
    }

    @Join(path = "join1", alias = "alias1", type = JoinType.LEFT)
    @Spec(path = "path1", spec = Like.class)
    public interface CustomSpecification extends Specification<Object> {}

    public static class TestController {

        public void testMethod_singleInnerSpec(
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }

        public void testMethod_multipleInnerSpecs(
                @Join(path = "join1", alias = "alias1", type = JoinType.LEFT, distinct = true)
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }

        public void testMethod_conjunctionWithMultipleInnerSpecs(
                @Conjunction(
                        @Or({
                                @Spec(path = "path1", spec = Like.class),
                                @Spec(path = "path2", spec = Like.class)
                        })) Specification<Object> spec) {
        }

        public void testMethod_interfaceSpecificationAsMethodParameter(
                CustomSpecification spec) {
        }

    }

}
