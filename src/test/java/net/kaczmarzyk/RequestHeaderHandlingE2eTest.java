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
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Muhammed Ali Karakurt
 */
public class RequestHeaderHandlingE2eTest extends E2eTestBase {

    @Controller
    public static class TestController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers/reqHeader", headers = "customerId")
        @ResponseBody
        public Object findByIdHeader(
                @Spec(path = "id", headers = "customerId", spec = Equal.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }


        @RequestMapping(value = "/customers/reqHeader/param", headers = "gender", params = "lastName")
        @ResponseBody
        public Object findCustomersByGenderAndLastName(
                @And({
                        @Spec(path = "lastName", params = "lastName", spec = Equal.class),
                        @Spec(path = "gender", headers = "gender", spec = Equal.class)
                }) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers/reqHeader/pathVar/{customerOccupation}", headers = "gender")
        @ResponseBody
        public Object findCustomersByGenderAndOccupation(
                @And({
                        @Spec(path = "occupation", pathVars = "customerOccupation", spec = Equal.class),
                        @Spec(path = "gender", headers = "gender", spec = Equal.class)
                }) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers/reqHeaders")
        @ResponseBody
        public Object findCustomersByGenderAndNickName(
                @RequestHeader(value = "gender", required = false) String gender,
                @RequestHeader(value = "nickName", required = false) String nickName,
                @And({
                        @Spec(path = "nickName", headers = "nickName", spec = Equal.class),
                        @Spec(path = "gender", headers = "gender", spec = Equal.class)
                }) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @Test
    public void findByIdProvidedInRequestHeaders() throws Exception {
        // headers = "customerId"
        mockMvc.perform(get("/customers/reqHeader")
                        .header("customerId", homerSimpson.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findByGenderProvidedInHeaderAndLastNameProvidedInRequestParams() throws Exception {
        // headers = "gender", params = "lastName"
        mockMvc.perform(get("/customers/reqHeader/param?lastName=Simpson")
                        .header("gender", "FEMALE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
                .andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());

        mockMvc.perform(get("/customers/reqHeader/param?lastName=Simpson")
                        .header("gender", "MALE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findByGenderProvidedInHeaderAndOccupationProvidedInPathVariables() throws Exception {
        // headers = "gender",  pathVariables="customerOccupation"
        mockMvc.perform(get("/customers/reqHeader/pathVar/Housewife")
                        .header("gender", "FEMALE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1]").doesNotExist());

        mockMvc.perform(get("/customers/reqHeader/pathVar/Housewife")
                        .header("gender", "MALE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    public void findByGenderAndNickNameProvidedInHeader() throws Exception {
        // headers = {"gender", "nickName"}
        /*mockMvc.perform(get("/customers/reqHeaders")
                        .header("gender", "FEMALE")
                        .header("nickName", "minnie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Minnie"))
                .andExpect(jsonPath("$[1]").doesNotExist());*/

        mockMvc.perform(get("/customers/reqHeaders")
                        .header("nickName", "Homie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());

    }
}
