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
package net.kaczmarzyk.spring.data.jpa.swagger;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public final class SpecExtractorUtil {

	private static final Map<Class<?>, Function<Annotation, List<Spec>>> NESTED_SPEC_ANNOTATION_EXTRACTORS = new HashMap<>();

	static {
		NESTED_SPEC_ANNOTATION_EXTRACTORS.put(Spec.class, annotation -> singletonList((Spec) annotation));
		NESTED_SPEC_ANNOTATION_EXTRACTORS.put(And.class, annotation -> extractSpecsFromAnd((And) annotation));
		NESTED_SPEC_ANNOTATION_EXTRACTORS.put(Or.class, annotation -> extractSpecsFromOr((Or) annotation));
		NESTED_SPEC_ANNOTATION_EXTRACTORS.put(Conjunction.class, annotation -> extractSpecsFromConjunction((Conjunction) annotation));
		NESTED_SPEC_ANNOTATION_EXTRACTORS.put(Disjunction.class, annotation -> extractSpecsFromDisjunction((Disjunction) annotation));
	}

	private SpecExtractorUtil() {

	}

	public static List<Spec> extractNestedSpecificationsFromAnnotations(List<Annotation> annotations) {

		return annotations.stream()
			.filter(annotation -> NESTED_SPEC_ANNOTATION_EXTRACTORS.containsKey(annotation.annotationType()))
			.map(annotation -> NESTED_SPEC_ANNOTATION_EXTRACTORS.get(annotation.annotationType())
				.apply(annotation))
			.flatMap(Collection::stream)
			.collect(toList());
	}

	private static List<Spec> extractSpecsFromOr(Or or) {
		return asList(or.value());
	}

	private static List<Spec> extractSpecsFromAnd(And and) {
		return asList(and.value());
	}

	private static List<Spec> extractSpecsFromConjunction(Conjunction conjunction) {
		List<Spec> conjunctionSpecs = new ArrayList<>(asList(conjunction.and()));

		stream(conjunction.value())
			.map(Or::value)
			.flatMap(Arrays::stream)
			.forEach(conjunctionSpecs::add);

		return conjunctionSpecs;
	}

	private static List<Spec> extractSpecsFromDisjunction(Disjunction conjunction) {
		List<Spec> disjunctionSpecs = new ArrayList<>(asList(conjunction.or()));

		stream(conjunction.value())
			.map(And::value)
			.flatMap(Arrays::stream)
			.forEach(disjunctionSpecs::add);

		return disjunctionSpecs;
	}
}
