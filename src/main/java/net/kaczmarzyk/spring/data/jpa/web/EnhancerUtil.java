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

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.jpa.domain.Specification;


/**
 * @author Tomasz Kaczmarzyk
 */
class EnhancerUtil {

    @SuppressWarnings("unchecked")
	static <T> T wrapWithIfaceImplementation(final Class<T> iface, final Specification<Object> targetSpec) {
    	Enhancer enhancer = new Enhancer();
		enhancer.setInterfaces(new Class[] { iface });
		enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            	if ("toString".equals(method.getName())) {
            		return iface.getSimpleName() + "[" + proxy.invoke(targetSpec, args) + "]";
            	}
            	return proxy.invoke(targetSpec, args);
            }
        });
		
		return (T) enhancer.create();
    }
}
