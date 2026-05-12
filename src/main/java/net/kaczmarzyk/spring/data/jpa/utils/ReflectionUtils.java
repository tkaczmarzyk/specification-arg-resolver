/*
 * Copyright 2014-2026 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import java.util.concurrent.Callable;

/**
 * Utility class for reflective operations.
 *
 * @since 3.4
 * @author Sebastian Nawrocki
 */
public abstract class ReflectionUtils {

    /**
     * Attempts to execute a series of reflective operations sequentially.
     * The first operation that completes without a {@link NoSuchMethodException} returns its result.
     *
     * @param <T> the type of the result
     * @param operations the operations to be attempted
     * @return the result of the first successful operation, or null if all threw {@link NoSuchMethodException}
     * @throws Exception if any operation throws an exception other than {@link NoSuchMethodException}
     */
    @SafeVarargs
    public static <T> T tryInstance(Callable<T>... operations) throws Exception {
        for (Callable<T> operation : operations) {
            try {
                return operation.call();
            } catch (NoSuchMethodException e) {
                // ignore and try next
            }
        }
        return null;
    }
}
