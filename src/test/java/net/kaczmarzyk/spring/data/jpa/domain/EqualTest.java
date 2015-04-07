/**
 * Copyright 2014-2015 the original author or authors.
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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;


/**
 * @author Tomasz Kaczmarzyk
 * @author Maciej Szewczyszyn
 */
public class EqualTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
    private Customer joeQuimby;

    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).build(em);

        joeQuimby = customer("Joe", "Quimby").golden().build(em); // Gender nor Weight nor Registration Date not specifed
    }
    
    @Test
    public void filtersByEnumValue() {
        Equal<Customer> genderMale = new Equal<>("gender", new String[] { "MALE" });
        List<Customer> males = customerRepo.findAll(genderMale);
        assertThat(males).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

        Equal<Customer> genderFemale = new Equal<>("gender", new String[] { "FEMALE" });
        List<Customer> females = customerRepo.findAll(genderFemale);
        assertThat(females).hasSize(1).containsOnly(margeSimpson);

        Equal<Customer> genderOther = new Equal<>("gender", new String[] { "OTHER" });
        List<Customer> others = customerRepo.findAll(genderOther);
        assertThat(others).hasSize(0);
    }

    @Test
    public void rejectsNotExistingEnumConstantName() {
        Equal<Customer> genderRobot = new Equal<>("gender", new String[] { "ROBOT" });
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("could not find value ROBOT for enum class Gender");
        customerRepo.findAll(genderRobot);
    }
    
    @Test
    public void filtersByLongValue() {
    	Equal<Customer> homerId = new Equal<>("id", new String[] { homerSimpson.getId().toString() });
    	
    	List<Customer> homers = customerRepo.findAll(homerId);
    	
    	assertThat(homers).hasSize(1).containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByIntegerValue() {
    	Equal<Customer> weight121 = new Equal<>("weight", new String[] { "121" });

    	List<Customer> found = customerRepo.findAll(weight121);
    	
    	assertThat(found).hasSize(1).containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	Equal<Customer> gold = new Equal<>("gold", new String[] { "true" });

    	List<Customer> found = customerRepo.findAll(gold);
    	
    	assertThat(found).hasSize(1).containsOnly(joeQuimby);
    }
    
    @Test
    public void filtersByString() {
    	Equal<Customer> simpsons = new Equal<>("lastName", new String[] { "Simpson" });
    	List<Customer> simpsonsFound = customerRepo.findAll(simpsons);
    	
    	assertThat(simpsonsFound).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    	
    	
    	Equal<Customer> lastNameS = new Equal<>("lastName", new String[] { "s" });
    	List<Customer> found = customerRepo.findAll(lastNameS);
    	
    	assertThat(found).isEmpty();
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	Equal<Customer> registered1stMarch = new Equal<>("registrationDate", new String[] { "2015-03-01" });
    	List<Customer> found = customerRepo.findAll(registered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    	
    	Equal<Customer> registered2ndMarch = new Equal<>("registrationDate", new String[] { "2015-03-02" });
    	found = customerRepo.findAll(registered2ndMarch);
    	
    	assertThat(found).hasSize(1).containsOnly(moeSzyslak);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	Equal<Customer> registered1stMarch = new Equal<>("registrationDate", new String[] { "01-03-2015" }, new String[] { "dd-MM-yyyy" });
    	List<Customer> found = customerRepo.findAll(registered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }
    
    @Test
    public void rejectsInvalidConfig_zeroArguments() {
    	String[] emptyConfig = new String[] {};

    	expectedException.expect(IllegalArgumentException.class);
    	
    	new Equal<>("registrationDate", new String[] { "01-03-2015" }, emptyConfig);
    }
    
    @Test
    public void rejectsInvalidConfig_tooManyArgument() {
    	String[] invalidConfig = new String[] {"yyyy-MM-dd", "unexpected"};
    	
    	expectedException.expect(IllegalArgumentException.class);
    	
    	new Equal<>("registrationDate", new String[] { "01-03-2015" }, invalidConfig);
    }
}
