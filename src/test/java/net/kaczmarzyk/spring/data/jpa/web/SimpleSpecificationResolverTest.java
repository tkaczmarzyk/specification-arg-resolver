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

import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SimpleSpecificationResolverTest extends ResolverTestBase {

    SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();

	private Converter converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EXCEPTION, null);

    @Test
    public void returnsNullIfTheWebParameterIsMissing_defaultParameterName() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isNull();
    }

    @Test
    public void resolvesZeroArgSpecificatinEvenWithoutAnyWebParameters() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithZeroArgSpec"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isInstanceOf(IsNull.class);
    }

    @Test
    public void returnsNullIfTheWebParameterIsMissing_customParameterName() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isNull();
    }

    @Test
    public void returnsNullIfTheWebParameterIsEmpty_defaultParameterName() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[] { "" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isNull();
    }

    @Test
    public void returnsNullIfTheWebParameterIsEmpty_customParameterName() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("theParameter")).thenReturn(new String[] { "" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isNull();
    }

    @Test
    public void returnsNullIfAtLeastOneEmptyWebParameter_defaultParameterName() {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod3"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[] { "theValue", "theValue2", "" });

        assertThat(resolver.buildSpecification(new WebRequestProcessingContext(param, req), param.getParameterAnnotation(Spec.class))).isNull();;
    }

    @Test
    public void returnsNullIfAtLeastOneEmptyWebParameter_customParameterName() {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[] { "theValue", "theValue2", "" });

        assertThat(resolver.buildSpecification(new WebRequestProcessingContext(param, req), param.getParameterAnnotation(Spec.class))).isNull();;
    }

    @Test
    public void buildsTheSpecUsingWebParameterTheSameAsPath() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("thePath")).thenReturn(new String[] { "theValue" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new Like<>(queryCtx, "thePath", new String[] { "theValue" }));
    }

    @Test
    public void buildsTheSpecUsingConstValue() throws Exception {
    	MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithConst1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new Equal<>(queryCtx, "thePath", new String[] { "constVal1" }, converter));
    }

    @Test
    public void ignoresHttpParamIfConstValueIsSpecified() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithConst1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("thePath")).thenReturn(new String[] { "theValue" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new Equal<>(queryCtx, "thePath", new String[] { "constVal1" }, converter));
    }

    @Test
    public void buildsTheSpecUsingCustomWebParameterName() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new Like<>(queryCtx, "thePath", new String[] { "theValue" }));
    }

    @Test
    public void buildsTheSpecUsingCustomMultiValueWebParameterName() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod3"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue", "theValue2" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new EqualEnum<>(queryCtx, "thePath", new String[] { "theValue", "theValue2" }));
    }

    @Test
    public void skipsEmptyWebParameterValues() {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod3"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("theParameter")).thenReturn(new String[] { "value1", "" });

        Specification<Object> resolved = resolver.buildSpecification(new WebRequestProcessingContext(param, req), param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new EqualEnum<>(queryCtx, "thePath", new String[] { "value1" }));
    }

    @Test
    public void buildsTheSpecUsingCustomMultiValueWebParametersNames() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod4"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("theParameter")).thenReturn(new String[] { "theValue", "theValue2" });
        when(req.getParameterValues("theParameter2")).thenReturn(new String[] { "theValue3", "theValue4" });

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new EqualEnum<>(queryCtx, "thePath", new String[] { "theValue", "theValue2", "theValue3", "theValue4" }));
    }

    @Test
    public void buildsTheSpecUsingMultiValueWebParameterTheSameAsPathWithoutParamSeparator() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod5"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);

        when(req.getParameterValues("thePath")).thenReturn(new String[] {"val1", "val2,val3,val4", "val5,val6", "val7"});

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new In<>(queryCtx, "thePath", new String[] {"val1", "val2,val3,val4", "val5,val6", "val7"}, converter));
    }

    @Test
    public void buildsTheSpecUsingMultiValueWebParameterTheSameAsPathAndParamSeparator() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod6"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);

        when(req.getParameterValues("thePath")).thenReturn(new String[] {"val1", "val2,val3,val4", "val5,val6", "val7"});

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new In<>(queryCtx, "thePath", new String[] { "val1", "val2", "val3", "val4", "val5", "val6", "val7" }, converter));
    }

    @Test
    public void buildsTheSpecUsingMultiValueWebParameterWithoutParamSeparator() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod7"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);

        when(req.getParameterValues("theParameter")).thenReturn(new String[] {"val1", "val2,val3,val4", "val5,val6", "val7"});

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new In<>(queryCtx, "thePath", new String[] { "val1", "val2,val3,val4", "val5,val6", "val7" }, converter));
    }

    @Test
    public void buildsTheSpecUsingMultiValueWebParameterAndParamSeparator() throws Exception {
        MethodParameter param = MethodParameter.forExecutable(testMethod("testMethod8"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);

        when(req.getParameterValues("theParameter")).thenReturn(new String[] {"val1", "val2,val3,val4", "val5,val6", "val7"});

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new In<>(queryCtx, "thePath", new String[] { "val1", "val2", "val3", "val4", "val5", "val6", "val7" }, converter));
    }

    public static class TestController {

        public void testMethod1(@Spec(path = "thePath", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod2(@Spec(path = "thePath", params = "theParameter", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod3(@Spec(path = "thePath", params = "theParameter", spec = EqualEnum.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod4(
                @Spec(path = "thePath", params = { "theParameter", "theParameter2" }, spec = EqualEnum.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod5(
                @Spec(path = "thePath", spec = In.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod6(
                @Spec(path = "thePath", paramSeparator = ',', spec = In.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod7(
                @Spec(path = "thePath", params = "theParameter", spec = In.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethod8(
                @Spec(path = "thePath", params = "theParameter", paramSeparator = ',', spec = In.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }


        public void testMethodWithConst1(@Spec(path = "thePath", spec = Equal.class, constVal = "constVal1", onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

        public void testMethodWithZeroArgSpec(@Spec(path = "thePath", spec = IsNull.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {}
    }

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
