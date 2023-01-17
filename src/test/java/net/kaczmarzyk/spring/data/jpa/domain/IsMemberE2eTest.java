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
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class IsMemberE2eTest extends E2eTestBase {

    @Controller
    private static class IsMemberSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "stringMember")
        @ResponseBody
        public Object findByMemberOfStringCollection(
                @Spec(path="phoneNumbers", params = "stringMember", spec=IsMember.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "longMember")
        @ResponseBody
        public Object findByMemberOfLongCollection(
                @Spec(path="luckyNumbers", params = "longMember", spec=IsMember.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @Before
    public void initData() {
        homerSimpson.addPhoneNumber("123456789");
        homerSimpson.addLuckyNumber(12L);

        maggieSimpson.addPhoneNumber("444444444");
        maggieSimpson.addLuckyNumber(1L);

        margeSimpson.addPhoneNumber("123456789");
        margeSimpson.addLuckyNumber(8L);

        moeSzyslak.addPhoneNumber("222222222");
        moeSzyslak.addLuckyNumber(25L);
        moeSzyslak.addLuckyNumber(8L);
    }

    @Test
    public void filtersByMemberOfStringCollection() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("stringMember", "123456789")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void filtersByMemberOfLongCollection() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("longMember", "8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
}