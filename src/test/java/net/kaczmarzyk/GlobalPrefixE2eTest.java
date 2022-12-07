/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithGlobalPrefix;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.Gender.FEMALE;
import static net.kaczmarzyk.spring.data.jpa.Gender.MALE;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalPrefixE2eTest extends IntegrationTestBaseWithGlobalPrefix {

    @And({
            @Spec(path = "gender", pathVars = "gender", spec = Equal.class),
            @Spec(path = "lastName", pathVars = "lastName", spec = Equal.class)
    })
    private static interface SimpsonSpec extends Specification<Customer> {
    }

    @Controller
    public static class TestController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping("/customers/{lastName}")
        @ResponseBody
        public Object listSimpsons_globalPrefix(SimpsonSpec spec) {
            return customerRepo.findAll(spec);
        }

    }

    @Before
    public void initializeTestData() {
        customer("Moe", "Szyslak").gender(MALE).build(em);
        customer("Minnie", "Szyslak").gender(FEMALE).build(em);

        customer("Homer", "Simpson").gender(MALE).build(em);
        customer("Bart", "Simpson").gender(MALE).build(em);
        customer("Lisa", "Simpson").gender(FEMALE).build(em);
        customer("Marge", "Simpson").gender(FEMALE).build(em);
    }

    @Test
    public void specificationSearchResultsShouldBeReturnedFromCache_annotatedInterface() throws Exception {
        mockMvc.perform(get("/api/MALE/customers/Simpson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName", equalTo("Homer")))
                .andExpect(jsonPath("$[1].firstName", equalTo("Bart")))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

}
