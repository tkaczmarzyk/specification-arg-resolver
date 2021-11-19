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

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Executable;
import java.lang.reflect.Proxy;
import java.util.Collection;




/**
 * @author Tomasz Kaczmarzyk
 */
public abstract class ResolverTestBase {

	protected Converter defaultConverter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null);
	
	protected MethodParameter testMethodParameter(String methodName) {
        return MethodParameter.forExecutable(testMethod(methodName, Specification.class), 0);
    }
	
	protected Executable testMethod(String methodName) {
        return testMethod(methodName, Specification.class);
    }
	
	protected Executable testMethod(String methodName, Class<?> specClass) {
        try {
            return controllerClass().getMethod(methodName, specClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected MethodParameter methodParameter(String methodName, Class<?> specClass) {
		return MethodParameter.forExecutable(
				testMethod(methodName, specClass), 0
		);
    }

	protected Collection<Specification<Object>> innerSpecs(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Conjunction<Object> resolvedConjunction =
				ReflectionUtils.get(Proxy.getInvocationHandler(resolvedSpec), "targetSpec");

		return ReflectionUtils.get(resolvedConjunction, "innerSpecs");
	}

	protected Collection<Specification<Object>> innerSpecsFromDisjunction(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Disjunction<Object> resolvedDisjunction =
				ReflectionUtils.get(Proxy.getInvocationHandler(resolvedSpec), "targetSpec");

		return ReflectionUtils.get(resolvedDisjunction, "innerSpecs");
	}

	protected abstract Class<?> controllerClass();
}
