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
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


public class EqualE2eTest extends E2eTestBase {

	@Controller
	public static class EqualSpecController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping(value = "/customers", params = "firstName")
		@ResponseBody
		public Object findCustomersByFirstName(
				@Spec(path="firstName", spec=Equal.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "id")
		@ResponseBody
		public Object findCustomersById(
				@Spec(path="id", spec=Equal.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "registrationDateEq")
		@ResponseBody
		public Object findCustomersByRegistrationDate(
				@Spec(path="registrationDate", params = "registrationDateEq", spec=Equal.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "gender")
		@ResponseBody
		public Object findCustomersByGender(
				@Spec(path="gender", spec=Equal.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void findsByExactStringValue() throws Exception {
		mockMvc.perform(get("/customers")
				.param("firstName", "Homer")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void findsByExactLongValue() throws Exception {
		mockMvc.perform(get("/customers")
				.param("id", homerSimpson.getId().toString())
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void findsByExactDateValue() throws Exception {
		mockMvc.perform(get("/customers")
				.param("registrationDateEq", "2014-03-31")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Maggie"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void findsByExactEnumValue() throws Exception {
		mockMvc.perform(get("/customers")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Marge"))
			.andExpect(jsonPath("$[1].firstName").value("Lisa"))
			.andExpect(jsonPath("$[2].firstName").value("Maggie"))
			.andExpect(jsonPath("$[3].firstName").value("Minnie"))
			.andExpect(jsonPath("$[4]").doesNotExist());
	}
}
