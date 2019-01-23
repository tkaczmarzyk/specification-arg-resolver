/**
 * Copyright 2014-2019 the original author or authors.
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


public class InE2eTest extends E2eTestBase {

	@Controller
	public static class InSpecController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping(value = "/customers", params = "firstNameIn")
		@ResponseBody
		public Object findCustomersByFirstName(
				@Spec(path="firstName", params = "firstNameIn", spec=In.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "idIn")
		@ResponseBody
		public Object findCustomersById(
				@Spec(path="id", params ="idIn", spec=In.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "registrationDateIn")
		@ResponseBody
		public Object findCustomersByRegistrationDate(
				@Spec(path="registrationDate", params = "registrationDateIn", spec=In.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "genderIn")
		@ResponseBody
		public Object findCustomersByGender(
				@Spec(path="gender", params="genderIn", spec=In.class) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void findsByListOfAllowedStringValues() throws Exception {
		mockMvc.perform(get("/customers")
				.param("firstNameIn", "Homer", "Marge")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Marge"))
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByListOfAllowedLongValues() throws Exception {
		mockMvc.perform(get("/customers")
				.param("idIn", homerSimpson.getId().toString(), moeSzyslak.getId().toString())
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Moe"))
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByListOfAllowedDateValues() throws Exception {
		mockMvc.perform(get("/customers")
				.param("registrationDateIn", "2014-03-30", "2014-03-31")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Lisa"))
			.andExpect(jsonPath("$[1].firstName").value("Maggie"))
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByListOfAllowedEnumValues() throws Exception {
		mockMvc.perform(get("/customers")
				.param("genderIn", "MALE", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Marge"))
			.andExpect(jsonPath("$[2].firstName").value("Bart"))
			.andExpect(jsonPath("$[3].firstName").value("Lisa"))
			.andExpect(jsonPath("$[4].firstName").value("Maggie"))
			.andExpect(jsonPath("$[5].firstName").value("Moe"))
			.andExpect(jsonPath("$[6].firstName").value("Ned"))
			.andExpect(jsonPath("$[7]").doesNotExist());
	}
	
	@Test
	public void ignoresUnparseableIntsWhenFilteringOnIntProperty() throws Exception {
	    mockMvc.perform(get("/customers")
                .param("idIn", homerSimpson.getId().toString(), "abc")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].firstName").value("Homer"))
            .andExpect(jsonPath("$[1]").doesNotExist());
	}
}
