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
import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;


/**
 * @author Tomasz Kaczmarzyk
 */
public class DateBeforeTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer moeSzyslak;
    
    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").registrationDate(2014, 03, 07).build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 12).build(em);
        moeSzyslak = customer("Moe", "Szyslak").registrationDate(2014, 03, 18).build(em);
    }
    
    @Test
    public void filtersByRegistrationDateWithDefaultDateFormat() throws ParseException {
        DateBefore<Customer> before13th = new DateBefore<>("registrationDate", "2014-03-13");
        
        List<Customer> result = customerRepo.findAll(before13th);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
        
        DateBefore<Customer> before10th = new DateBefore<>("registrationDate", "2014-03-10");
        
        result = customerRepo.findAll(before10th);
        assertThat(result)
            .hasSize(1)
            .containsOnly(homerSimpson);
    }
    
    @Test
    public void filtersByRegistrationDateWithCustomDateFormat() throws ParseException {
        DateBefore<Customer> before13th = new DateBefore<>("registrationDate", new String[] {"13-03-2014"}, new String[] {"dd-MM-yyyy"});
        
        List<Customer> result = customerRepo.findAll(before13th);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidNumberOfArguments() throws ParseException {
        new DateBefore<>("path", "2014-03-10", "2014-03-11");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingArgument() throws ParseException {
        new DateBefore<>("path", new String[] {});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidNumberOfConfigArguments() throws ParseException {
        new DateBefore<>("path", new String[] {"2014-03-10"}, new String[] {"yyyy-MM-dd", "MM-dd-yyyy"});
    }
}
