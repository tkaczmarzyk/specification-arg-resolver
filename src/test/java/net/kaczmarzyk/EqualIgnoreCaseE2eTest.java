/**
 * Copyright 2014-2020 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.domain.EqualIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.Gender.FEMALE;
import static net.kaczmarzyk.spring.data.jpa.Gender.MALE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Radlica
 */
public class EqualIgnoreCaseE2eTest extends IntegrationTestBase {

	@Controller
	@RequestMapping("/equal-ignore-case")
	public static class EqualIgnoreCaseSpecController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/customers", params = "firstName")
		@ResponseBody
		public Object findCustomersByFirstNameIgnoringCase(
				@Spec(path = "firstName", spec = EqualIgnoreCase.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "gender")
		@ResponseBody
		public Object findCustomersByGenderIgnoringCase(
				@Spec(path = "gender", spec = EqualIgnoreCase.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}

	@Autowired
	WebApplicationContext wac;

	protected MockMvc mockMvc;

	@BeforeEach
	public void initializeTestData() {
		customer("Homer", "Simpson").gender(MALE).build(em);
		customer("Marge", "Simpson").gender(FEMALE).build(em);
		customer("Bart", "Simpson").gender(MALE).build(em);
		customer("Lisa", "Simpson").gender(FEMALE).build(em);
		customer("Maggie", "Simpson").gender(FEMALE).build(em);

		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void findsByStringValueIgnoringCase() throws Exception {
		mockMvc.perform(get("/equal-ignore-case/customers")
				.param("firstName", "hOmEr")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByEnumValueIgnoringCase() throws Exception {
		mockMvc.perform(get("/equal-ignore-case/customers")
				.param("gender", "fEmAlE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Marge"))
				.andExpect(jsonPath("$[1].firstName").value("Lisa"))
				.andExpect(jsonPath("$[2].firstName").value("Maggie"))
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

}
