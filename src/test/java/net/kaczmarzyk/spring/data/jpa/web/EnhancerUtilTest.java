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

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Disjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.utils.SimpleSpecificationGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

import static net.kaczmarzyk.spring.data.jpa.utils.SimpleSpecificationGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

public class EnhancerUtilTest {

	@Test
	public void equalsContract_singleSimpleSpecification() {
		LIST_OF_SIMPLE_SPECIFICATION_TYPES.forEach(specType -> {
			Specification<Object> firstSpec = testSpecification(specType);
			Specification<Object> secondSpec = testSpecification(specType);

			CustomSpecInterface firstSpecEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
			CustomSpecInterface secondSpecEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondSpec);

			assertThat(firstSpec).isEqualTo(firstSpec);
			assertThat(firstSpec).isEqualTo(secondSpec);
			assertThat(firstSpec).isNotEqualTo(firstSpecEnhanced);
			assertThat(firstSpec).isNotEqualTo(secondSpecEnhanced);

			assertThat(secondSpec).isEqualTo(secondSpec);
			assertThat(secondSpec).isEqualTo(secondSpec);
			assertThat(secondSpec).isNotEqualTo(firstSpecEnhanced);
			assertThat(secondSpec).isNotEqualTo(secondSpecEnhanced);

			assertThat(firstSpecEnhanced).isNotEqualTo(firstSpec);
			assertThat(firstSpecEnhanced).isNotEqualTo(secondSpec);
			assertThat(firstSpecEnhanced).isEqualTo(firstSpecEnhanced);
			assertThat(firstSpecEnhanced).isEqualTo(secondSpecEnhanced);

			assertThat(secondSpecEnhanced).isNotEqualTo(firstSpec);
			assertThat(secondSpecEnhanced).isNotEqualTo(secondSpec);
			assertThat(secondSpecEnhanced).isEqualTo(firstSpecEnhanced);
			assertThat(secondSpecEnhanced).isEqualTo(secondSpecEnhanced);
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

		assertThat(firstConjunctionEnhanced).isEqualTo(firstConjunctionEnhanced);
	}

	@Test
	public void equalsContract_conjunction() {
		Conjunction<Object> firstConjunction = new Conjunction<>(testSimpleSpecifications());

		CustomSpecInterface firstConjunctionEnhanced =
				EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstConjunction);

		Specification<Object> secondConjunction = new Conjunction<>(testSimpleSpecifications());

		CustomSpecInterface secondConjunctionEnhanced =
				EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondConjunction);

		assertThat(firstConjunction).isEqualTo(firstConjunction);
		assertThat(firstConjunction).isEqualTo(secondConjunction);
		assertThat(firstConjunction).isNotEqualTo(firstConjunctionEnhanced);
		assertThat(firstConjunction).isNotEqualTo(secondConjunctionEnhanced);

		assertThat(secondConjunction).isEqualTo(firstConjunction);
		assertThat(secondConjunction).isEqualTo(secondConjunction);
		assertThat(secondConjunction).isNotEqualTo(firstConjunctionEnhanced);
		assertThat(secondConjunction).isNotEqualTo(secondConjunctionEnhanced);

		assertThat(firstConjunctionEnhanced).isNotEqualTo(firstConjunction);
		assertThat(firstConjunctionEnhanced).isNotEqualTo(secondConjunction);
		assertThat(firstConjunctionEnhanced).isEqualTo(firstConjunctionEnhanced);
		assertThat(firstConjunctionEnhanced).isEqualTo(secondConjunctionEnhanced);

