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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
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
public class IsFalseE2eTest extends E2eTestBase {

    @Controller
    private static class IsFalseSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @ResponseBody
        @RequestMapping(value = "/nonGoldenCustomers")
        public Object findByFalseValueOfPrimitiveBooleanType(
                @Spec(path="gold", spec=IsFalse.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @ResponseBody
        @RequestMapping(value = "/nonGoldenCustomersObj")
        public Object findByFalseValueOfObjectBooleanType(
                @Spec(path="goldObj", spec=IsFalse.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @BeforeEach
    public void initData() {
        customer("Barry", "Benson")
                .golden()
                .build(em);
        customer("Vanessa", "Bloom")
                .notGolden()
                .build(em);
    }

    @Test
    public void findsByFalseValueOfPrimitiveBooleanType() throws Exception {
        customer("Adam", "Flayman")
                .notGolden()
                .build(em);

        mockMvc.perform(get("/nonGoldenCustomers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Moe"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[6].firstName").value("Adam"))
                .andExpect(jsonPath("$[7]").doesNotExist());
    }

    @Test
    public void findsByFalseValueOfObjectBooleanType() throws Exception {
        customer("Larry", "Buzzwell")
                .notGolden()
                .build(em);

        mockMvc.perform(get("/nonGoldenCustomersObj")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Moe"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[6].firstName").value("Larry"))
                .andExpect(jsonPath("$[7]").doesNotExist());
    }
}
