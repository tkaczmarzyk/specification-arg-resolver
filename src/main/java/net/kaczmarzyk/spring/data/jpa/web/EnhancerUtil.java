/**
 * Copyright 2014-2023 the original author or authors.
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;


/**
 * @author Tomasz Kaczmarzyk
 */
abstract class EnhancerUtil {

    @SuppressWarnings("unchecked")
    static <T> T wrapWithIfaceImplementation(final Class<T> iface, final Specification<Object> targetSpec) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                EnhancerUtil.class.getClassLoader(),
                new Class[]{iface},
                (proxy, method, args) -> switch (method.getName()) {
                    case "toPredicate" -> targetSpec.toPredicate(
                            (Root<Object>) args[0],
                            (CriteriaQuery<?>) args[1],
                            (CriteriaBuilder) args[2]
                    );
                    case "toString" -> iface.getSimpleName() + "[" + targetSpec.toString() + "]";
                    case "equals" -> EnhancerUtil.equals(iface, targetSpec, args);
                    case "hashCode" -> targetSpec.hashCode();
                    default -> targetSpec.getClass().getMethod(method.getName(), method.getParameterTypes())
                            .invoke(targetSpec, args);
                });
    }

    private static boolean equals(Class<?> iface, Specification<Object> targetSpec, Object[] args) {
        if (args.length != 1 || args[0] == null) {
            return false;
        }

        // The argument is not equal to the actual object if it is not a glib enhanced object
        if (!Proxy.isProxyClass(args[0].getClass())) {
            return false;
        }

        // The argument is not equal to the actual object if it is not a direct implementation of the actual interface
        if (!isAnObjectThatDirectImplementsGivenInterface(args[0], iface)) {
            return false;
        }

        return ReflectionUtils.get(Proxy.getInvocationHandler(args[0]), "arg$1").equals(targetSpec);
    }

    private static boolean isAnObjectThatDirectImplementsGivenInterface(Object object, Class<?> expectedType) {
        return arrayContains(object.getClass().getInterfaces(), expectedType);
    }

    private static boolean arrayContains(Class<?>[] array, Class<?> valueToFind) {
        for (Class<?> arrayValue : array) {
            if (arrayValue.equals(valueToFind)) {
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
