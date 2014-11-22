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
import net.kaczmarzyk.spring.data.jpa.domain.Disjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author Tomasz Kaczmarzyk
 */
public class DisjunctionSpecificationResolverTest {

    DisjunctionSpecificationResolver resolver = new DisjunctionSpecificationResolver();
    
    @Test
    public void resolvesWrapperOfInnerSpecs() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("path1")).thenReturn(new String[]{"value1"});
        when(req.getParameterValues("path2")).thenReturn(new String[]{"value2"});
        
        Specification<?> result = resolver.resolveArgument(param, null, req, null);
        
        assertThat(result).isEqualTo(new Disjunction<>(new Like<>("path1", "value1"),
                new Like<>("path2", "value2")));
    }
    
    @Test
    public void skipsMissingInnerSpec() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("path1")).thenReturn(new String[]{"value1"});
        
        Specification<?> result = resolver.resolveArgument(param, null, req, null);
        
        assertThat(result).isEqualTo(new Disjunction<>(new Like<>("path1", "value1")));
    }
    
    @Test
    public void returnsNullIfNoInnerSpecCanBeResolved() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        Specification<?> result = resolver.resolveArgument(param, null, req, null);
        
        assertThat(result).isNull();
    }
    
    @Test
    public void rejectsMissingInnerSpecs() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        
        assertFalse(resolver.canBuildSpecification(req, param.getParameterAnnotation(Or.class)));
    }
    
    private Object testMethod(String methodName) {
        try {
            return TestController.class.getMethod(methodName, Specification.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class TestController {
        public void testMethod(
                @Or({@Spec(path="path1", spec=Like.class), @Spec(path="path2", spec=Like.class)}) Specification<Object> spec) {
        }
    }
}
