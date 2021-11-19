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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author Tomasz Kaczmarzyk
 */
public class ComparableE2eTest extends E2eTestBase {

	@Controller
	public static class ComparableSpecsController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping(value = "/comparable/customers", params = { "nameAfter", "nameBefore"})
		@ResponseBody
		public Object findCustomersByNameRange(
				@And({
					@Spec(path="firstName", params="nameAfter", spec=GreaterThanOrEqual.class),
					@Spec(path="firstName", params="nameBefore", spec=LessThanOrEqual.class)
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/comparable/customers", params = { "registeredAfter", "registeredBefore"})
		@ResponseBody
		public Object findCustomersByRegistrationDateRange(
				@And({
					@Spec(path="registrationDate", params="registeredAfter", spec=GreaterThanOrEqual.class),
					@Spec(path="registrationDate", params="registeredBefore", spec=LessThanOrEqual.class)
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/comparable/customers2", params = { "registeredAfter", "registeredBefore"})
		@ResponseBody
		public Object findCustomersByRegistrationDateRange_customDateFormat(
				@And({
					@Spec(path="registrationDate", params="registeredAfter", spec=GreaterThanOrEqual.class, config = { "dd.MM.yyyy" }),
					@Spec(path="registrationDate", params="registeredBefore", spec=LessThanOrEqual.class, config = { "dd.MM.yyyy" })
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void findsByNameRange() throws Exception {
		mockMvc.perform(get("/comparable/customers")
				.param("nameAfter", "Homer")
				.param("nameBefore", "Mah")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Lisa"))
			.andExpect(jsonPath("$[2].firstName").value("Maggie"))
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void filtersByRegistrationDateRange_defaultDateFormat() throws Exception {
		mockMvc.perform(get("/comparable/customers")
				.param("registeredAfter", "2014-03-20")
				.param("registeredBefore", "2014-03-26")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Marge"))
			.andExpect(jsonPath("$[1].firstName").value("Bart"))
			.andExpect(jsonPath("$[2].firstName").value("Ned"))
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void filtersByRegistrationDateRange_customDateFormat() throws Exception {
		mockMvc.perform(get("/comparable/customers2")
				.param("registeredAfter", "20.03.2014")
				.param("registeredBefore", "26.03.2014")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Marge"))
			.andExpect(jsonPath("$[1].firstName").value("Bart"))
			.andExpect(jsonPath("$[2].firstName").value("Ned"))
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
}
