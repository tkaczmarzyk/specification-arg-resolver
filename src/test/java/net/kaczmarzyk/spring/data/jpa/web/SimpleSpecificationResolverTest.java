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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;


public class SimpleSpecificationResolverTest {

    SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();
    
    
    @Test
    public void returnsNullIfTheWebParameterIsMissing() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isNull();
    }
    
    @Test
    public void returnsNullIfTheWebParameterIsEmpty() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameter("thePath")).thenReturn("");
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isNull();
    }
    
    @Test
    public void buildsTheSpecUsingWebParameterTheSameAsPath() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod1"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameter("thePath")).thenReturn("theValue");
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isEqualTo(new Like<>("thePath", new String[] {"theValue"}));
    }
    
    @Test
    public void buildsTheSpecUsingCustomWebParameterName() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod2"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameter("theParameter")).thenReturn("theValue");
        
        Specification<?> resolved = resolver.resolveArgument(param, null, req, null);
        
        assertThat(resolved).isEqualTo(new Like<>("thePath", new String[] {"theValue"}));
    }
    
    private Object testMethod(String methodName) {
        try {
            return TestController.class.getMethod(methodName, Specification.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public class TestController {
        
        public void testMethod1(@Spec(path="thePath", spec=Like.class) Specification<Object> spec) {
        }
        
        public void testMethod2(@Spec(path="thePath", params="theParameter", spec=Like.class) Specification<Object> spec) {
        }
    }
}
