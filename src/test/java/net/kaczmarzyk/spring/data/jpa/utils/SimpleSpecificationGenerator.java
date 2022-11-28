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
package net.kaczmarzyk.spring.data.jpa.utils;

import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.DefaultQueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class SimpleSpecificationGenerator {

	public static List<Class<? extends PathSpecification>> LIST_OF_SIMPLE_SPECIFICATION_TYPES = Stream.of(Between.class, DateAfter.class, DateAfterInclusive.class, DateBefore.class,
			DateBetween.class, EndingWith.class, EndingWithIgnoreCase.class, Equal.class, EqualEnum.class,
			EqualIgnoreCase.class, GreaterThan.class, GreaterThanOrEqual.class, In.class, IsNull.class,
			LessThan.class, LessThanOrEqual.class, Like.class, LikeIgnoreCase.class, NotEqual.class,
			NotEqualIgnoreCase.class, NotIn.class, NotLike.class, NotNull.class, Null.class,
			StartingWith.class, StartingWithIgnoreCase.class).collect(Collectors.toList());

	private static Class<?>[] parametersOfConstructorType1 = new Class<?>[]{ QueryContext.class, String.class, String[].class };
	private static Class<?>[] parametersOfConstructorType2 = new Class<?>[]{ QueryContext.class, String.class, String[].class, Converter.class };

	public static final Integer SEED_1 = 1;
	public static final Integer SEED_2 = 2;

	private static Converter defaultConverter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null);
	private static DefaultQueryContext queryCtx = new DefaultQueryContext();

	private static LocalDateTime GENERATOR_LOCAL_BASE_DATE_TIME = LocalDateTime.now();

	/**
	 * Returns specification instance with test values
	 */
	public static Specification<Object> testSpecification(Class<? extends PathSpecification> clazz) {
		return testSpecification(clazz, SEED_1);
	}

	/**
	 * Returns specification instance. Specification path and argument values depend on a seed value (for the given seed value always the same values are returned)
	 *
	 * @param specificationType type of specification to be created
	 * @param seed              seed value used for specification generation
	 * @return specification instance
	 */
	public static Specification<Object> testSpecification(Class<? extends PathSpecification> specificationType, Integer seed) {
		Specification<Object> spec = null;

		try {
			Constructor<?>[] constructors = specificationType.getConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] parameters = constructor.getParameterTypes();
				if (Arrays.equals(parameters, parametersOfConstructorType1)) {
					spec = (Specification<Object>) specificationType.getConstructor(parametersOfConstructorType1)
							.newInstance(queryCtx, getPath(seed), getArgs(1, seed));
				} else if (Arrays.equals(parameters, parametersOfConstructorType2)) {
					try {
						spec = (Specification<Object>) specificationType.getConstructor(parametersOfConstructorType2)
								.newInstance(queryCtx, getPath(seed), getArgs(1, seed), defaultConverter);
					} catch (Throwable ex) {
						spec = (Specification<Object>) specificationType.getConstructor(parametersOfConstructorType2)
								.newInstance(queryCtx, getPath(seed), getArgs(2, seed), defaultConverter);
					}
				}
				break;
			}
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}

		if (isNull(spec)) {
			throw new IllegalStateException("Specification of type: " + specificationType + " could not be initialized!");
		}
		return spec;
	}

	private static String getPath(Integer seed) {
		return "path_" + seed;
	}

	private static String[] getArgs(Integer argsNumber, Integer seed) {
		String[] args = new String[argsNumber];
		for (int i = 0; i < argsNumber; i++) {
			args[i] = GENERATOR_LOCAL_BASE_DATE_TIME.plusSeconds(seed).toString();
		}
		return args;
	}

}
