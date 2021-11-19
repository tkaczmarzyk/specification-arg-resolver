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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Tomasz Kaczmarzyk
 */
public class NotInTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
	private Customer joeQuimby;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).build(em);

        joeQuimby = customer("Joe", "Quimby").gender(Gender.OTHER).registrationDate(2019,  03,  15).weight(100).golden().build(em);
    }
    
    @Test
    public void filtersByEnumValue_singleValue() {
        NotIn<Customer> genderNotMale = new NotIn<>(queryCtx, "gender", new String[] { "MALE" }, defaultConverter);
        List<Customer> notMales = customerRepo.findAll(genderNotMale);
        assertThat(notMales).hasSize(2).containsOnly(margeSimpson, joeQuimby);

        NotIn<Customer> genderNotFemale = new NotIn<>(queryCtx, "gender", new String[] { "FEMALE" }, defaultConverter);
        List<Customer> notFemales = customerRepo.findAll(genderNotFemale);
        assertThat(notFemales).hasSize(3).containsOnly(homerSimpson, moeSzyslak, joeQuimby);

        NotIn<Customer> genderNotOther = new NotIn<>(queryCtx, "gender", new String[] { "OTHER" }, defaultConverter);
        List<Customer> notOthers = customerRepo.findAll(genderNotOther);
        assertThat(notOthers).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersWithTwoEnumValues() {
    	NotIn<Customer> genderNotMaleNorFemale = new NotIn<>(queryCtx, "gender", new String[] { "MALE", "FEMALE" }, defaultConverter);
        List<Customer> notMalesNorFemales = customerRepo.findAll(genderNotMaleNorFemale);
        assertThat(notMalesNorFemales).hasSize(1).containsOnly(joeQuimby);
    }

    @Test
    public void filtersByLongValue() {
    	NotIn<Customer> simpsonsIds = new NotIn<>(queryCtx, "id", new String[] { homerSimpson.getId().toString(), margeSimpson.getId().toString() }, defaultConverter);
    	
    	List<Customer> notSimpsons = customerRepo.findAll(simpsonsIds);
    	
    	assertThat(notSimpsons).hasSize(2).containsOnly(moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByLongValue_withAdditionalNonExistingValue() {
    	NotIn<Customer> notSimpsonsIdsWithTrash = new NotIn<>(queryCtx, "id", new String[] { "12345", homerSimpson.getId().toString(), margeSimpson.getId().toString() }, defaultConverter);
    	
    	List<Customer> notSimpsons = customerRepo.findAll(notSimpsonsIdsWithTrash);
    	
    	assertThat(notSimpsons).hasSize(2).containsOnly(moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	NotIn<Customer> notGoldCustomers = new NotIn<>(queryCtx, "gold", new String[] { "true" }, defaultConverter);
    	
    	List<Customer> notGold = customerRepo.findAll(notGoldCustomers);
    	
    	assertThat(notGold).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByIntegerValue() {
    	NotIn<Customer> weights = new NotIn<>(queryCtx, "weight", new String[] { "121", "65" }, defaultConverter);

    	List<Customer> found = customerRepo.findAll(weights);
    	
    	assertThat(found).hasSize(2).containsOnly(margeSimpson, joeQuimby);
    }
    
    @Test
    public void filtersByString() {
    	NotIn<Customer> notSimpsonNorQuimby = new NotIn<>(queryCtx, "lastName", new String[] { "Simpson", "Quimby" }, defaultConverter);
    	List<Customer> found = customerRepo.findAll(notSimpsonNorQuimby);
    	
    	assertThat(found).hasSize(1).containsOnly(moeSzyslak);
    	
    	
    	NotIn<Customer> lastNameNotS = new NotIn<>(queryCtx, "lastName", new String[] { "s" }, defaultConverter);
    	found = customerRepo.findAll(lastNameNotS);
    	
    	assertThat(found).hasSize(4);
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	NotIn<Customer> notRegistered1stMarch = new NotIn<>(queryCtx, "registrationDate", new String[] { "2015-03-01" }, defaultConverter);
    	List<Customer> found = customerRepo.findAll(notRegistered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(moeSzyslak, joeQuimby);
    	
    	NotIn<Customer> notRegistered1stNor2ndMarch = new NotIn<>(queryCtx, "registrationDate", new String[] { "2015-03-02", "2015-03-01" }, defaultConverter);
    	found = customerRepo.findAll(notRegistered1stNor2ndMarch);
    	
    	assertThat(found).hasSize(1).containsOnly(joeQuimby);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	NotIn<Customer> notRegistered1stMarch = new NotIn<>(queryCtx, "registrationDate", new String[] { "01-03-2015" },
    			Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
    	
    	List<Customer> found = customerRepo.findAll(notRegistered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(moeSzyslak, joeQuimby);
    	
    	NotIn<Customer> notRegistered1stNor2ndMarch = new NotIn<>(queryCtx, "registrationDate", new String[] { "01-03-2015", "02-03-2015" },
    			Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
    	
    	found = customerRepo.findAll(notRegistered1stNor2ndMarch);
    	
    	assertThat(found).hasSize(1).containsOnly(joeQuimby);
    }
}
