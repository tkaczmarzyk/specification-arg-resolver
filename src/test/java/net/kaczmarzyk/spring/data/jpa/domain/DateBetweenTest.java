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
public class DateBetweenTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer moeSzyslak;
    
    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").registrationDate(2014, 03, 07).build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 12).build(em);
        moeSzyslak = customer("Moe", "Szyslak").registrationDate(2014, 03, 18).build(em);
    }
    
    @Test
    public void filtersByRegistrationDateWithDefaultDateFormat() throws ParseException {
        DateBetween<Customer> between6and13 = new DateBetween<>(queryCtx, "registrationDate", new String[] { "2014-03-06", "2014-03-13" }, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(between6and13);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
        
        DateBetween<Customer> between11and19 = new DateBetween<>(queryCtx, "registrationDate", new String[] { "2014-03-11", "2014-03-19" }, defaultConverter);
        
        result = customerRepo.findAll(between11and19);
        assertThat(result)
            .hasSize(2)
            .containsOnly(margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByRegistrationDateWithCustomDateFormat() throws ParseException {
        DateBetween<Customer> between8and13 = new DateBetween<>(queryCtx, "registrationDate", new String[] {"08-03-2014", "13-03-2014"},
        		Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
        
        List<Customer> result = customerRepo.findAll(between8and13);
        assertThat(result)
            .hasSize(1)
            .containsOnly(margeSimpson);
    }
    
    @Test
    public void rejectsTooFewArguments() throws ParseException {
        assertThrows(IllegalArgumentException.class,
                () -> new DateBetween<>(queryCtx, "path", new String[] { "2014-03-10" }, defaultConverter));
    }
    
    @Test
    public void rejectsTooManyArguments() throws ParseException {
        assertThrows(IllegalArgumentException.class,
                () -> new DateBetween<>(queryCtx, "path", new String[] { "2014-03-10", "2014-03-11", "2014-03-11" }, defaultConverter));
    }
}
