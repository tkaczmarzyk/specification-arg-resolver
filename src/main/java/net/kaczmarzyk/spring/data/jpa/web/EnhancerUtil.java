/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.web;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Tomasz Kaczmarzyk
 */
@UtilityClass
class EnhancerUtil {

    @SuppressWarnings("unchecked")
    public <T> T wrapWithInterfaceImplementation(final Class<T> targetInterface, final Specification<Object> targetSpec) {
        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class[]{targetInterface},
                new ProxyInvocationHandler(targetSpec, targetInterface));
    }

    @ToString
    @RequiredArgsConstructor
    public static class ProxyInvocationHandler implements InvocationHandler {

        private final Specification<Object> targetSpec;
        private final Class<?> targetInterface;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("toString".equals(method.getName())) {
                return targetInterface.getSimpleName() + "[" + targetSpec.toString() + "]";
            }
            return targetSpec.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(targetSpec, args);
        }
    }
}
