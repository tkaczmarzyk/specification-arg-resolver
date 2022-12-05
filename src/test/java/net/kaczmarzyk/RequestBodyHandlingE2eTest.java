/**
 * Copyright 2014-2022 the original author or authors.
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

import com.google.gson.JsonParseException;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.ParamType;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Andrei Shakarov
 */
public class RequestBodyHandlingE2eTest extends E2eTestBase {

    @RestController
    public static class TestController {

        @Autowired
        CustomerRepository customerRepo;

        @PostMapping(value = "/customers/search")
        public List<Customer> findByIdInBody(
            @Spec(path = "id", params = "customerId", paramType = ParamType.BODY, spec = Equal.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @PostMapping(value = "/customers/search/firstName")
        public List<Customer> findByLastNameInBody(
                @Spec(path = "firstName", params = "filters.firstName.nameValue", paramType = ParamType.BODY, spec = Equal.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @ExceptionHandler(JsonParseException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public void handleJsonParsingException() {
        }

        @PostMapping(value = "/customers/search/firstNames")
        public List<Customer> findByLastNamesInBodyByCompositeKey(
            @Spec(path = "firstName", params = "filters.firstNames", paramType = ParamType.BODY, spec = In.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @PostMapping(value = "/customers/search/composite", params = "gender")
        public List<Customer> findByIdInBodyAndGenderInRequestParam(
            @And({
                @Spec(path = "lastName", paramType = ParamType.BODY, spec = Equal.class),
                @Spec(path = "gender", spec = Equal.class)
            }) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

    }

    @Test
    public void findsByIdProvidedInRequestBody() throws Exception {
        mockMvc.perform(post("/customers/search")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(" { \"customerId\": \"" + homerSimpson.getId() + "\" }"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].firstName").value(homerSimpson.getFirstName()));
    }

    @Test
    public void returnsBadRequestWhenContentBodyContainsArrayJsonNode() throws Exception {
        mockMvc.perform(post("/customers/search/firstName")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"filters\": { \"firstName\": [{ \"nameValue\": \"" + homerSimpson.getFirstName() + "\" }, { \"nameValue2\": \"" + lisaSimpson.getFirstName() + "\" }]}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findsByArrayOfLastNamesProvidedInRequestBodyWithCompositeStructure() throws Exception {
        mockMvc.perform(post("/customers/search/firstNames")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(" { \"filters\": { \"firstNames\": [\"" + homerSimpson.getFirstName() + "\", \"" + lisaSimpson.getFirstName() + "\"] }}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
            .andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists());
    }

    @Test
    public void findsByLastNameProvidedInRequestBodyAndByRegularSpec() throws Exception {
        mockMvc.perform(post("/customers/search/composite?gender=FEMALE")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(" { \"lastName\": \"Simpson\" }"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
            .andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
            .andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
            .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(post("/customers/search/composite?gender=MALE")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(" { \"lastName\": \"Simpson\" }"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
            .andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
            .andExpect(jsonPath("$.length()").value(2));
    }
}
