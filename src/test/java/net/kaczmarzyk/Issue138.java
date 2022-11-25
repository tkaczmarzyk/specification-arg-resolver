/**
 * Copyright 2014-2022 the original author or authors.
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

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Radlica
 * @author Tomasz Kaczmarzyk
 */
public class Issue138 extends E2eTestBase {

	@RestController
	public static class TestControllerIssue138 {

		@Autowired
		private CustomerRepository customerRepository;

		@RequestMapping(value = "/issue138/join-fetch-interface/customers", params = { "orderIn" })
		@ResponseBody
		public Object findByOrderIn(@JoinFetch(paths = "orders", alias = "o")
									@Spec(path = "o.itemName", params = "orderIn", spec = In.class) Specification<Customer> spec, Pageable pageable) {
			return customerRepository.findAll(spec, pageable);
		}

	}

	@Test
	public void resolveSpecBasedOnJoinFetchAliasForPagedRequest() throws Exception {
		mockMvc.perform(get("/issue138/join-fetch-interface/customers")
				.param("orderIn", "Pizza")
						.param("order", "Duff Beer")
						.param("page", "0")
						.param("size", "1")
						.param("sort", "id")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content[0].firstName").value("Homer"))
			.andExpect(jsonPath("$.content[1]").doesNotExist());
	}

}