/*
 * Copyright 2014-2025 the original author or authors.
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

import java.lang.reflect.Constructor;
import java.util.Set;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.Empty;
import net.kaczmarzyk.spring.data.jpa.domain.EndingWith;
import net.kaczmarzyk.spring.data.jpa.domain.EndingWithIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.EqualDay;
import net.kaczmarzyk.spring.data.jpa.domain.EqualIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.False;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.InIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.InTheFuture;
import net.kaczmarzyk.spring.data.jpa.domain.InThePast;
import net.kaczmarzyk.spring.data.jpa.domain.IsEmpty;
import net.kaczmarzyk.spring.data.jpa.domain.IsFalse;
import net.kaczmarzyk.spring.data.jpa.domain.IsMember;
import net.kaczmarzyk.spring.data.jpa.domain.IsNotEmpty;
import net.kaczmarzyk.spring.data.jpa.domain.IsNotMember;
import net.kaczmarzyk.spring.data.jpa.domain.IsNotNull;
import net.kaczmarzyk.spring.data.jpa.domain.IsNull;
import net.kaczmarzyk.spring.data.jpa.domain.IsTrue;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.NotEmpty;
import net.kaczmarzyk.spring.data.jpa.domain.NotEqual;
import net.kaczmarzyk.spring.data.jpa.domain.NotEqualIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.NotIn;
import net.kaczmarzyk.spring.data.jpa.domain.NotLike;
import net.kaczmarzyk.spring.data.jpa.domain.NotLikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.NotNull;
import net.kaczmarzyk.spring.data.jpa.domain.Null;
import net.kaczmarzyk.spring.data.jpa.domain.StartingWith;
import net.kaczmarzyk.spring.data.jpa.domain.StartingWithIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.True;

/**
 * Registers the constructors of specifications defined in package net.kaczmarzyk.spring.data.jpa.domain (they are invoked by SimpleSpecificationResolver via reflection).
 * This is needed for using this library in Spring Native builds (otherwise constructors would be considered unused)
 * @author Jakub Radlica
 */
public class SpecificationArgumentResolverHintRegistrar implements RuntimeHintsRegistrar {

    private static final Set<Class<?>> SPECIFICATION_CLASSES = Set.of(
            Between.class,
            EndingWith.class,
            EndingWithIgnoreCase.class,
            Equal.class,
            EqualDay.class,
            EqualIgnoreCase.class,
            False.class,
            GreaterThan.class,
            GreaterThanOrEqual.class,
            In.class,
            InIgnoreCase.class,
            InTheFuture.class,
            InThePast.class,
            Empty.class,
            NotEmpty.class,
            IsEmpty.class,
            IsFalse.class,
            IsNotEmpty.class,
            IsMember.class,
            IsNotMember.class,
            IsNull.class,
            IsNotNull.class,
            IsTrue.class,
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
            StartingWithIgnoreCase.class,
            True.class
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
