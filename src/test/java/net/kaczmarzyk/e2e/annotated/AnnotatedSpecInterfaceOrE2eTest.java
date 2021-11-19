package net.kaczmarzyk.e2e.annotated;

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
import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
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
 *  TC-1. interface with @Or spec
 *  TC-2. interface with @Or spec extending param spec
 *  TC-3. interface without any spec extending param spec
 *  TC-4. interface without any spec extending interface with @Or annotation
 *  TC-5. interface with @Or spec extending other interface with @Or spec
 */
public class AnnotatedSpecInterfaceOrE2eTest extends E2eTestBase {

	@Or({
			@Spec(params = "gender", path = "gender", spec = Equal.class),
			@Spec(params = "firstName", path = "firstName", spec = Like.class)
	})
	private static interface GenderOrFirstNameSpec extends Specification<Customer> {
	}

	// TC-3. interface without any spec extending param spec
	private static interface EmptyFilter extends Specification<Customer> {}

	// TC-4. interface without any spec extending interface with @Or annotation
	private static interface EmptyFilterExtendingInterfaceWithOrSpec extends GenderOrFirstNameSpec {
	}
	
	// TC-5. interface with @Or spec extending other interface with @Or spec
	private static interface LastNameOrNickNameAndGenderOrFirstNameSpec extends GenderOrFirstNameSpec {
		
	}
	
	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Or spec
		@RequestMapping(value = "/anno-iface-or/customersByGenderOrFirstName")
		@ResponseBody
		public List<Customer> getCustomersByGenderOrFirstName(GenderOrFirstNameSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface with @Or spec extending param spec
		@RequestMapping(value = "/anno-iface-or/customersByGenderOrFirstNameAndLastName")
		@ResponseBody
		public List<Customer> getCustomersByGenderOrFirstNameSpecExtendedBySpecParam(
				@Or({
						@Spec(params = "lastName", path = "lastName", spec = Equal.class)
				}) GenderOrFirstNameSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extending param spec
		@RequestMapping(value = "/anno-iface-or/customersByParamSpec")
		@ResponseBody
		public List<Customer> getCustomersByGenderOrFirstNameParamSpec(
				@Or({
						@Spec(params = "gender", path = "gender", spec = Equal.class),
						@Spec(params = "firstName", path = "firstName", spec = Like.class)
				}) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface without any spec extending interface with @Or annotation
		@RequestMapping(value = "/anno-iface-or/customersByEmptySpecInterfaceExtendedByInterfaceWithOrSpec")
		@ResponseBody
		public List<Customer> customersByEmptySpecInterfaceExtendedByInterfaceWithOrSpec(
				EmptyFilterExtendingInterfaceWithOrSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-5. interface with @Or spec extending other interface with @Or spec
		@RequestMapping(value = "/anno-iface-or/customersByLastNameOrNickNameAndGenderOrFirstName")
		@ResponseBody
		public List<Customer> customersByEmptySpecInterfaceExtendedByInterfaceWithOrSpec(
				LastNameOrNickNameAndGenderOrFirstNameSpec spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @Or spec
	public void filtersAccordingToInterfaceWithOrSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-or/customersByGenderOrFirstName")
				.param("firstName", "Homer")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}

	@Test // TC-2. interface with @Or spec extending param spec
	public void filtersAccordingToInterfaceWithOrSpecExtendedBySpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-or/customersByGenderOrFirstNameAndLastName")
				.param("firstName", "Homer")
				.param("gender", "FEMALE")
				.param("lastName", "Simpson")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extending param spec
	public void filtersAccordingToEmptyFilterExtendingSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-or/customersByParamSpec")
				.param("firstName", "Homer")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}

	@Test // TC-4. interface without any spec extended by param spec
	public void filtersAccordingToInterfaceWithoutAnySpecExtendingInterfaceWithOrSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-or/customersByEmptySpecInterfaceExtendedByInterfaceWithOrSpec")
				.param("firstName", "Homer")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[5]").doesNotExist());
	}

	@Test // TC-5. interface without any spec extended by interface with @Or annotation
	public void filtersAccordingToInterfaceWithOrSpecExtendingInterfaceWithOrSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-or/customersByEmptySpecInterfaceExtendedByInterfaceWithOrSpec")
				.param("firstName", "Ned")
				.param("gender", "FEMALE")
				.param("nickName", "Flanders")
				.param("lastName", "Simpson")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[5]").doesNotExist());
	}

}
