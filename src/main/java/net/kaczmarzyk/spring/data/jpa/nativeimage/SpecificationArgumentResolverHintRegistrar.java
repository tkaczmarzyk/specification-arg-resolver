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
package net.kaczmarzyk.spring.data.jpa.nativeimage;

import net.kaczmarzyk.spring.data.jpa.domain.*;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * Registers the constructors of specifications defined in package net.kaczmarzyk.spring.data.jpa.domain (they are invoked by SimpleSpecificationResolver via reflection).
 * This is needed for using this library in Spring Native builds (otherwise constructors would be considered unused)
 * @author Jakub Radlica
 */
public class SpecificationArgumentResolverHintRegistrar implements RuntimeHintsRegistrar {

    private static final Set<Class<?>> SPECIFICATION_CLASSES = Set.of(
            Between.class,
            DateAfter.class,
            DateAfterInclusive.class,
            DateBefore.class,
            DateBeforeInclusive.class,
            DateBetween.class,
            EndingWith.class,
            EndingWithIgnoreCase.class,
            Equal.class,
            EqualEnum.class,
            EqualIgnoreCase.class,
            GreaterThan.class,
            GreaterThanOrEqual.class,
            In.class,
            InTheFuture.class,
            InThePast.class,
            IsNull.class,
            LessThan.class,
            LessThanOrEqual.class,
            Like.class,
            LikeIgnoreCase.class,
            NotEqual.class,
            NotEqualIgnoreCase.class,
            NotIn.class,
            NotLike.class,
            NotLikeIgnoreCase.class,
            NotNull.class,
            Null.class,
            StartingWith.class,
            StartingWithIgnoreCase.class
    );

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        SPECIFICATION_CLASSES.forEach(specificationClass -> {
            for (Constructor<?> constructor : specificationClass.getConstructors()) {
                hints.reflection().registerConstructor(constructor, ExecutableMode.INVOKE);
            }
        });
    }
}
