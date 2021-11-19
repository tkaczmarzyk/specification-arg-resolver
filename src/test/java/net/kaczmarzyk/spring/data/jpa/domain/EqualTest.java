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
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author Tomasz Kaczmarzyk
 * @author Maciej Szewczyszyn
 */
public class EqualTest extends IntegrationTestBase {

    protected Customer homerSimpson;
    protected Customer margeSimpson;
    protected Customer moeSzyslak;
    protected Customer joeQuimby;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).build(em);

        joeQuimby = customer("Joe", "Quimby").golden().build(em); // Gender nor Weight nor Registration Date not specifed
    }
    
    @Test
    public void filtersByEnumValue() {
        Equal<Customer> genderMale = new Equal<>(queryCtx, "gender", new String[] { "MALE" }, defaultConverter);
        List<Customer> males = customerRepo.findAll(genderMale);
        assertThat(males).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

        Equal<Customer> genderFemale = new Equal<>(queryCtx, "gender", new String[] { "FEMALE" }, defaultConverter);
        List<Customer> females = customerRepo.findAll(genderFemale);
        assertThat(females).hasSize(1).containsOnly(margeSimpson);

        Equal<Customer> genderOther = new Equal<>(queryCtx, "gender", new String[] { "OTHER" }, defaultConverter);
        List<Customer> others = customerRepo.findAll(genderOther);
        assertThat(others).hasSize(0);
    }

    @Test
    public void rejectsNotExistingEnumConstantName() {
        Equal<Customer> genderRobot = new Equal<>(queryCtx, "gender", new String[] { "ROBOT" }, defaultConverter);
        assertThrows(InvalidDataAccessApiUsageException.class, () ->customerRepo.findAll(genderRobot));
    }
    
    @Test
    public void filtersByLongValue() {
    	Equal<Customer> homerId = new Equal<>(queryCtx, "id", new String[] { homerSimpson.getId().toString() }, defaultConverter);
    	
    	List<Customer> homers = customerRepo.findAll(homerId);
    	
    	assertThat(homers).hasSize(1).containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	Equal<Customer> homerId = new Equal<>(queryCtx, "weightLong", new String[] { "121" }, defaultConverter);
    	
    	List<Customer> homers = customerRepo.findAll(homerId);
    	
    	assertThat(homers).hasSize(1).containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByIntegerValue() {
    	Equal<Customer> weight121 = new Equal<>(queryCtx, "weight", new String[] { "121" }, defaultConverter);

    	List<Customer> found = customerRepo.findAll(weight121);
    	
    	assertThat(found).hasSize(1).containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	Equal<Customer> weight121 = new Equal<>(queryCtx, "weightInt", new String[] { "121" }, defaultConverter);

    	List<Customer> found = customerRepo.findAll(weight121);
    	
    	assertThat(found).hasSize(1).containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	Equal<Customer> gold = new Equal<>(queryCtx, "gold", new String[] { "true" }, defaultConverter);

    	List<Customer> found = customerRepo.findAll(gold);
    	
    	assertThat(found).hasSize(1).containsOnly(joeQuimby);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	Equal<Customer> gold = new Equal<>(queryCtx, "goldObj", new String[] { "true" }, defaultConverter);

    	List<Customer> found = customerRepo.findAll(gold);
    	
    	assertThat(found).hasSize(1).containsOnly(joeQuimby);
    }
    
    @Test
    public void filtersByString() {
    	Equal<Customer> simpsons = new Equal<>(queryCtx, "lastName", new String[] { "Simpson" }, defaultConverter);
    	List<Customer> simpsonsFound = customerRepo.findAll(simpsons);
    	
    	assertThat(simpsonsFound).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    	
    	
    	Equal<Customer> lastNameS = new Equal<>(queryCtx, "lastName", new String[] { "s" }, defaultConverter);
    	List<Customer> found = customerRepo.findAll(lastNameS);
    	
    	assertThat(found).isEmpty();
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	Equal<Customer> registered1stMarch = new Equal<>(queryCtx, "registrationDate", new String[] { "2015-03-01" }, defaultConverter);
    	List<Customer> found = customerRepo.findAll(registered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    	
    	Equal<Customer> registered2ndMarch = new Equal<>(queryCtx, "registrationDate", new String[] { "2015-03-02" }, defaultConverter);
    	found = customerRepo.findAll(registered2ndMarch);
    	
    	assertThat(found).hasSize(1).containsOnly(moeSzyslak);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	Equal<Customer> registered1stMarch = new Equal<>(queryCtx, "registrationDate", new String[] { "01-03-2015" },
    			Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
    	List<Customer> found = customerRepo.findAll(registered1stMarch);
    	
    	assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByDouble() {
    	Equal<Customer> homerWeightDouble = new Equal<>(queryCtx, "weightDouble", new String[] { String.valueOf(homerSimpson.getWeightDouble()) }, defaultConverter);
    	assertFilterMembers(homerWeightDouble, homerSimpson);
    }

}
