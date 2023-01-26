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
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.EqualDay;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
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
 * @author Robert Dworak (Tratif sp. z o.o.)
 */
public class CalendarE2eTest extends E2eTestBase {

    @Controller
    public static class CalendarSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "registeredEqualDay")
        @ResponseBody
        public Object findCustomersRegisteredDayEqualWithDefaultConfig(
                @Spec(path="registrationCalendar", params="registeredEqualDay", spec=EqualDay.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredEqualDayCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredDayEqualWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params="registeredEqualDayCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=EqualDay.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredEqual")
        @ResponseBody
        public Object findCustomersRegisteredEqualWithDefaultConfig(
                @Spec(path="registrationCalendar", params="registeredEqual", spec=Equal.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredEqualCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredEqualWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params="registeredEqualCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=Equal.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredBeforeDate")
        @ResponseBody
        public Object findCustomersRegisteredBefore(
                @Spec(path="registrationCalendar", params="registeredBeforeDate", spec=LessThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredBeforeDateCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredBeforeWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params="registeredBeforeDateCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=LessThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredAfterDate")
        @ResponseBody
        public Object findCustomersRegisteredAfter(
                @Spec(path="registrationCalendar", params="registeredAfterDate", spec=GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredAfterDateCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredAfterWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params="registeredAfterDateCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = {"registeredBeforeDate", "registeredAfterDate"})
        @ResponseBody
        public Object findCustomersRegisteredBetween(
                @Spec(path="registrationCalendar", params={"registeredAfterDate", "registeredBeforeDate"}, spec=Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = {"registeredBeforeDateCustomConfig", "registeredAfterDateCustomConfig"})
        @ResponseBody
        public Object findCustomersRegisteredBetweenWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params={"registeredAfterDateCustomConfig", "registeredBeforeDateCustomConfig"}, config="yyyy-MM-dd\'T\'HH:mm:ss", spec=Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredBeforeOrEqualDate")
        @ResponseBody
        public Object findCustomersRegisteredBeforeOrEqual(
                @Spec(path="registrationCalendar", params="registeredBeforeOrEqualDate", spec=LessThanOrEqual.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredBeforeOrEqualDateCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredBeforeOrEqualWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params="registeredBeforeOrEqualDateCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=LessThanOrEqual.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredAfterOrEqualDate")
        @ResponseBody
        public Object findCustomersRegisteredGreaterOrEqual(
                @Spec(path="registrationCalendar", params="registeredAfterOrEqualDate", spec=GreaterThanOrEqual.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredAfterOrEqualDateCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredAfterOrEqualWithCustomConfigContainingTime(
                @Spec(path="registrationCalendar", params="registeredAfterOrEqualDateCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=GreaterThanOrEqual.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
    }

    @BeforeEach
    public void setupCustomersWithCalendarContainingTime() {
        customer("Barry", "Benson")
                .registrationDate(2014, 3, 16, 0, 0, 0, 0)
                .build(em);
        customer("Adam", "Flayman")
                .registrationDate(2014, 3, 14, 23, 59, 59, 999)
                .build(em);
        customer("Vanessa", "Bloom")
                .registrationDate(2014, 3, 15, 14, 29, 4, 0)
                .build(em);
    }

    @Test
    public void findsByEqualDayUsingDefaultConfig() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredEqualDay", "2014-03-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

    @Test
    public void findsByEqualDayUsingCustomConfigIgnoringTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredEqualDayCustomConfig", "2014-03-15T12:34:19")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

    @Test
    public void findsByEqualUsingDefaultConfig() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredEqual", "2014-03-16")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Barry"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByEqualWithCustomConfigContainingTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredEqualCustomConfig", "2014-03-15T14:29:04")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByBetween() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredAfterDate", "2014-03-15")
                        .param("registeredBeforeDate", "2014-03-21")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"))
                .andExpect(jsonPath("$[2].firstName").value("Moe"))
                .andExpect(jsonPath("$[3].firstName").value("Barry"))
                .andExpect(jsonPath("$[4].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByBetweenWithCustomConfigContainingTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredAfterDateCustomConfig", "2014-03-15T15:34:19")
                        .param("registeredBeforeDateCustomConfig", "2014-03-21T12:34:19")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Barry"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByGreaterThan() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredAfterDate", "2014-03-16")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Maggie"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Ned"))
                .andExpect(jsonPath("$[6]").doesNotExist());
    }

    @Test
    public void findsByGreaterThanWithCustomConfigContainingTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredAfterDateCustomConfig", "2014-03-15T14:29:04")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Maggie"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Ned"))
                .andExpect(jsonPath("$[6].firstName").value("Barry"))
                .andExpect(jsonPath("$[7]").doesNotExist());
    }

    @Test
    public void findsByGreaterThanOrEqual() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredAfterOrEqualDate", "2014-03-16")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Maggie"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Ned"))
                .andExpect(jsonPath("$[6].firstName").value("Barry"))
                .andExpect(jsonPath("$[7]").doesNotExist());
    }

    @Test
    public void findsByGreaterThanOrEqualWithCustomConfigContainingTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredAfterOrEqualDateCustomConfig", "2014-03-15T14:29:04")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Marge"))
                .andExpect(jsonPath("$[1].firstName").value("Bart"))
                .andExpect(jsonPath("$[2].firstName").value("Lisa"))
                .andExpect(jsonPath("$[3].firstName").value("Maggie"))
                .andExpect(jsonPath("$[4].firstName").value("Minnie"))
                .andExpect(jsonPath("$[5].firstName").value("Ned"))
                .andExpect(jsonPath("$[6].firstName").value("Barry"))
                .andExpect(jsonPath("$[7].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[8]").doesNotExist());
    }

    @Test
    public void findsByLessThan() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredBeforeDate", "2014-03-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Adam"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByLessThanWithCustomConfigContainingTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredBeforeDateCustomConfig", "2014-03-15T14:29:04")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2].firstName").value("Adam"))
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

    @Test
    public void findsByLessThanOrEqual() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredBeforeOrEqualDate", "2014-03-16")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2].firstName").value("Barry"))
                .andExpect(jsonPath("$[3].firstName").value("Adam"))
                .andExpect(jsonPath("$[4].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    public void findsByLessThanOrEqualWithCustomConfigContainingTime() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("registeredBeforeOrEqualDateCustomConfig", "2014-03-15T14:29:04")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2].firstName").value("Adam"))
                .andExpect(jsonPath("$[3].firstName").value("Vanessa"))
                .andExpect(jsonPath("$[4]").doesNotExist());
    }

}
