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
import net.kaczmarzyk.spring.data.jpa.domain.InIgnoreCase;
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


public class InIgnoreCaseE2eTest extends E2eTestBase {

    private static final String BASE_URL = "/in-ignore-case";

    @Controller
    @RequestMapping(BASE_URL)
    public static class InIgnoreCaseSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "firstNameInIgnoreCase")
        @ResponseBody
        public Object findCustomersByFirstNameIgnoringCase(
                @Spec(path = "firstName", params = "firstNameInIgnoreCase", spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "firstNameInIgnoreCase")
        @ResponseBody
        public Object findCustomersByFirstNameIgnoringCaseUsingParamSeparator(
                @Spec(path = "firstName", params = "firstNameInIgnoreCase", paramSeparator = ',', spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-with-missing-parameter-name")
        @ResponseBody
        public Object findCustomersByFirstNameIgnoringCaseUsingWebParameterNameTheSameAsPath(
                @Spec(path = "firstName", spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator-missing-parameter-name")
        @ResponseBody
        public Object findCustomersByFirstNameIgnoringCaseUsingWebParameterNameTheSameAsPathAndParamSeparator(
                @Spec(path = "firstName", paramSeparator = ',', spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "idInIgnoreCase")
        @ResponseBody
        public Object findCustomersByIdIgnoringCase(
                @Spec(path = "id", params = "idInIgnoreCase", spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "idInIgnoreCase")
        @ResponseBody
        public Object findCustomersByIdIgnoringCaseUsingParamSeparator(
                @Spec(path = "id", params = "idInIgnoreCase", paramSeparator = ';', spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registrationDateInIgnoreCase")
        @ResponseBody
        public Object findCustomersByRegistrationDateIgnoringCase(
                @Spec(path = "registrationDate", params = "registrationDateInIgnoreCase", spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "registrationDateInIgnoreCase")
        @ResponseBody
        public Object findCustomersByRegistrationDateIgnoringCaseUsingParamSeparator(
                @Spec(path = "registrationDate", params = "registrationDateInIgnoreCase", paramSeparator = '.', spec = InIgnoreCase.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "genderInIgnoreCase")
        @ResponseBody
        public Object findCustomersByGenderIgnoringCase(
                @Spec(path = "gender", params = "genderInIgnoreCase", spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers-param-separator", params = "genderInIgnoreCase")
        @ResponseBody
        public Object findCustomersByGenderIgnoringCaseUsingParamSeparator(
                @Spec(path = "gender", params = "genderInIgnoreCase", paramSeparator = '|', spec = InIgnoreCase.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("firstNameInIgnoreCase", "homer", "marge")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator?firstNameInIgnoreCase=homer&firstNameInIgnoreCase=moe,bart&firstNameInIgnoreCase=lisa,maggie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Maggie"))
                .andExpect(jsonPath("$[4].firstName").value("Moe"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingWebParameterNameTheSameAsPathName() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-with-missing-parameter-name")
                        .param("firstName", "homer", "marge")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingWebParameterNameTheSameAsPathNameWithoutUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-with-missing-parameter-name?firstName=homer&firstName=moe,bart&firstName=lisa,maggie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedStringValuesIgnoringCaseUsingParamSeparatorAndWebParameterNameTheSameAsPathName() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator-missing-parameter-name?firstName=homer&firstName=moe,bart&firstName=lisa,maggie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Maggie"))
                .andExpect(jsonPath("$[4].firstName").value("Moe"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedLongValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("idInIgnoreCase", homerSimpson.getId().toString(), moeSzyslak.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedLongValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator")
                        .param("idInIgnoreCase", homerSimpson.getId().toString() + ";" + moeSzyslak.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedDateValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("registrationDateInIgnoreCase", "2014-03-30", "2014-03-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Lisa"))
                .andExpect(jsonPath("$[1].firstName").value("Maggie"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedDateValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator?registrationDateInIgnoreCase=2014-03-30.2014-03-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Lisa"))
                .andExpect(jsonPath("$[1].firstName").value("Maggie"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedEnumValuesIgnoringCase() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("genderInIgnoreCase", "male", "female")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Bart"))
                .andExpect(jsonPath("$[3].firstName").value("Lisa"))
                .andExpect(jsonPath("$[4].firstName").value("Maggie"))
                .andExpect(jsonPath("$[5].firstName").value("Moe"))
                .andExpect(jsonPath("$[6].firstName").value("Minnie"))
                .andExpect(jsonPath("$[7].firstName").value("Ned"))
                .andExpect(jsonPath("$[8]").doesNotExist());
    }

    @Test
    public void findsByListOfAllowedEnumValuesIgnoringCaseUsingParamSeparator() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers-param-separator?genderInIgnoreCase=male|female")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Bart"))
                .andExpect(jsonPath("$[3].firstName").value("Lisa"))
                .andExpect(jsonPath("$[4].firstName").value("Maggie"))
                .andExpect(jsonPath("$[5].firstName").value("Moe"))
                .andExpect(jsonPath("$[6].firstName").value("Minnie"))
                .andExpect(jsonPath("$[7].firstName").value("Ned"))
                .andExpect(jsonPath("$[8]").doesNotExist());
    }

    @Test
    public void ignoresUnparseableIntsWhenFilteringOnIntProperty() throws Exception {
        mockMvc.perform(get(BASE_URL + "/customers")
                        .param("idInIgnoreCase", homerSimpson.getId().toString(), "abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

}
