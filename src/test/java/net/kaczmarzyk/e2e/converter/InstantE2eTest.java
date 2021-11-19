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

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InstantE2eTest extends E2eTestBase {
	
	@Controller
	@RequestMapping("/customers")
	public static class InstantSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantBefore")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantBefore", spec = LessThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantBefore_customFormat")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantBefore_customFormat", config = "yyyy-MM-dd\'T\' HH:mm:ss.SSS_XXX", spec = LessThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantAfter")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferAfter_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantAfter", spec = GreaterThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantAfter_customFormat")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferAfter_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantAfter_customFormat", config = "yyyy-MM-dd\'T\' HH:mm(ss.SSS XXX)", spec = GreaterThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = {"dateOfNextSpecialOfferInstantAfter", "dateOfNextSpecialOfferInstantBefore"})
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBetween_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = {"dateOfNextSpecialOfferInstantAfter", "dateOfNextSpecialOfferInstantBefore"}, spec = Between.class) Specification<Customer> spec) {
 			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = {"dateOfNextSpecialOfferInstantBefore_customFormat", "dateOfNextSpecialOfferInstantAfter_customFormat"})
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBetween_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = {"dateOfNextSpecialOfferInstantBefore_customFormat", "dateOfNextSpecialOfferInstantAfter_customFormat"}, config = "yyyy-MM-dd\'T\'HH:mm:ss.SSS (XXX)", spec = Between.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
	}
	
	@Test
	public void findsByInstantBeforeWithDefaultInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantBefore", "2020-07-16T16:17:00.000+04:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void findsByInstantBeforeWithCustomInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantBefore_customFormat", "2020-07-16T 16:17:00.000_+03:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void findsByInstantAfterWithDefaultInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter", "2020-07-16T16:17:00.000+00:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void findsByInstantAfterWithCustomInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter_customFormat", "2020-07-17T 16:17(00.000 +04:00)"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void findsByInstantBetweenWithDefaultInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter", "2020-07-16T16:17:00.000+00:00")
				.param("dateOfNextSpecialOfferInstantBefore", "2020-07-19T16:17:00.000+04:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}
	
	@Test
	public void findsByInstantBetweenWithCustomInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter", "2020-07-16T16:17:00.000+00:00")
				.param("dateOfNextSpecialOfferInstantBefore", "2020-07-19T16:17:00.000+04:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}
}
