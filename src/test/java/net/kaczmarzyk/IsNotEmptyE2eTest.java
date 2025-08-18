/*
 * Copyright 2014-2025 the original author or authors.
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
package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.IsNotEmpty;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class IsNotEmptyE2eTest extends E2eTestBase {

    @Controller
    public static class IsNotEmptySpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customersWithNotEmptyOrders")
        @ResponseBody
        public Object findByNotEmptyOrders(
                @Spec(path="orders", spec=IsNotEmpty.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customersWithNotEmptyPhoneNumbers")
        @ResponseBody
        public Object findByNotEmptyPhoneNumbers(
                @Spec(path="phoneNumbers", spec=IsNotEmpty.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @BeforeEach
    public void initData() {
        customer("Barry", "Benson")
                .phoneNumbers("123456789")
                .build(em);
        customer("Adam", "Flayman")
                .phoneNumbers("987654321")
                .build(em);
        customer("Vanessa", "Bloom")
                .orders(order("flowers"))
                .build(em);
    }

    @Test
    public void findsByNotEmptyCustomerOrders_oneToManyAssociation() throws Exception {
        mockMvc.perform(get("/customersWithNotEmptyOrders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Moe"))
                .andExpect(jsonPath("$[3].firstName").value("Ned"))
                .andExpect(jsonPath("$[4].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByNotEmptyCustomerPhoneNumbers_elementCollectionWithSimpleNonEntityValues() throws Exception {
        mockMvc.perform(get("/customersWithNotEmptyPhoneNumbers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Barry"))
                .andExpect(jsonPath("$[1].firstName").value("Adam"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
}
