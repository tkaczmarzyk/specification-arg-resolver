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
package net.kaczmarzyk.e2e.annotated;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static javax.persistence.criteria.JoinType.LEFT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test cases:
 * TC-1. interface with @JoinFetch spec
 */
public class AnnotatedSpecInterfaceJoinFetchE2eTest extends E2eTestBase {

	// TC-1. interface with @JoinFetch spec
	@JoinFetch(paths = "badges", joinType = LEFT)
	@Spec(path = "firstName", params = "firstName", spec = Equal.class)
	private interface FirstNameFilter extends Specification<Customer> {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @JoinFetch spec
		@RequestMapping(value = "/anno-iface-join-fetch/customersByFirstName")
		@ResponseBody
		public List<Customer> getCustomersWithCustomJoinFilter(FirstNameFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @JoinFetch spec
	public void filtersAccordingToAnnotatedSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-join-fetch/customersByFirstName")
				.param("firstName", "Homer")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

}
