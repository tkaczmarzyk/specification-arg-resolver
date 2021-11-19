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
package net.kaczmarzyk.spring.data.jpa;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;

/**
 * Base class for all Comparable Specification tests,
 * providing data and methods to make and test ComparableSpecifications.
 *
 * @author TP Diffenbach
 */
public abstract class ComparableTestBase extends IntegrationTestBase {
	
	protected static final String HEAVIER_THAN_MOE_DOUBLE = "65.21";
	protected static final String HEAVIER_THAN_MARGE = "56";
	
	protected Customer homerSimpson;
	protected Customer margeSimpson;
	protected Customer moeSzyslak;
	protected Customer joeQuimby;
	
	@BeforeEach
	public void initData() {
		homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
		margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
		moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).notGolden().build(em);
		joeQuimby = customer("Joe", "Quimby").golden().build(em); // Gender nor Weight nor Registration Date not specifed
	}
	
	/**
	 * Template Method Pattern to get an instance of the class under test.
	 *
	 * @param path the Specification's path
	 * @param value the value to compare against the database values
	 * @param converter the converter
	 * @return a Specification created with the passed-in parameters,
	 * 	e.g: return new Spec(path, value, config);
	 */
	protected abstract Specification<Customer> makeUUT(String path, String[] value, Converter converter);
	
	/**
	 * Convenience function to create an instance of the class under test,
	 * 	converting String into String[].
	 *
	 * @param path the Specification's path
	 * @param value the value to compare against the database values
	 * @param converter the converter
	 * @return a Specification, by calling subclass's makeUUT.
	 */
	protected Specification<Customer> makeUUT(String path, String value, Converter converter) {
		return makeUUT(path, new String[] { value }, converter);
	}
	
	/**
	 * Convenience function to create an instance of the class under test,
	 * 	with default Converter.
	 *
	 * @param path the Specification's path
	 * @param value the value to compare against the database values
	 * @return a Specification, by calling subclass's makeUUT, with an empty config.
	 */
	protected Specification<Customer> makeUUT(String path, String value) {
		return makeUUT(path, value, defaultConverter);
	}
	
	/**
	 * Create the Specification under test, filter with it, and assert the returned Customers
	 * are only those expected.
	 * @param path Specification path
	 * @param value Specification value
	 * @param expectedMembers the Customers we expect to be filtered in
	 */
	protected void assertFilterContainsOnlyExpectedMembers(String path, String value, Customer... expectedMembers) {
		assertFilterMembers(makeUUT(path, value), expectedMembers);
	}
	
	/**
	 * Create the Specification under test, filter with it, and assert it returns no Customers.
	 *
	 * This function is redundant, as assertFilterMembers(path, value) works just as well.
	 * But we retain it as the name suggests the expected outcome.
	 * @param path Specification path
	 * @param value Specification value
	 */
	protected void assertFilterIsEmpty(String path, String value) {
		assertFilterEmpty(makeUUT(path, value));
	}
	
	/**
	 * Create the Specification under test, filter with it, and assert the returned Customers.
	 * are only those expected
	 * @param path Specification path
	 * @param value Specification value
	 * @param dateFormat date-time format
	 * @param expectedMembers the Customers we expect to be filtered in
	 */
	protected void assertFilterContainsOnlyExpectedMembers(String path, String value, String dateFormat, Customer... expectedMembers) {
		assertFilterMembers(makeUUT(path, value, Converter.withDateFormat(dateFormat, OnTypeMismatch.EMPTY_RESULT, null)), expectedMembers);
	}
	
	/**
	 * Create the Specification under test, filter with it, and assert it returns no Customers.
	 *
	 * This function is redundant, as assertFilterMembers(path, value) works just as well.
	 * But we retain it as the name suggests the expected outcome.
	 * @param path Specification path
	 * @param converter the converter
	 * @param value Specification value
	 */
	protected void assertFilterIsEmpty(String path, String value, Converter converter) {
		assertFilterEmpty(makeUUT(path, value, converter));
	}
	
	/**
	 * Create the Specification under test, filter with it, and assert it returns no Customers.
	 *
	 * This function is redundant, as assertFilterMembers(path, value) works just as well.
	 * But we retain it as the name suggests the expected outcome.
	 * @param path Specification path
	 * @param dateFormat custom date format value
	 * @param value Specification value
	 */
	protected void assertFilterIsEmpty(String path, String value, String dateFormat) {
		assertFilterEmpty(makeUUT(path, value, Converter.withDateFormat(dateFormat, OnTypeMismatch.EMPTY_RESULT, null)));
	}
	
	@Test
	public void rejectsNotExistingEnumConstantName() {
		assertThrows(InvalidDataAccessApiUsageException.class, () -> customerRepo.findAll(makeUUT("gender", "ROBOT")));
	}
	
	@Test
	public void rejectsNonIntegerArguments() {
		assertThrows(InvalidDataAccessApiUsageException.class, () -> assertFilterIsEmpty("weight", "1.1"));
	}
	
	@Test
	public void rejectsNonNumericArguments() {
		assertThrows(InvalidDataAccessApiUsageException.class, () -> assertFilterIsEmpty("weightDouble", "one"));
	}
}