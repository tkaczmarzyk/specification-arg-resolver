/**
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
import net.kaczmarzyk.spring.data.jpa.domain.*;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Sebastian Nawrocki
 */
public class CharEscapingE2eTest extends E2eTestBase {

    @Controller
    public static class CharEscapingSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/char-escaping-like", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastName(
                @Spec(path = "lastName", spec = Like.class, config = "\\%_") Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-like-ignore-case", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameIgnoreCase(
                @Spec(path = "lastName", spec = LikeIgnoreCase.class, config = {"en_US", "\\%_"}) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-not-like", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameNotLike(
                @Spec(path = "lastName", spec = NotLike.class, config = "\\%_") Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-not-like-ignore-case", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameNotLikeIgnoreCase(
                @Spec(path = "lastName", spec = NotLikeIgnoreCase.class, config = {"en_US", "\\%_"}) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-starting-with", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameStartingWith(
                @Spec(path = "lastName", spec = StartingWith.class, config = "\\%_") Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-starting-with-ignore-case", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameStartingWithIgnoreCase(
                @Spec(path = "lastName", spec = StartingWithIgnoreCase.class, config = {"en_US", "\\%_"}) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-ending-with", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameEndingWith(
                @Spec(path = "lastName", spec = EndingWith.class, config = "\\%_") Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/char-escaping-ending-with-ignore-case", params = "lastName")
        @ResponseBody
        public Object findCustomersByLastNameEndingWithIgnoreCase(
                @Spec(path = "lastName", spec = EndingWithIgnoreCase.class, config = {"en_US", "\\%_"}) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @BeforeEach
    public void initData() {
        customer("any", "percent%").build(em);
        customer("any", "apercent").build(em); // would match percent% if % was wildcard

        customer("any", "underscore_").build(em);
        customer("any", "aunderscore").build(em); // would match underscore_ if _ was wildcard

        customer("any", "%beginning").build(em);
        customer("any", "xbeginning").build(em); // would match %beginning if % was wildcard

        customer("any", "_beginning").build(em);
        customer("any", "ybeginning").build(em); // would match _beginning if _ was wildcard

        customer("any", "mi%ddle").build(em);
        customer("any", "mi_ddle").build(em);
        customer("any", "mieddle").build(em); // would match mi_ddle if _ was wildcard

        customer("any", "combo%_").build(em);
        customer("any", "comboxx").build(em); // would match combo%_ if wildcards
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignAtEnd() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "percent%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("percent%"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignAtBeginning() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "%beginning")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("%beginning"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignInMiddle() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "mi%ddle")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("mi%ddle"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedUnderscoreAtEnd() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "underscore_")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("underscore_"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedUnderscoreAtBeginning() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "_beginning")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("_beginning"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedUnderscoreInMiddle() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "mi_ddle")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("mi_ddle"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedComboOfBothCharacters() throws Exception {
        mockMvc.perform(get("/char-escaping-like")
                        .param("lastName", "combo%_")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("combo%_"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignIgnoreCase() throws Exception {
        mockMvc.perform(get("/char-escaping-like-ignore-case")
                        .param("lastName", "PERCENT%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("percent%"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameNotLikeWithEscapedPercentSign() throws Exception {
        mockMvc.perform(get("/char-escaping-not-like")
                        .param("lastName", "percent%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].lastName").value(not(hasItem("percent%"))));
    }

    @Test
    public void findsByLastNameNotLikeIgnoreCaseWithEscapedPercentSign() throws Exception {
        mockMvc.perform(get("/char-escaping-not-like-ignore-case")
                        .param("lastName", "PERCENT%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].lastName").value(not(hasItem("percent%"))));
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignStartingWith() throws Exception {
        mockMvc.perform(get("/char-escaping-starting-with")
                        .param("lastName", "percent%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("percent%"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignStartingWithIgnoreCase() throws Exception {
        mockMvc.perform(get("/char-escaping-starting-with-ignore-case")
                        .param("lastName", "PERCENT%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("percent%"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignEndingWith() throws Exception {
        mockMvc.perform(get("/char-escaping-ending-with")
                        .param("lastName", "percent%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("percent%"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLastNameWithEscapedPercentSignEndingWithIgnoreCase() throws Exception {
        mockMvc.perform(get("/char-escaping-ending-with-ignore-case")
                        .param("lastName", "PERCENT%")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("percent%"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }
}

