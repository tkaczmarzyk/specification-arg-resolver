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
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JoinFetchE2eTest extends E2eTestBase {

	@RestController
	public static class TestController {

		@Autowired
		private CustomerRepository customerRepository;

		@RequestMapping("/join-fetch/customers")
		public Object findByFirstName(

				@JoinFetch(paths = "badges")
				@Spec(path = "firstName", spec = Equal.class) Specification<Customer> spec) {

			return customerRepository.findAll(spec);
		}
		
		@RequestMapping("/join-fetch/customers/not-distinct")
		public Object findByFirstNameAndJoinFetchDistinctSetToFalse(
				
				@JoinFetch(paths = "badges", distinct = false)
				@Spec(path = "firstName", spec = Equal.class) Specification<Customer> spec) {
			
			return customerRepository.findAll(spec);
		}

		@RequestMapping("/join-fetch-pageable/customers")
		public Object findByLastNameWithPagination(

				@JoinFetch(paths = "badges")
				@Spec(path = "lastName", spec = Equal.class) Specification<Customer> spec,
				Pageable pageable) {

			return customerRepository.findAll(spec, pageable);
		}

	}

	@Test
	public void createsDistinctQueryByDefault() throws Exception {
		mockMvc.perform(get("/join-fetch/customers")
				.param("firstName", "Homer")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void createsNotDistinctQueryIfDistinctAttributeIsSetToFalse() throws Exception {
		mockMvc.perform(get("/join-fetch/customers/not-distinct")
				.param("firstName", "Homer")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Homer"))
				.andExpect(jsonPath("$[2].firstName").value("Homer"))
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test
	public void findsByLastNameWithPagination() throws Exception {
		mockMvc.perform(get("/join-fetch-pageable/customers")
				.param("lastName", "Simpson")
				.param("page", "0")
				.param("size", "1")
				.param("sort", "id")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].firstName").value("Homer"))
			.andExpect(jsonPath("$.totalPages").value(5))
			.andExpect(jsonPath("$.totalElements").value(5))
			.andExpect(jsonPath("$.size").value(1));
	}

}