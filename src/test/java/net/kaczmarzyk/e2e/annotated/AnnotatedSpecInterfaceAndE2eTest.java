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
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
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
 *  TC-1. interface with @And spec
 *  TC-2. interface with @And spec extending param spec
 *  TC-3. interface without any spec extending param spec
 *  TC-4. interface without any spec extending interface with @And spec
 *  TC-5. interface with @And spec extending other interface with @And spec
 *  TC-6. interface with @And spec extending other interface with @And spec and extending param spec
 *  TC-7. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec
 *  TC-8. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec and extending param spec
 */
public class AnnotatedSpecInterfaceAndE2eTest extends E2eTestBase {

	// TC-1. interface with @And spec
	@And({
			@Spec(params = "lastName", path = "lastName", spec = Equal.class)
	})
	private static interface LastNameAndSpec extends Specification<Customer> {
	}

	// TC-3. interface without any spec extending by param spec
	private static interface InterfaceWithoutAnySpec extends Specification<Customer> {}

	// TC-4. interface without any spec extending interface with @And spec
	private static interface InterfaceWithoutAnySpecExtendedByLastNameAndSpec extends LastNameAndSpec {}

	// TC-5. interface with @And spec extending other interface with @And spec
	@And({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "firstName", path = "firstName", spec = Like.class)
	})
	private static interface GenderAndFirstNameLikeSpecAndLastNameSpec extends LastNameAndSpec {
	}

	@And({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "firstName", path = "firstName", spec = Like.class)
	})
	private static interface GenderAndFirstNameLikeSpec extends Specification<Customer> {
	}

	@And({
			@Spec(params = "firstNameLike2", path = "firstName", spec = Like.class)
	})
	private static interface FirstNameLike2Spec extends GenderAndFirstNameLikeSpec {
	}

	// TC-7. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec
	@And({
			@Spec(params = "weightIn", path = "weight", spec = In.class)
	})
	private static interface FilterWithAndSpecExtendedByTwoFilters extends FirstNameLike2Spec, LastNameAndSpec{}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @And spec
		@RequestMapping(value = "/anno-iface-and/customersByLastName")
		@ResponseBody
		public List<Customer> getCustomersByLastNameFilter(LastNameAndSpec firstNameAndSpec) {
			return customerRepo.findAll(firstNameAndSpec);
		}

		// TC-2. interface with @And spec extending param spec
		@RequestMapping(value = "/anno-iface-and/customersByLastNameAndGender")
		@ResponseBody
		public List<Customer> getCustomersByLastNameFilterExtendingSpecParam(
				@And({
						@Spec(params = "gender", path = "gender", spec = Equal.class)
				}) LastNameAndSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extending by param spec
		@RequestMapping(value = "/anno-iface-and/customersByGender")
		@ResponseBody
		public List<Customer> getCustomersByGenderAndFirstNameSpecFilter(
				@And({
						@Spec(params = "gender", path = "gender", spec = Equal.class)
				}) InterfaceWithoutAnySpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface without any spec extending interface with @And spec
		@RequestMapping(value = "/anno-iface-and/customersByLastName2")
		@ResponseBody
		public List<Customer> getCustomersByGenderAndFirstNameSpecFilterExtendingSpecParam(
				InterfaceWithoutAnySpecExtendedByLastNameAndSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-5. interface with @And spec extending other interface with @And spec
		@RequestMapping(value = "/anno-iface-and/customersByGenderAndFirstNameAndLastName")
		@ResponseBody
		public List<Customer> getCustomersByGenderAndFirstNameSpec(GenderAndFirstNameLikeSpecAndLastNameSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-6. interface with @And spec extending other interface with @And spec and extending param spec
		@RequestMapping(value = "/anno-iface-and/customersByGenderAndFirstNameLikeAndLastNameAndFirstNameLike2")
		@ResponseBody
		public List<Customer> getCustomersByGenderAndFirstNameSpecAndNickName(
				@And({
						@Spec(params = "firstNameLike3", path = "firstName", spec = LikeIgnoreCase.class)
				}) GenderAndFirstNameLikeSpecAndLastNameSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-7. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec
		@RequestMapping(value = "/anno-iface-and/customersByComplexFilter")
		@ResponseBody
		public List<Customer> getCustomersByComplexFilter(FilterWithAndSpecExtendedByTwoFilters spec) {
			return customerRepo.findAll(spec);
		}

		// TC-8. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec and extending param spec
		@RequestMapping(value = "/anno-iface-and/customersByComplexFilterAndSpecParam")
		@ResponseBody
		public List<Customer> getCustomersByComplexFilterAndSpecParam(
				@And({
						@Spec(params = "occupation", path = "occupation", spec = Like.class)
				}) FilterWithAndSpecExtendedByTwoFilters spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @And spec
	public void filtersAccordingToInterfaceWithAndSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByLastName")
				.param("lastName", "Simpson")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}

	@Test // TC-2. interface with @And spec extending param spec
	public void filtersAccordingToInterfaceWithAndSpecAndSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByLastNameAndGender")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extending by interface with @And spec
	public void filtersAccordingToInterfaceWithoutAnySpecAndBySpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByGender")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test // TC-4. interface without any spec extending interface with @And spec
	public void filtersAccordingToInterfaceWithoutAnySpecExtendingInterfaceWithAndSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByLastName2")
				.param("lastName", "Simpson")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}

	@Test // TC-5. interface with @And spec extending other interface with @And spec
	public void filtersAccordingToInterfaceWithAndSpecExtendingInterfaceWithAndSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByGenderAndFirstNameAndLastName")
				.param("firstName", "M")
				.param("lastName", "Simpson")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}


	@Test // TC-6. interface with @And spec extending other interface with @And spec and extending param spec
	public void filtersAccordingToInterfaceWithAndSpecExtendingInterfaceWithAndSpecAndExtendingParamSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByGenderAndFirstNameLikeAndLastNameAndFirstNameLike2")
				.param("firstName", "M")
				.param("firstNameLike3", "ar")
				.param("lastName", "Simpson")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-7. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec
	public void filtersAccordingToInterfaceWithAndSpecExtendingInterfaceWithAndSpecAndExtendingInterfaceWithAndSpecExtendingAnotherInterfaceWithAndSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByComplexFilter")
				.param("firstName", "a")
				.param("firstNameLike2", "e")
				.param("gender", "FEMALE")
				.param("lastName", "Simpson")
				.param("weightIn", "50")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}


	@Test // TC-8. interface with @And spec extending interface with @And spec and extending interface with @And spec extending another interface with @And spec and extending param spec
	public void filtersAccordingToInterfaceWithAndSpecExtendingInterfaceWithAndSpecAndExtendingInterfaceWithAndSpecExtendingAnotherInterfaceWithAndSpecAndExtendingParamSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-and/customersByComplexFilterAndSpecParam")
				.param("firstName", "a")
				.param("firstNameLike2", "g")
				.param("gender", "FEMALE")
				.param("lastName", "Simpson")
				.param("weightIn", "50")
				.param("occupation", "Housewife")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
