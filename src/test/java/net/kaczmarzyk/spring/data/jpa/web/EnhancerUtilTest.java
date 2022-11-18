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

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Disjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.utils.SimpleSpecificationGenerator;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

import static net.kaczmarzyk.spring.data.jpa.utils.SimpleSpecificationGenerator.*;

public class EnhancerUtilTest {

	@Test
	public void equalsContract_singleSimpleSpecification() {
		LIST_OF_SIMPLE_SPECIFICATION_TYPES.forEach(specType -> {
			Specification<Object> firstSpec = testSpecification(specType);
			Specification<Object> secondSpec = testSpecification(specType);

			CustomSpecInterface firstSpecEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
			CustomSpecInterface secondSpecEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondSpec);

			Assertions.assertThat(firstSpec).isEqualTo(firstSpec);
			Assertions.assertThat(firstSpec).isEqualTo(secondSpec);
			Assertions.assertThat(firstSpec).isNotEqualTo(firstSpecEnhanced);
			Assertions.assertThat(firstSpec).isNotEqualTo(secondSpecEnhanced);

			Assertions.assertThat(secondSpec).isEqualTo(secondSpec);
			Assertions.assertThat(secondSpec).isEqualTo(secondSpec);
			Assertions.assertThat(secondSpec).isNotEqualTo(firstSpecEnhanced);
			Assertions.assertThat(secondSpec).isNotEqualTo(secondSpecEnhanced);

			Assertions.assertThat(firstSpecEnhanced).isNotEqualTo(firstSpec);
			Assertions.assertThat(firstSpecEnhanced).isNotEqualTo(secondSpec);
			Assertions.assertThat(firstSpecEnhanced).isEqualTo(firstSpecEnhanced);
			Assertions.assertThat(firstSpecEnhanced).isEqualTo(secondSpecEnhanced);

			Assertions.assertThat(secondSpecEnhanced).isNotEqualTo(firstSpec);
			Assertions.assertThat(secondSpecEnhanced).isNotEqualTo(secondSpec);
			Assertions.assertThat(secondSpecEnhanced).isEqualTo(firstSpecEnhanced);
			Assertions.assertThat(secondSpecEnhanced).isEqualTo(secondSpecEnhanced);
		});
	}

	@Test
	public void test() {
		Conjunction<Object> firstConjunction = new Conjunction<>(testSimpleSpecifications());

		CustomSpecInterface firstConjunctionEnhanced =
				EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstConjunction);

		Specification<Object> secondConjunction = new Conjunction<>(testSimpleSpecifications());

		CustomSpecInterface secondConjunctionEnhanced =
				EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondConjunction);

		Assertions.assertThat(firstConjunctionEnhanced).isEqualTo(firstConjunctionEnhanced);
	}

	@Test
	public void equalsContract_conjunction() {
		Conjunction<Object> firstConjunction = new Conjunction<>(testSimpleSpecifications());

		CustomSpecInterface firstConjunctionEnhanced =
				EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstConjunction);

		Specification<Object> secondConjunction = new Conjunction<>(testSimpleSpecifications());

		CustomSpecInterface secondConjunctionEnhanced =
				EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondConjunction);

		Assertions.assertThat(firstConjunction).isEqualTo(firstConjunction);
		Assertions.assertThat(firstConjunction).isEqualTo(secondConjunction);
		Assertions.assertThat(firstConjunction).isNotEqualTo(firstConjunctionEnhanced);
		Assertions.assertThat(firstConjunction).isNotEqualTo(secondConjunctionEnhanced);

		Assertions.assertThat(secondConjunction).isEqualTo(firstConjunction);
		Assertions.assertThat(secondConjunction).isEqualTo(secondConjunction);
		Assertions.assertThat(secondConjunction).isNotEqualTo(firstConjunctionEnhanced);
		Assertions.assertThat(secondConjunction).isNotEqualTo(secondConjunctionEnhanced);

		Assertions.assertThat(firstConjunctionEnhanced).isNotEqualTo(firstConjunction);
		Assertions.assertThat(firstConjunctionEnhanced).isNotEqualTo(secondConjunction);
		Assertions.assertThat(firstConjunctionEnhanced).isEqualTo(firstConjunctionEnhanced);
		Assertions.assertThat(firstConjunctionEnhanced).isEqualTo(secondConjunctionEnhanced);

		Assertions.assertThat(secondConjunctionEnhanced).isNotEqualTo(firstConjunction);
		Assertions.assertThat(secondConjunctionEnhanced).isNotEqualTo(secondConjunction);
		Assertions.assertThat(secondConjunctionEnhanced).isEqualTo(firstConjunctionEnhanced);
		Assertions.assertThat(secondConjunctionEnhanced).isEqualTo(secondConjunctionEnhanced);
	}

	@Test
	public void equalsContract_disjunction() {
		Disjunction<Object> firstDisjunction = new Disjunction<>(testSimpleSpecifications());
		CustomSpecInterface firstDisjunctionEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstDisjunction);

		Disjunction<Object> secondDisjunction = new Disjunction<>(testSimpleSpecifications());
		CustomSpecInterface secondDisjunctionEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondDisjunction);

		Assertions.assertThat(firstDisjunction).isEqualTo(firstDisjunction);
		Assertions.assertThat(firstDisjunction).isEqualTo(secondDisjunction);
		Assertions.assertThat(firstDisjunction).isNotEqualTo(firstDisjunctionEnhanced);
		Assertions.assertThat(firstDisjunction).isNotEqualTo(secondDisjunctionEnhanced);

		Assertions.assertThat(secondDisjunction).isEqualTo(firstDisjunction);
		Assertions.assertThat(secondDisjunction).isEqualTo(secondDisjunction);
		Assertions.assertThat(secondDisjunction).isNotEqualTo(firstDisjunctionEnhanced);
		Assertions.assertThat(secondDisjunction).isNotEqualTo(secondDisjunctionEnhanced);

		Assertions.assertThat(firstDisjunctionEnhanced).isNotEqualTo(firstDisjunction);
		Assertions.assertThat(firstDisjunctionEnhanced).isNotEqualTo(secondDisjunction);
		Assertions.assertThat(firstDisjunctionEnhanced).isEqualTo(firstDisjunctionEnhanced);
		Assertions.assertThat(firstDisjunctionEnhanced).isEqualTo(secondDisjunctionEnhanced);

		Assertions.assertThat(secondDisjunctionEnhanced).isNotEqualTo(firstDisjunction);
		Assertions.assertThat(secondDisjunctionEnhanced).isNotEqualTo(secondDisjunction);
		Assertions.assertThat(secondDisjunctionEnhanced).isEqualTo(firstDisjunctionEnhanced);
		Assertions.assertThat(secondDisjunctionEnhanced).isEqualTo(secondDisjunctionEnhanced);
	}

	@Test
	public void equalsContract_twoEqualObjectsAreNotEqualIfTheyAreWrappedInDifferentInterfaces() {
		Specification<Object> firstSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface2 secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface2.class, firstSpec);

		Assertions.assertThat(customSpecInterface).isNotEqualTo(secondSpecInterface);
	}

	@Test
	public void equalsContract_twoDifferentObjectsAreNotEqualIfTheyAreWrappedInTheSameInterface() {
		Specification<Object> firstSpec = testSpecification(Equal.class, SEED_1);
		Specification<Object> secondSpec = testSpecification(Equal.class, SEED_2);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondSpec);

		Assertions.assertThat(customSpecInterface).isNotEqualTo(secondSpecInterface);
	}

	@Test
	public void equalsContract_twoEqualsObjectsAreNotEqualIfTheyAreWrappedInTheSameInterfacesInTheTransitiveContext() {
		Specification<Object> firstSpec = testSpecification(Equal.class);
		Specification<Object> secondSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(SpecExtendingCustomSpecInterface.class, secondSpec);

		Assertions.assertThat(customSpecInterface).isNotEqualTo(secondSpecInterface);
	}

	private List<Specification<Object>> testSimpleSpecifications() {
		return LIST_OF_SIMPLE_SPECIFICATION_TYPES.stream()
				.map(SimpleSpecificationGenerator::testSpecification)
				.collect(Collectors.toList());
	}

	interface CustomSpecInterface extends Specification<Object> {
	}
	
	interface SpecExtendingCustomSpecInterface extends CustomSpecInterface {
	}
	
	interface CustomSpecInterface2 extends Specification<Object> {
	}
}
