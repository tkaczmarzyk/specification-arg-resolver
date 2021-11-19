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
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
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
public class ConstValsE2eTest extends E2eTestBase {

	@Spec(path = "lastName", spec = Equal.class, constVal = "Simpson")
	public static interface SimpsonSpec extends Specification<Customer> {
	}
	
	@Controller
	public static class TestController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping("/simpsons")
		@ResponseBody
		public Object listSimpsons(SimpsonSpec spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "2x")
		@ResponseBody
		public Object listCustomersRegisteredOn20or25thMarch(
				@Spec(path = "registrationDate", spec = In.class, constVal = {"2014-03-25", "2014-03-20"}) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = { "lastName", "evergreen"})
		@ResponseBody
		public Object listCustomersLivingOnEvergreenTerraceByLastName(
				@And({
					@Spec(path = "lastName", spec = Like.class),
					@Spec(path = "address.street", spec = Equal.class, constVal = "Evergreen Terrace")
				}) Specification<Customer> spec) {
			
			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void filtersBySingleSpecWithSingleConstVal() throws Exception {
		mockMvc.perform(get("/simpsons")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithMultipleConstVals() throws Exception {
		mockMvc.perform(get("/customers")
				.param("2x", "")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void filtersByConjunctionSpecWithConstValues() throws Exception {
		mockMvc.perform(get("/customers")
				.param("evergreen", "true")
				.param("lastName", "la")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Ned"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
