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
package net.kaczmarzyk.e2e.converter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.kaczmarzyk.E2eTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.DateBetween;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


public class DateE2eTest extends E2eTestBase {

	@Controller
	public static class DateSpecsController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping(value = "/customers", params = "registeredBefore")
		@ResponseBody
		public Object findCustomersRegisteredBefore(
				@Spec(path="registrationDate", params="registeredBefore", config="dd-MM-yyyy", spec=LessThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "registeredAfter")
		@ResponseBody
		public Object findCustomersRegisteredAfter(
				@Spec(path="registrationDate", params="registeredAfter", spec=GreaterThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = {"registeredBefore", "registeredAfter"})
		@ResponseBody
		public Object findCustomersRegisteredBetween(
				@Spec(path="registrationDate", params={"registeredAfter", "registeredBefore"}, spec=DateBetween.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void findsByDateBeforeWithCustomDateFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("registeredBefore", "16-03-2014")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Moe"))
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void findsByDateBetween() throws Exception {
		mockMvc.perform(get("/customers")
				.param("registeredAfter", "2014-03-16")
				.param("registeredBefore", "2014-03-30")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Marge"))
			.andExpect(jsonPath("$[1].firstName").value("Bart"))
			.andExpect(jsonPath("$[2].firstName").value("Lisa"))
			.andExpect(jsonPath("$[3].firstName").value("Ned"))
			.andExpect(jsonPath("$[4]").doesNotExist());
	}
	
	@Test
	public void findsByDateAfter() throws Exception {
		mockMvc.perform(get("/customers")
				.param("registeredAfter", "2014-03-26")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Lisa"))
			.andExpect(jsonPath("$[1].firstName").value("Maggie"))
			.andExpect(jsonPath("$[2].firstName").value("Minnie"))
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
}
