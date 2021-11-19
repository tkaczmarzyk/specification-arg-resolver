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

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithConfiguredConversionService;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConverterWithConversionServiceE2eTest extends IntegrationTestBaseWithConfiguredConversionService {
	
	@Controller
	@RequestMapping("/customers")
	public static class CustomConverterSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(params = "address")
		@ResponseBody
		public Object findCustomersByAddressUsingCustomConverter(
				@Spec(path = "address", params = "address", spec = Equal.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
	}
	
	@BeforeEach
	public void initializeTestData() {
		customer("Homer", "Simpson")
				.street("Evergreen Terrace").build(em);
		
		customer("Marge", "Simpson")
				.street("Evergreen Terrace").build(em);
		
		customer("Moe", "Szyslak")
				.street("Unknown").build(em);
	}
	
	@Test
	public void findsByAddressUsing() throws Exception {
		mockMvc.perform(get("/customers")
				.param("address", "Evergreen Terrace"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
}
