/**
 * Copyright 2014-2023 the original author or authors.
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

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.EqualDay;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class CalendarE2eTest extends E2eTestBase {
    @Controller
    public static class CalendarSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "registeredEqualDay")
        @ResponseBody
        public Object findCustomersRegisteredDayEqualWithDefaultConfig(
                @Spec(path="registrationDate", params="registeredEqualDay", spec=EqualDay.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredEqualDayCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredDayEqualWithCustomConfigContainingTime(
                @Spec(path="registrationDate", params="registeredEqualDayCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=EqualDay.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @Test
    public void findsByEqualDayUsingDefaultConfig() throws Exception {
        customer("Barry", "Benson")
                .registrationDate(2014, 3, 16)
                .build(em);
        customer("Adam", "Flayman")
                .registrationDate(2014, 3, 14, 23, 59, 59, 999)
                .build(em);

        mockMvc.perform(get("/customers")
                        .param("registeredEqualDay", "2014-03-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByEqualDayUsingCustomConfigIgnoringTime() throws Exception {
        customer("Barry", "Benson")
                .registrationDate(2014, 3, 16)
                .build(em);
        customer("Adam", "Flayman")
                .registrationDate(2014, 3, 14, 23, 59, 59, 999)
                .build(em);

        mockMvc.perform(get("/customers")
                        .param("registeredEqualDayCustomConfig", "2014-03-15T12:34:19")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
}
