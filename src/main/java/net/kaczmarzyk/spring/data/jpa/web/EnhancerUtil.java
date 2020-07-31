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

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * @author Tomasz Kaczmarzyk
 */
class EnhancerUtil {

	public interface GlibEnhancedObjectMarker {
	}

	@SuppressWarnings("unchecked")
	static <T> T wrapWithIfaceImplementation(final Class<T> iface, final Specification<Object> targetSpec) {
    	Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(iface);
		enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
			if ("toString".equals(method.getName())) {
				return iface.getSimpleName() + "[" + method.invoke(targetSpec, args) + "]";
			}
			if ("equals".equals(method.getName())) {
				return EnhancerUtil.equals(iface, targetSpec, args);
			}

				return proxy.invoke(targetSpec, args);
			});
		return (T) enhancer.create();
	}

	private static boolean equals(Class<?> iface, Specification<Object> targetSpec, Object[] args) {
		if (args.length != 1 || args[0] == null) {
			return false;
		}

		// The argument is not equal to the actual object if it is not a glib enhanced object
		if (!isAnObjectThatDirectImplementsGivenInterface(args[0], GlibEnhancedObjectMarker.class)) {
			return false;
		}

		// The argument is not equal to the actual object if it is not a direct implementation of the actual interface
		if (!isAnObjectThatDirectImplementsGivenInterface(args[0], iface)) {
			return false;
		}

		return ReflectionUtils.get(ReflectionUtils.get(args[0], "CGLIB$CALLBACK_0"), "val$targetSpec").equals(targetSpec);
	}

	private static boolean isAnObjectThatDirectImplementsGivenInterface(Object object, Class<?> expectedType) {
		return arrayContains(object.getClass().getInterfaces(), expectedType);
	}

	private static boolean arrayContains(Class<?>[] array, Class<?> valueToFind) {
		for(Class<?> arrayValue: array) {
			if(arrayValue.equals(valueToFind)) {
				return true;
			}
		}
		return false;
	}

	private static final class ReflectionUtils {

		@SuppressWarnings("unchecked")
		static <T> T get(Object target, String fieldname) {
			try {
				Class<?> classToBeUsed = target.getClass();
				do {
					try {
						Field f = classToBeUsed.getDeclaredField(fieldname);
						f.setAccessible(true);
						return (T) f.get(target);
					} catch (NoSuchFieldException err) {
						classToBeUsed = classToBeUsed.getSuperclass();
						if (classToBeUsed == Object.class) {
							throw err;
						}
					}
				} while (classToBeUsed != Object.class);
				throw new NoSuchFieldException();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