		assertThat(secondConjunctionEnhanced).isNotEqualTo(firstConjunction);
		assertThat(secondConjunctionEnhanced).isNotEqualTo(secondConjunction);
		assertThat(secondConjunctionEnhanced).isEqualTo(firstConjunctionEnhanced);
		assertThat(secondConjunctionEnhanced).isEqualTo(secondConjunctionEnhanced);
	}

	@Test
	public void equalsContract_disjunction() {
		Disjunction<Object> firstDisjunction = new Disjunction<>(testSimpleSpecifications());
		CustomSpecInterface firstDisjunctionEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstDisjunction);

		Disjunction<Object> secondDisjunction = new Disjunction<>(testSimpleSpecifications());
		CustomSpecInterface secondDisjunctionEnhanced = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondDisjunction);

		assertThat(firstDisjunction).isEqualTo(firstDisjunction);
		assertThat(firstDisjunction).isEqualTo(secondDisjunction);
		assertThat(firstDisjunction).isNotEqualTo(firstDisjunctionEnhanced);
		assertThat(firstDisjunction).isNotEqualTo(secondDisjunctionEnhanced);

		assertThat(secondDisjunction).isEqualTo(firstDisjunction);
		assertThat(secondDisjunction).isEqualTo(secondDisjunction);
		assertThat(secondDisjunction).isNotEqualTo(firstDisjunctionEnhanced);
		assertThat(secondDisjunction).isNotEqualTo(secondDisjunctionEnhanced);

		assertThat(firstDisjunctionEnhanced).isNotEqualTo(firstDisjunction);
		assertThat(firstDisjunctionEnhanced).isNotEqualTo(secondDisjunction);
		assertThat(firstDisjunctionEnhanced).isEqualTo(firstDisjunctionEnhanced);
		assertThat(firstDisjunctionEnhanced).isEqualTo(secondDisjunctionEnhanced);

		assertThat(secondDisjunctionEnhanced).isNotEqualTo(firstDisjunction);
		assertThat(secondDisjunctionEnhanced).isNotEqualTo(secondDisjunction);
		assertThat(secondDisjunctionEnhanced).isEqualTo(firstDisjunctionEnhanced);
		assertThat(secondDisjunctionEnhanced).isEqualTo(secondDisjunctionEnhanced);
	}

	@Test
	public void equalsContract_twoEqualObjectsAreNotEqualIfTheyAreWrappedInDifferentInterfaces() {
		Specification<Object> firstSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface2 secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface2.class, firstSpec);

		assertThat(customSpecInterface).isNotEqualTo(secondSpecInterface);
	}

	@Test
	public void equalsContract_twoDifferentObjectsAreNotEqualIfTheyAreWrappedInTheSameInterface() {
		Specification<Object> firstSpec = testSpecification(Equal.class, SEED_1);
		Specification<Object> secondSpec = testSpecification(Equal.class, SEED_2);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, secondSpec);

		assertThat(customSpecInterface).isNotEqualTo(secondSpecInterface);
	}

	@Test
	public void equalsContract_twoEqualsObjectsAreNotEqualIfTheyAreWrappedInTheSameInterfacesInTheTransitiveContext() {
		Specification<Object> firstSpec = testSpecification(Equal.class);
		Specification<Object> secondSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(SpecExtendingCustomSpecInterface.class, secondSpec);

		assertThat(customSpecInterface).isNotEqualTo(secondSpecInterface);
	}

	@Test
	public void equalsContract_enhancedInterfaceIsNotEqualToNull() {
		Specification<Object> firstSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);

		assertThat(customSpecInterface).isNotEqualTo(null);
	}

	@Test
	public void tryToInvokeOriginalSpecificationMethodAsFallbackWhenProxyDoesNotDefineCallbackForGivenMethod_and() {
		Specification<Object> firstSpec = testSpecification(Equal.class);
		Specification<Object> secondSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(SpecExtendingCustomSpecInterface.class, secondSpec);

		Specification<Object> and = customSpecInterface.and(secondSpecInterface);

		assertThat(and)
				.isNotNull();
	}

	@Test
	public void tryToInvokeOriginalSpecificationMethodAsFallbackWhenProxyDoesNotDefineCallbackForGivenMethod_or() {
		Specification<Object> firstSpec = testSpecification(Equal.class);
		Specification<Object> secondSpec = testSpecification(Equal.class);

		CustomSpecInterface customSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(CustomSpecInterface.class, firstSpec);
		CustomSpecInterface secondSpecInterface = EnhancerUtil.wrapWithIfaceImplementation(SpecExtendingCustomSpecInterface.class, secondSpec);

		Specification<Object> and = customSpecInterface.or(secondSpecInterface);

		assertThat(and)
				.isNotNull();
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
