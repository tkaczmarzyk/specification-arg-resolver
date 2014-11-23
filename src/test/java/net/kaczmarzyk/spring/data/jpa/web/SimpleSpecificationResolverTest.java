/**
 * Copyright 2014 the original author or authors.
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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.kaczmarzyk.spring.data.jpa.domain.EqualEnum;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;


public class SimpleSpecificationResolverTest {

    SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();
    
    
    @Test
    public void returnsNullIfTheWebParameterIsMissing_defaultParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isNull();
    }
    
    @Test
    public void returnsNullIfTheWebParameterIsMissing_customParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isNull();
    }
    
    @Test
    public void returnsNullIfTheWebParameterIsEmpty_defaultParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[]{""});
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isNull();
    }
    
    @Test
    public void returnsNullIfTheWebParameterIsEmpty_customParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("theParameter")).thenReturn(new String[]{""});
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isNull();
    }
    
    @Test
    public void rejectsMissingWebParameter_defaultParameterName() {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Spec.class)));
    }
    
    @Test
    public void rejectsMissingWebParameter_customParameterName() {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Spec.class)));
    }
    
    @Test
    public void rejectsSingleEmptyWebParameter_defaultParameterName() {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[]{""});
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Spec.class)));
    }
    
    @Test
    public void rejectsSingleEmptyWebParameter_customParameterName() {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("theParameter")).thenReturn(new String[]{""});
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Spec.class)));
    }
    
    @Test
    public void rejectsAtLeastOneEmptyWebParameter_defaultParameterName() {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[]{"theValue", "theValue2", ""});
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Spec.class)));
    }
    
    @Test
    public void rejectsAtLeastOneEmptyWebParameter_customParameterName() {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[]{"theValue", "theValue2", ""});
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Spec.class)));
    }
    
    @Test
    public void buildsTheSpecUsingWebParameterTheSameAsPath() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("thePath")).thenReturn(new String[]{"theValue"});
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isEqualTo(new Like<>("thePath", new String[] {"theValue"}));
    }
    
    @Test
    public void buildsTheSpecUsingCustomWebParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("theParameter")).thenReturn(new String[]{"theValue"});
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isEqualTo(new Like<>("thePath", new String[] {"theValue"}));
    }
    
    @Test
    public void buildsTheSpecUsingCustomMultiValueWebParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod3"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("theParameter")).thenReturn(new String[]{"theValue", "theValue2"});
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isEqualTo(new EqualEnum<>("thePath", new String[] {"theValue", "theValue2"}));
    }
    
    @Test
    public void buildsTheSpecUsingCustomMultiValueWebParametersNames() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod4"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("theParameter")).thenReturn(new String[]{"theValue", "theValue2"});
        when(req.getParameterValues("theParameter2")).thenReturn(new String[]{"theValue3", "theValue4"});
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isEqualTo(new EqualEnum<>("thePath", new String[] {"theValue", "theValue2", "theValue3", "theValue4"}));
    }
    
    private Object testMethod(String methodName) {
        try {
            return TestController.class.getMethod(methodName, Specification.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class TestController {
        
        public void testMethod1(@Spec(path="thePath", spec=Like.class) Specification<Object> spec) {
        }
        
        public void testMethod2(@Spec(path="thePath", params="theParameter", spec=Like.class) Specification<Object> spec) {
        }
        
        public void testMethod3(@Spec(path="thePath", params="theParameter", spec=EqualEnum.class) Specification<Object> spec) {
        }
        
        public void testMethod4(@Spec(path="thePath", params={"theParameter", "theParameter2"}, spec=EqualEnum.class) Specification<Object> spec) {
        }
    }
}
