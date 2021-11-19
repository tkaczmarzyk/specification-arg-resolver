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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Tomasz Kaczmarzyk
 * @author Maciej Szewczyszyn
 */
public class InTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
	private Customer joeQuimby;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).build(em);

        joeQuimby = customer("Joe", "Quimby").golden().build(em); // Gender nor Weight nor Registration Date not specifed
    }
    
    @Test
    public void filtersByEnumValue_singleValue() {
        In<Customer> genderMale = new In<>(queryCtx, "gender", new String[] { "MALE" }, defaultConverter);
        List<Customer> males = customerRepo.findAll(genderMale);
        assertThat(males).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

        In<Customer> genderFemale = new In<>(queryCtx, "gender", new String[] { "FEMALE" }, defaultConverter);
        List<Customer> females = customerRepo.findAll(genderFemale);
        assertThat(females).hasSize(1).containsOnly(margeSimpson);

        In<Customer> genderOther = new In<>(queryCtx, "gender", new String[] { "OTHER" }, defaultConverter);
        List<Customer> others = customerRepo.findAll(genderOther);
        assertThat(others).hasSize(0);
    }
    
    @Test
    public void filtersWithTwoEnumValues() {
    	In<Customer> genderMaleOrFemale = new In<>(queryCtx, "gender", new String[] { "MALE", "FEMALE" }, defaultConverter);
        List<Customer> malesOrFemales = customerRepo.findAll(genderMaleOrFemale);
        assertThat(malesOrFemales).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByLongValue() {
    	In<Customer> simpsonsIds = new In<>(queryCtx, "id", new String[] { homerSimpson.getId().toString(), margeSimpson.getId().toString() }, defaultConverter);
    	
    	List<Customer> simpsons = customerRepo.findAll(simpsonsIds);
    	
	    assertThat(simpsons).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByLongValue_withAdditionalNonExistingValue() {
    	In<Customer> simpsonsIdsWithTrash = new In<>(queryCtx, "id", new String[] { "12345", homerSimpson.getId().toString(), margeSimpson.getId().toString() }, defaultConverter);
    	
    	List<Customer> simpsons = customerRepo.findAll(simpsonsIdsWithTrash);
    	
    	assertThat(simpsons).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	In<Customer> goldCustomers = new In<>(queryCtx, "gold", new String[] { "true" }, defaultConverter);
    	
    	List<Customer> simpsons = customerRepo.findAll(goldCustomers);
    	
    	assertThat(simpsons).hasSize(1).containsOnly(joeQuimby);
    }
    
    @Test
    public void filtersByIntegerValue() {
    	In<Customer> weights = new In<>(queryCtx, "weight", new String[] { "121", "65" }, defaultConverter);

    	List<Customer> found = customerRepo.findAll(weights);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByString() {
    	In<Customer> simpsons = new In<>(queryCtx, "lastName", new String[] { "Simpson", "Quimby" }, defaultConverter);
    	List<Customer> simpsonsFound = customerRepo.findAll(simpsons);
    	
    	assertThat(simpsonsFound).hasSize(3).containsOnly(homerSimpson, margeSimpson, joeQuimby);
    	
    	
    	In<Customer> lastNameS = new In<>(queryCtx, "lastName", new String[] { "s" }, defaultConverter);
    	List<Customer> found = customerRepo.findAll(lastNameS);
    	
    	assertThat(found).isEmpty();
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	In<Customer> registered1stMarch = new In<>(queryCtx, "registrationDate", new String[] { "2015-03-01" }, defaultConverter);
    	List<Customer> found = customerRepo.findAll(registered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    	
    	In<Customer> registered1stOr2ndMarch = new In<>(queryCtx, "registrationDate", new String[] { "2015-03-02", "2015-03-01" }, defaultConverter);
    	found = customerRepo.findAll(registered1stOr2ndMarch);
    	
    	assertThat(found).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	In<Customer> registered1stMarch = new In<>(queryCtx, "registrationDate", new String[] { "01-03-2015" },
    			Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
    	
    	List<Customer> found = customerRepo.findAll(registered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    	
    	In<Customer> registered1stOr2ndMarch = new In<>(queryCtx, "registrationDate", new String[] { "01-03-2015", "02-03-2015" },
    			Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
    	
    	found = customerRepo.findAll(registered1stOr2ndMarch);
    	
    	assertThat(found).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }
}
