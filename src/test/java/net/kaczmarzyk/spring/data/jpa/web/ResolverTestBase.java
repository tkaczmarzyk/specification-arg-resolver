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

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Executable;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


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
		return ReflectionUtils.get(resolvedSpec, "innerSpecs");
	}

	protected Collection<Specification<Object>> proxiedInnerSpecs(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Conjunction<Object> resolvedConjunction =
				ReflectionUtils.get(ReflectionUtils.get(resolvedSpec, "CGLIB$CALLBACK_0"), "arg$2");

		return ReflectionUtils.get(resolvedConjunction, "innerSpecs");
	}

	protected Collection<Specification<Object>> innerSpecsFromDisjunction(Specification<?> resolvedSpec) {
		net.kaczmarzyk.spring.data.jpa.domain.Disjunction<Object> resolvedDisjunction =
				ReflectionUtils.get(ReflectionUtils.get(resolvedSpec, "CGLIB$CALLBACK_0"), "arg$2");

		return ReflectionUtils.get(resolvedDisjunction, "innerSpecs");
	}

	protected abstract Class<?> controllerClass();

	protected void assertThatSpecIsProxy(Specification<?> specification) {
		assertThat(Enhancer.isEnhanced(specification.getClass())).isTrue();
	}

	protected void assertThatSpecIsNotProxy(Specification<?> specification) {
		assertThat(Enhancer.isEnhanced(specification.getClass())).isFalse();
	}
}
