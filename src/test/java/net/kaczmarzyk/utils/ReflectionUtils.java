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
package net.kaczmarzyk.utils;

import java.lang.reflect.Field;


/**
 * <p>A set of utility methods which provide simplified way of using reflection API.</p>
 * 
 * <p>Intended to be used <strong>only</strong> in test code</p>
 */
public final class ReflectionUtils {

    /**
     * Hidden utility class constructor
     */
    private ReflectionUtils() {
    }
    
    public static void set(Object target, String fieldname, Object value) {
        try {
            Class<?> classToBeUsed = target.getClass();
            do {
                try {
                    Field f = classToBeUsed.getDeclaredField(fieldname);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException err) {
                    classToBeUsed = classToBeUsed.getSuperclass();
                    if (classToBeUsed == Object.class) {
                        throw err;
                    }
                }
            } while (classToBeUsed != Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(Object target, String fieldname) {
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