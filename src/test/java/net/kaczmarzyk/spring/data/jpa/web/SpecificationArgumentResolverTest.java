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
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Joins;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.JoinType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SpecificationArgumentResolverTest extends ResolverTestBase {

    SpecificationArgumentResolver resolver = new SpecificationArgumentResolver();
    
    @Test
    public void resolvesJoinFetchForSimpleSpec() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
            .hasSize(2)
            .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
            .contains(new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch1", "fetch2" }, JoinType.LEFT, true));
    }
    
    @Test
    public void resolvesRepeatedFetchJoins() throws Exception {
    	MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_repeatedFetch"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
            .hasSize(2)
            .contains(new Like<>(queryCtx, "path1", new String[]{ "value1" }))
            .contains(new Conjunction<>(
                    new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{ "fetch1" }, JoinType.LEFT, true),
                    new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{ "fetch2" }, JoinType.INNER, true)));
    }
    
    @Test
    public void resolvesJoinContainerWithJoinFetch() throws Exception {
    	MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_joinContainerWithJoinFetch"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
            .hasSize(2)
            .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
            .contains(new Conjunction<Object>(
            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch1" }, JoinType.LEFT, true),
            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch2" }, JoinType.INNER, true)));
    }
    
    @Test
    public void resolvesJoinContainerWithRegularJoin() throws Exception {
    	MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_joinContainerWithRegularJoin"), 0);
    	FakeWebRequest req = new FakeWebRequest();
        QueryContext queryCtx = new WebRequestQueryContext(req);
        req.setParameterValues("path1", "value1");

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
            .hasSize(2)
            .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
            .contains(new Conjunction<Object>(
            		new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(queryCtx, "join1", "alias1", JoinType.INNER, true),
            		new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(queryCtx, "join2", "alias2", JoinType.LEFT, false)));
    }
    
    @Test
    public void resolvesJoinContainerWithRegularAndFetchJoins() throws Exception {
    	MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod_joinContainerWithRegularAndFetchJoins"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
            .hasSize(2)
            .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
            .contains(new Conjunction<Object>(
            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch1" }, JoinType.LEFT, true),
            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch2" }, JoinType.INNER, true),
            		new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(queryCtx, "join1", "alias1", JoinType.INNER, true),
            		new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(queryCtx, "join2", "alias2", JoinType.LEFT, false)));
    }
    
    @Test
    public void resolvesJoinFetchEvenIfOtherSpecificationIsNotPresent() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isEqualTo(new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch1", "fetch2" }, JoinType.LEFT, true));
    }
    
    @Test
    public void resolvesJoinFetchForAnnotatedInterface() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithCustomSpec", CustomSpec.class), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
                .hasSize(2)
                .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
                .contains(new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(queryCtx, new String[] { "fetch1", "fetch2" }, JoinType.LEFT, true));
    }
    
    @Test
    public void resolvesRepeatedJoinFetchForAnnotatedInterface() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithCustomSpec_repeatedFetch", CustomSpecRepeatedFetch.class), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(innerSpecs(resolved))
                .hasSize(2)
                .contains(new Like<>(queryCtx, "path1", new String[]{ "value1" }))
                .contains(new Conjunction<>(
                        new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{ "fetch1" }, JoinType.LEFT, true),
                        new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(queryCtx, new String[]{ "fetch2" }, JoinType.LEFT, true)));
    }
    
    @Test
    public void resolvesJoinContainerForAnnotatedInterface() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithCustomSpec_joinContainer", CustomSpecJoinContainer.class), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isInstanceOf(CustomSpecJoinContainer.class); // TODO better assertions
    }

    @Override
    protected Class<?> controllerClass() {
        return TestController.class;
    }
    
    @JoinFetch(paths = { "fetch1", "fetch2" })
    @Spec(path = "path1", spec = Like.class)
    public static interface CustomSpec extends Specification<Object> {
    }
    
    @JoinFetch(paths = { "fetch1" })
    @JoinFetch(paths = { "fetch2" })
    @Spec(path = "path1", spec = Like.class)
    public static interface CustomSpecRepeatedFetch extends Specification<Object> {
    }
    
    @Joins(fetch = {
    	@JoinFetch(paths = { "fetch1" }),
        @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
    })
    @Spec(path = "path1", spec = Like.class)
    public static interface CustomSpecJoinContainer extends Specification<Object> {
    }
    
    public static class TestController {
        
    	public void testMethodWithCustomSpec(CustomSpec spec) {
        }
    	
    	public void testMethodWithCustomSpec_repeatedFetch(CustomSpecRepeatedFetch spec) {
        }
    	
    	public void testMethodWithCustomSpec_joinContainer(CustomSpecJoinContainer spec) {
        }
        
        public void testMethod(
                @JoinFetch(paths = { "fetch1", "fetch2" })
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
        
        public void testMethod_repeatedFetch(
                @JoinFetch(paths = { "fetch1" })
                @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
        
        public void testMethod_joinContainerWithJoinFetch(
                @Joins(fetch = {
                	@JoinFetch(paths = { "fetch1" }),
                    @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
                })
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
        
        public void testMethod_joinContainerWithRegularJoin(
                @Joins({
                	@Join(path = "join1", alias = "alias1", type = JoinType.INNER, distinct = true),
                	@Join(path = "join2", alias = "alias2", type = JoinType.LEFT, distinct = false)
                })
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
        
        public void testMethod_joinContainerWithRegularAndFetchJoins(
                @Joins(value = {
                	@Join(path = "join1", alias = "alias1", type = JoinType.INNER, distinct = true),
                	@Join(path = "join2", alias = "alias2", type = JoinType.LEFT, distinct = false)
                }, fetch = {
                    	@JoinFetch(paths = { "fetch1" }),
                        @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
                    })
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
    }
}
