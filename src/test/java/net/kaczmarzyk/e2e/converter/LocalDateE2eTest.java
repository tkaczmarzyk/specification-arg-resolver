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
package net.kaczmarzyk.e2e.converter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.kaczmarzyk.E2eTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


public class LocalDateE2eTest extends E2eTestBase {
    @Controller
    public static class LocalDateSpecsController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "birthDateBefore")
        @ResponseBody
        public Object findCustomersBornBefore_defaultDateFormat(
                @Spec(path="birthDate", params="birthDateBefore", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = "birthDateBefore_customFormat")
        @ResponseBody
        public Object findCustomersBornBefore_customDateFormat(
                @Spec(path="birthDate", params="birthDateBefore_customFormat", config="dd/MM/yyyy", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "birthDateAfter")
        @ResponseBody
        public Object findCustomersBornAfter_defaultDateFormat(
                @Spec(path="birthDate", params="birthDateAfter", spec= GreaterThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = "birthDateAfter_customFormat")
        @ResponseBody
        public Object findCustomersBornAfter_customDateFormat(
                @Spec(path="birthDate", params="birthDateAfter_customFormat", config="dd/MM/yyyy", spec= GreaterThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = { "birthDateAfter", "birthDateBefore" })
        @ResponseBody
        public Object findByBirhDateBetween(
                @Spec(path="birthDate", params={ "birthDateAfter", "birthDateBefore"}, spec=Between.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = { "birthDateAfter_customFormat", "birthDateBefore_customFormat" })
        @ResponseBody
        public Object findByBirhDateBetweenWithCustomFormat(
                @Spec(path="birthDate", params={ "birthDateAfter_customFormat", "birthDateBefore_customFormat"}, config="dd/MM/yyyy", spec=Between.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }
    
    @Test
    public void findsByDateBeforeWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("birthDateBefore_customFormat", "12/07/1972")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByDateBeforeWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("birthDateBefore", "1972-07-12")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByDateAfterWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("birthDateAfter", "1996-03-26")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Maggie"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateAfterWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("birthDateAfter_customFormat", "26/03/1996")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Maggie"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateBetweenWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
        						.param("birthDateAfter", "1970-03-10")
        		 				.param("birthDateBefore", "1972-07-14")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateBetweenWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
        						.param("birthDateAfter_customFormat", "10/03/1970")
        		 				.param("birthDateBefore_customFormat", "14/07/1972")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
}
