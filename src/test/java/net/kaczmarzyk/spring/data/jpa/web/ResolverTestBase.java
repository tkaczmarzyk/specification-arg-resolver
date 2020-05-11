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

import java.lang.reflect.Executable;

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;


/**
 * @author Tomasz Kaczmarzyk
 */
public abstract class ResolverTestBase {

	protected Converter defaultConverter = Converter.DEFAULT;
	
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
	
	protected abstract Class<?> controllerClass();
}
