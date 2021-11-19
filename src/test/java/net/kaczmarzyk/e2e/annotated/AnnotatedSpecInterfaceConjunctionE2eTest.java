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
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test cases:
 * TC-1. interface with @Conjunction spec
 * TC-2. interface without any spec extending param spec
 * TC-3. interface without any spec extending interface with @Conjunction spec
 * TC-4. interface with @Conjunction spec extending interface with @Conjunction spec
 */
public class AnnotatedSpecInterfaceConjunctionE2eTest extends E2eTestBase {

	// TC-1. interface with @Conjunction spec
	@Conjunction(value = @Or({
			@Spec(params = "lastName", path = "lastName", spec = Equal.class),
			@Spec(params = "firstName", path = "firstName", spec = Equal.class)
	}), and = {
			@Spec(params = "gender", path = "gender", spec = Equal.class)
	})
	private static interface LastNameOrFirstNameAndGenderFilter extends Specification<Customer> {
	}

	// TC-2. interface without any spec extending param spec
	private static interface EmptyFilter extends Specification<Customer> {
	}

	// TC-3. interface without any spec extending interface with @Conjunction spec
	private static interface EmptyFilterExtendingInterfaceWithConjunctionSpec extends LastNameOrFirstNameAndGenderFilter {
	}

	// TC-4. interface with @Conjunction spec extending interface with @Conjunction spec
	@Conjunction(value = @Or({
			@Spec(params = "firstName2", path = "firstName", spec = Like.class),
			@Spec(params = "firstName3", path = "firstName", spec = Like.class)
	}), and = @Spec(params = "firstName4", path = "firstName", spec = Like.class))
	private static interface ConjunctionFilterExtendedByOtherConjunctionFilter extends LastNameOrFirstNameAndGenderFilter {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Conjunction spec
		@RequestMapping(value = "/anno-iface-conjunction/customersByLastNameOrFirstNameAndGenderFilter")
		@ResponseBody
		public List<Customer> getCustomersByLastNameAndGenderOrFirstNameFilter(LastNameOrFirstNameAndGenderFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface without any spec extending param spec
		@RequestMapping(value = "/anno-iface-conjunction/customersByEmptyFilterExtendedByConjunctionSpecParam")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendedByConjunctionSpecParam(
				@Conjunction(value = @Or({
						@Spec(params = "lastName", path = "lastName", spec = Equal.class),
						@Spec(params = "firstName", path = "firstName", spec = Equal.class)
				}), and = {
						@Spec(params = "gender", path = "gender", spec = Equal.class)
				}) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extending interface with @Conjunction spec
		@RequestMapping(value = "/anno-iface-conjunction/customersByEmptyFilterExtendingInterfaceWithConjunctionSpec")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendedByConjunctionSpecParam(
				EmptyFilterExtendingInterfaceWithConjunctionSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface with @Conjunction spec extending interface with @Conjunction spec
		@RequestMapping(value = "/anno-iface-conjunction/customersByConjunctionFilterExtendedByOtherConjunctionFilter")
		@ResponseBody
		public List<Customer> getCustomersByConjunctionFilterExtendedByOtherConjunctionFilter(
				ConjunctionFilterExtendedByOtherConjunctionFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @Conjunction spec
	public void filtersAccordingToInterfaceWithOrSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-conjunction/customersByLastNameOrFirstNameAndGenderFilter")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test // TC-2. interface without any spec extending param spec
	public void filtersAccordingToEmptyFilterExtendedByConjunctionSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-conjunction/customersByEmptyFilterExtendedByConjunctionSpecParam")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extending interface with @Conjunction spec
	public void filtersAccordingToEmptyFilterExtendingInterfaceWithDisjunctionSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-conjunction/customersByEmptyFilterExtendingInterfaceWithConjunctionSpec")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test // TC-4. interface with @Conjunction spec extending interface with @Conjunction spec
	public void filtersAccordingToDisjunctionFilterExtendedByOtherDisjunctionFilter() throws Exception {
		mockMvc.perform(get("/anno-iface-conjunction/customersByConjunctionFilterExtendedByOtherConjunctionFilter")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.param("firstName2", "Homer")
				.param("firstName3", "Bart")
				.param("firstName4", "r")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

}
