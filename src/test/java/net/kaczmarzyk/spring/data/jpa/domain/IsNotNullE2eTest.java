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
public class IsNotNullE2eTest extends E2eTestBase {

    @Controller
    @RequestMapping("/isNotNull")
    private static class IsNotNullSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @ResponseBody
        @RequestMapping(value = "/customersWithNicknames")
        public Object findByNotNullNickname(
                @Spec(path="nickName", spec=IsNotNull.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @BeforeEach
    public void initData() {
        customer("Barry", "Benson")
                .nickName("Bee")
                .build(em);
        customer("Vanessa", "Bloom")
                .build(em);
    }

    @Test
    public void findsByNotNullNickname() throws Exception {
        mockMvc.perform(get("/isNotNull/customersWithNicknames")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Minnie"))
                .andExpect(jsonPath("$[3].firstName").value("Ned"))
                .andExpect(jsonPath("$[4].firstName").value("Barry"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }
}
