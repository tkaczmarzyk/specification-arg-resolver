/**
 * Copyright 2014-2016 the original author or authors.
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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


public class ComparableE2eTest extends E2eTestBase {

	@Controller
	public static class ComparableSpecsController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping(value = "/customers", params = { "nameAfter", "nameBefore"})
		@ResponseBody
		public Object findCustomersByNameRange(
				@And({
					@Spec(path="firstName", params="nameAfter", spec=GreaterThanOrEqual.class),
					@Spec(path="firstName", params="nameBefore", spec=LessThanOrEqual.class)
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void findsByNameRange() throws Exception {
		mockMvc.perform(get("/customers")
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
}
