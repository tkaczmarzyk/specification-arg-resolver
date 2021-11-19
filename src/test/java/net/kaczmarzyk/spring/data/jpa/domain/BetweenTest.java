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

import java.text.ParseException;
import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tomasz Kaczmarzyk
 */
public class BetweenTest extends IntegrationTestBase {

	Customer homerSimpson;
    Customer margeSimpson;
    Customer moeSzyslak;
    Customer benderRodriguez;
    
    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson")
        		.gender(Gender.MALE)
        		.registrationDate(2014, 03, 07)
        		.weight(300)
        		.build(em);
        margeSimpson = customer("Marge", "Simpson")
        		.gender(Gender.FEMALE)
        		.registrationDate(2014, 03, 12)
        		.weight(80)
        		.build(em);
        moeSzyslak = customer("Moe", "Szyslak")
        		.gender(Gender.MALE)
        		.registrationDate(2014, 03, 18)
        		.weight(90)
        		.build(em);
        benderRodriguez = customer("Bender", "Rodriguez")
        		.gender(Gender.OTHER) // Sorry, Bender, it's just for the test purpose!
        		.registrationDate(3014, 03, 18)
        		.weight(150)
        		.build(em);
    }
    
    @Test
    public void filtersByIntRange() throws Exception {
    	Between<Customer> between70and110 = new Between<>(queryCtx, "weightInt", new String[] { "70", "100" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(between70and110);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, moeSzyslak);
        
        Between<Customer> between85and400 = new Between<>(queryCtx, "weightInt", new String[] { "85", "400" }, defaultConverter);
        
        result = customerRepo.findAll(between85and400);
        assertThat(result)
            .hasSize(3)
            .containsOnly(homerSimpson, moeSzyslak, benderRodriguez);
    }
    
    @Test
    public void filtersByFloatRange() throws Exception {
    	Between<Customer> between70and110 = new Between<>(queryCtx, "weightFloat", new String[] { "70", "100" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(between70and110);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, moeSzyslak);
        
        Between<Customer> between85and400 = new Between<>(queryCtx, "weightFloat", new String[] { "85", "300.2" }, defaultConverter);
        
        result = customerRepo.findAll(between85and400);
        assertThat(result)
	        .hasSize(3)
	        .containsOnly(homerSimpson, moeSzyslak, benderRodriguez);
    }
    
    @Test
    public void filtersByDoubleRange() throws Exception {
    	Between<Customer> between70and110 = new Between<>(queryCtx, "weightDouble", new String[] { "70", "100" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(between70and110);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, moeSzyslak);
        
        Between<Customer> between85and400 = new Between<>(queryCtx, "weightDouble", new String[] { "85", "300.2" }, defaultConverter);
        
        result = customerRepo.findAll(between85and400);
        assertThat(result)
	        .hasSize(3)
	        .containsOnly(homerSimpson, moeSzyslak, benderRodriguez);
    }
    
    @Test
    public void filtersByBigDecimalRange() throws Exception {
    	Between<Customer> between70and110 = new Between<>(queryCtx, "weightBigDecimal", new String[] { "70", "100" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(between70and110);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, moeSzyslak);
        
        Between<Customer> between85and400 = new Between<>(queryCtx, "weightBigDecimal", new String[] { "85", "300.31" }, defaultConverter);
        
        result = customerRepo.findAll(between85and400);
        assertThat(result)
	        .hasSize(3)
	        .containsOnly(homerSimpson, moeSzyslak, benderRodriguez);
    }
    
    @Test
    public void filtersByEnumRange_ordinal() throws Exception {
    	Between<Customer> betweenFemaleAndOther = new Between<>(queryCtx, "gender", new String[] { "FEMALE", "OTHER" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(betweenFemaleAndOther);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, benderRodriguez);
    }
    
    @Test
    public void filtersByEnumRange_string() throws Exception {
    	Between<Customer> betweenMaleAndOther = new Between<>(queryCtx, "genderAsString", new String[] { "MALE", "OTHER" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(betweenMaleAndOther);
        assertThat(result)
            .hasSize(3)
            .containsOnly(homerSimpson, moeSzyslak, benderRodriguez);
    }
    
    @Test
    public void filtersByRegistrationDateWithDefaultDateFormat() throws ParseException {
        Between<Customer> between6and13 = new Between<>(queryCtx, "registrationDate", new String[] { "2014-03-06", "2014-03-13" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(between6and13);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
        
        Between<Customer> between11and19 = new Between<>(queryCtx, "registrationDate", new String[] { "2014-03-11", "2014-03-19" }, defaultConverter);
        
        result = customerRepo.findAll(between11and19);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByRegistrationDateWithCustomDateFormat() throws ParseException {
    	Between<Customer> between8and13 = new Between<>(queryCtx, "registrationDate", new String[] {"08-03-2014", "13-03-2014"},
        		Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
        
        List<Customer> result = customerRepo.findAll(between8and13);
        assertThat(result)
            .hasSize(1)
            .containsOnly(margeSimpson);
    }
    
    @Test
    public void rejectsTooFewArguments() throws ParseException {
        assertThrows(IllegalArgumentException.class,
                () -> new Between<>(queryCtx, "path", new String[] { "2014-03-10" }, defaultConverter));
    }
    
    @Test
    public void rejectsTooManyArguments() throws ParseException {
        assertThrows(IllegalArgumentException.class,
                () -> new Between<>(queryCtx, "path", new String[] { "2014-03-10", "2014-03-11", "2014-03-11" }, defaultConverter));
    }
}
