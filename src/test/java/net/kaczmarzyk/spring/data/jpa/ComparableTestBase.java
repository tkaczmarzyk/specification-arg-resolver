/**
 * Copyright 2014-2016 the original author or authors.
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

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

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

	@Before
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
	 * @param config the config
	 * @return a Specification created with the passed-in parameters, 
	 * 	e.g: return new Spec(path, value, config);
	 */
	protected abstract Specification<Customer> makeUUT(String path, String[] value, String[] config);

	/**
	 * Convenience function to create an instance of the class under test, 
	 * 	converting String into String[].
	 * 
	 * @param path the Specification's path
	 * @param value the value to compare against the database values 
	 * @param config the config
	 * @return a Specification, by calling subclass's makeUUT.
	 */
	protected Specification<Customer> makeUUT(String path, String value, String[] config) {
		return makeUUT(path, new String[] { value }, config);
	}
	
	/**
	 * Convenience function to create an instance of the class under test, 
	 * 	converting String into String[].
	 * 
	 * @param path the Specification's path
	 * @param value the value to compare against the database values 
	 * @param config the config
	 * @return a Specification, by calling subclass's makeUUT.
	 */
	protected Specification<Customer> makeUUT(String path, String value, String config) {
		return makeUUT(path, new String[] { value }, config == null ? null : new String[] { config });
	}

	/**
	 * Convenience function to create an instance of the class under test, 
	 * 	converting String into String[].
	 * 
	 * @param path the Specification's path
	 * @param value the value to compare against the database values 
	 * @return a Specification, by calling subclass's makeUUT, with an empty config.
	 */
	protected Specification<Customer> makeUUT(String path, String value) {
		return makeUUT(path, value, (String[]) null);
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
	 * @config the Specification's configuration
	 * @param expectedMembers the Customers we expect to be filtered in
	 */
	protected void assertFilterContainsOnlyExpectedMembers(String path, String value, String config, Customer... members) {
		assertFilterMembers(makeUUT(path, value, config), members);
	}

	/**
	 * Create the Specification under test, filter with it, and assert it returns no Customers.
	 * 
	 * This function is redundant, as assertFilterMembers(path, value) works just as well. 
	 * But we retain it as the name suggests the expected outcome.
	 * @param path Specification path
	 * @config the Specification's configuration
	 * @param value Specification value 
	 */
	protected void assertFilterIsEmpty(String path, String value, String config) {
		assertFilterEmpty(makeUUT(path, value, config));
	}
	
    @Test
    public void rejectsNotExistingEnumConstantName() {
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("could not find value ROBOT for enum class Gender");
        customerRepo.findAll(makeUUT("gender", "ROBOT"));
    }
	
    @Test
    public void rejectsInvalidConfig_zeroArguments() {
    	String[] emptyConfig = new String[] {};

    	expectedException.expect(IllegalArgumentException.class);
    	
    	makeUUT("registrationDate", "01-03-2015", emptyConfig);
    }
    
    @Test
    public void rejectsInvalidConfig_tooManyArgument() {
    	String[] invalidConfig = new String[] {"yyyy-MM-dd", "unexpected"};
    	
    	expectedException.expect(IllegalArgumentException.class);
    	
    	makeUUT("registrationDate", "01-03-2015", invalidConfig);
    }

    @Test
    public void rejectsNonIntegerArguments() {
    	expectedException.expect(InvalidDataAccessApiUsageException.class);
    	assertFilterIsEmpty("weight", "1.1");
    }
    
    @Test
    public void rejectsNonNumericArguments() {
    	expectedException.expect(InvalidDataAccessApiUsageException.class);
    	assertFilterIsEmpty("weightDouble", "one");
    }
}