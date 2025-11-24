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
import net.kaczmarzyk.spring.data.jpa.domain.NotInIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class NotInIgnoreCaseE2eTest extends E2eTestBase {

    private static final String BASE_URL = "/not-in-ignore-case";

    @Controller
    @RequestMapping(BASE_URL)
    public static class NotInIgnoreCaseSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "firstNameNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByFirstNameIgnoringCase(
                @Spec(path = "firstName", params = "firstNameNotInIgnoreCase", spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "firstNameNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByFirstNameIgnoringCaseUsingParamSeparator(
                @Spec(path = "firstName", params = "firstNameNotInIgnoreCase", paramSeparator = ',', spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-with-missing-parameter-name")
        @ResponseBody
        public Object findCustomersByLastNameIgnoringCaseUsingWebParameterNameTheSameAsPath(
                @Spec(path = "lastName", spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator-missing-parameter-name")
        @ResponseBody
        public Object findCustomersByLastNameIgnoringCaseUsingWebParameterNameTheSameAsPathAndParamSeparator(
                @Spec(path = "lastName", paramSeparator = ',', spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "idNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByIdIgnoringCase(
                @Spec(path = "id", params = "idNotInIgnoreCase", spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "idNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByIdIgnoringCaseUsingParamSeparator(
                @Spec(path = "id", params = "idNotInIgnoreCase", paramSeparator = ';', spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registrationDateNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByRegistrationDateIgnoringCase(
                @Spec(path = "registrationDate", params = "registrationDateNotInIgnoreCase", spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "registrationDateNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByRegistrationDateIgnoringCaseUsingParamSeparator(
                @Spec(path = "registrationDate", params = "registrationDateNotInIgnoreCase", paramSeparator = '.', spec = NotInIgnoreCase.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "genderNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByGenderIgnoringCase(
                @Spec(path = "gender", params = "genderNotInIgnoreCase", spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "genderNotInIgnoreCase")
        @ResponseBody
        public Object findCustomersByGenderIgnoringCaseUsingParamSeparator(
                @Spec(path = "gender", params = "genderNotInIgnoreCase", paramSeparator = '|', spec = NotInIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("firstNameNotInIgnoreCase", "homer", "marge")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Bart"))
                .andExpect(jsonPath("$[1].firstName").value("Lisa"))
                .andExpect(jsonPath("$[2].firstName").value("Maggie"))
                .andExpect(jsonPath("$[3].firstName").value("Moe"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Ned"))
                .andExpect(jsonPath("$[6].firstName").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator?firstNameNotInIgnoreCase=homer&firstNameNotInIgnoreCase=moe,bart&firstNameNotInIgnoreCase=lisa,maggie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Minnie"))
                .andExpect(jsonPath("$[2].firstName").value("Ned"))
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingWebParameterNameTheSameAsPathName() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-with-missing-parameter-name")
                        .param("lastName", "simpson", "szyslak")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Ned"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingWebParameterNameTheSameAsPathNameWithoutUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-with-missing-parameter-name?lastName=simpson&lastName=szyslak,flanders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Moe"))
                .andExpect(jsonPath("$[1].firstName").value("Minnie"))
                .andExpect(jsonPath("$[2].firstName").value("Ned"))
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingParamSeparatorAndWebParameterNameTheSameAsPathName() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator-missing-parameter-name?lastName=simpson&lastName=szyslak,flanders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedLongValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("idNotInIgnoreCase", moeSzyslak.getId().toString(), minnieSzyslak.getId().toString(), nedFlanders.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Bart"))
                .andExpect(jsonPath("$[3].firstName").value("Lisa"))
                .andExpect(jsonPath("$[4].firstName").value("Maggie"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedLongValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator")
                        .param("idNotInIgnoreCase", moeSzyslak.getId().toString() + ";" + minnieSzyslak.getId().toString() + ";" + nedFlanders.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Bart"))
                .andExpect(jsonPath("$[3].firstName").value("Lisa"))
                .andExpect(jsonPath("$[4].firstName").value("Maggie"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedDateValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("registrationDateNotInIgnoreCase", "2014-03-20", "2014-03-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Bart"))
                .andExpect(jsonPath("$[1].firstName").value("Lisa"))
                .andExpect(jsonPath("$[2].firstName").value("Maggie"))
                .andExpect(jsonPath("$[3].firstName").value("Minnie"))
                .andExpect(jsonPath("$[4].firstName").value("Ned"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedDateValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator?registrationDateNotInIgnoreCase=2014-03-20.2014-03-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Bart"))
                .andExpect(jsonPath("$[1].firstName").value("Lisa"))
                .andExpect(jsonPath("$[2].firstName").value("Maggie"))
                .andExpect(jsonPath("$[3].firstName").value("Minnie"))
                .andExpect(jsonPath("$[4].firstName").value("Ned"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedEnumValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("genderNotInIgnoreCase", "female")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Moe"))
                .andExpect(jsonPath("$[3].firstName").value("Ned"))
                .andExpect(jsonPath("$[4]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedEnumValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator?genderNotInIgnoreCase=male|female")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").doesNotExist());
    }

    @Test
    public void ignoresUnparseableIntsWhenFilteringOnIntProperty() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("idNotInIgnoreCase", "abc", margeSimpson.getId().toString(), bartSimpson.getId().toString(), lisaSimpson.getId().toString(), maggieSimpson.getId().toString(), moeSzyslak.getId().toString(), minnieSzyslak.getId().toString(), nedFlanders.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

}
