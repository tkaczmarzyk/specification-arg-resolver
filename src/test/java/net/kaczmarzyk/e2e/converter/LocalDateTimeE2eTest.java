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

public class LocalDateTimeE2eTest extends E2eTestBase {
    @Controller
    public static class LocalDateSpecsController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "lastOrderTimeBefore")
        @ResponseBody
        public Object findCustomersWIthLastOrderBefore_defaultDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeBefore", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = "lastOrderTimeBefore_customFormat")
        @ResponseBody
        public Object findCustomersWIthLastOrderBefore_customDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeBefore_customFormat", config="yyyy/MM/dd', 'HH:mm", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "lastOrderTimeAfter")
        @ResponseBody
        public Object findCustomersWIthLastOrderAfter_defaultDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeAfter", spec= GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = "lastOrderTimeAfter_customFormat")
        @ResponseBody
        public Object findCustomersWithLastOrderAfter_customDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeAfter_customFormat", config="yyyy/MM/dd', 'HH:mm", spec= GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = { "lastOrderTimeAfter", "lastOrderTimeBefore" })
        @ResponseBody
        public Object findCustomersWithLastOrderTimeBetween_defaultDateTimePattern(
                @Spec(path="lastOrderTime", params={ "lastOrderTimeAfter", "lastOrderTimeBefore" }, spec= Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = { "lastOrderTimeAfter_customFormat", "lastOrderTimeBefore_customFormat" })
        @ResponseBody
        public Object findCustomersWithLastOrderTimeBetween_customDateTimePattern(
                @Spec(path="lastOrderTime", params={ "lastOrderTimeAfter_customFormat", "lastOrderTimeBefore_customFormat" }, config="yyyy/MM/dd', 'HH:mm", spec= Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

    }

    @Test
    public void findsByDateTimeBeforeWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeBefore_customFormat", "2017/08/22, 09:17")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeBeforeWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeBefore", "2016-10-17T18:29:00")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByDateTimeAfterWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter", "2017-11-21T11:12:59")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeAfterWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter_customFormat", "2017/11/21, 11:13")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeBetweenWithDefaultDateFormat() throws Exception {
    	mockMvc.perform(get("/customers")
				                .param("lastOrderTimeAfter", "2017-11-21T11:12:59")
				                .param("lastOrderTimeBefore", "2017-12-14T11:12:59")
				                .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeBetweenWithCustomDateFormat() throws Exception {
    	mockMvc.perform(get("/customers")
				                .param("lastOrderTimeAfter_customFormat", "2017/11/21, 11:12")
				                .param("lastOrderTimeBefore_customFormat", "2017/12/14, 11:12")
				                .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
    }
}
