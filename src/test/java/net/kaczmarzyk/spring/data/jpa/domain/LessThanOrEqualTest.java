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
package net.kaczmarzyk.spring.data.jpa.domain;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;


/**
 * @author Tomasz Kaczmarzyk
 * @author Maciej Szewczyszyn
 * @author TP Diffenbach
 */
public class LessThanOrEqualTest extends IntegrationTestBase {

    private static final String HEAVIER_THAN_MOE_DOUBLE = "65.21";
    private static final String MOES_FLOAT_WEIGHT = "65.1";
	private Customer homerSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
    private Customer joeQuimby;

    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).notGolden().build(em);

        joeQuimby = customer("Joe", "Quimby").golden().build(em); // Gender nor Weight nor Registration Date not specifed
}
    
    private Specification<Customer> make(String path, String value) {
    	return new LessThanOrEqual<Customer>(path, new String[] { value });
    }
    
    private Specification<Customer> make(String path, String value, String config) {
    	return new LessThanOrEqual<Customer>(path, new String[] { value }, new String[] { config });
    }
    
    private void assertFilterMembers(String path, String value, Customer... members) {
    	assertFilterMembers(make(path, value), members);
    }
    
    private void assertFilterEmpty(String path, String value) {
    	assertFilterEmpty(make(path, value));
    }
    
    private void assertFilterMembers(String path, String value, String config, Customer... members) {
    	assertFilterMembers(make(path, value, config), members);
    }
    
    private void assertFilterEmpty(String path, String value, String config) {
    	assertFilterEmpty(make(path, value, config));
    }
    
    @Test
    public void filtersByEnumValue() {
        assertFilterMembers("gender", "MALE", homerSimpson, moeSzyslak);

        assertFilterMembers("gender", "FEMALE", homerSimpson, moeSzyslak, margeSimpson);

        assertFilterMembers("gender", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterMembers("genderAsString", "MALE", homerSimpson, moeSzyslak, margeSimpson);

        assertFilterMembers("genderAsString", "FEMALE", margeSimpson);

        assertFilterMembers("genderAsString", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void rejectsNotExistingEnumConstantName() {
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("could not find value ROBOT for enum class Gender");
        customerRepo.findAll(make("gender", "ROBOT"));
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterMembers("id", moeSzyslak.getId().toString(), moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterMembers("weightLong", String.valueOf(margeSimpson.getWeightLong()), margeSimpson, joeQuimby); // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterMembers("weight", margeSimpson.getWeight().toString(), margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterMembers("weightInt", String.valueOf(margeSimpson.getWeightInt()), margeSimpson, joeQuimby);  // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterMembers("weightDouble", HEAVIER_THAN_MOE_DOUBLE, moeSzyslak, margeSimpson);
    	assertFilterMembers("weightDouble", margeSimpson.getWeightDouble().toString(), margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterMembers("weightFloat", MOES_FLOAT_WEIGHT, margeSimpson, moeSzyslak, joeQuimby);
    	assertFilterMembers("weightFloat", Float.toString(margeSimpson.getWeightFloat()), margeSimpson, joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	assertFilterMembers("gold", "true", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    	
    	// filters gold... or no gold, as (true >= false) == true
    	assertFilterMembers("gold", "false", homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterMembers("goldObj", "true", joeQuimby, moeSzyslak);
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterMembers("goldObj", "false", moeSzyslak);
    }
    
    @Test
    public void filtersByString() {
    	assertFilterMembers("lastName", "Szyslak", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    	
    	assertFilterMembers("lastName", "S", joeQuimby);
    	
    	// but with lower case...
    	assertFilterMembers("lastName", "s", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterMembers("registrationDate", "2015-03-01", homerSimpson, margeSimpson);
    	
    	assertFilterMembers("registrationDate", "2015-03-02", moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterMembers("registrationDate", "01-03-2015", "dd-MM-yyyy", homerSimpson, margeSimpson);
    	assertFilterMembers("registrationDate", "03-03-2015", "dd-MM-yyyy", moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void rejectsInvalidConfig_zeroArguments() {
    	String[] emptyConfig = new String[] {};

    	expectedException.expect(IllegalArgumentException.class);
    	
    	new GreaterThanOrEqual<>("registrationDate", new String[] { "01-03-2015" }, emptyConfig);
    }
    
    @Test
    public void rejectsInvalidConfig_tooManyArgument() {
    	String[] invalidConfig = new String[] {"yyyy-MM-dd", "unexpected"};
    	
    	expectedException.expect(IllegalArgumentException.class);
    	
    	new GreaterThanOrEqual<>("registrationDate", new String[] { "01-03-2015" }, invalidConfig);
    }
}
