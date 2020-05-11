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

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


public class AnnotatedSpecInterfaceE2eTest extends E2eTestBase {

	@Spec(path = "lastName", spec = Like.class)
	public static interface LastNameSpec extends Specification<Customer> {
	}
	
	@Or({
		@Spec(params = "name", path = "firstName", spec = Like.class),
		@Spec(params = "name", path = "lastName", spec = Like.class)
	})
	public static interface FullNameSpec extends Specification<Customer> {
	}
	
	@And({
		@Spec(path = "lastName", spec = Like.class),
		@Spec(path = "gender", spec = Equal.class)
	})
	public static interface NameGenderSpec extends Specification<Customer> {
	}

	@Spec(path = "gold", constVal = "true", spec = Equal.class)
	public static interface GoldenSpec extends Specification<Customer> {
	}

	@Or({
		@Spec(params = "name", path = "firstName", spec = Like.class),
		@Spec(params = "name", path = "lastName", spec = Like.class)
	})
	public static interface GoldenAndFullNameSpec extends GoldenSpec {
	}
	
	@Controller
	public static class CustomSpecTestsController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping(value = "/anno-iface/customersByLastName")
		@ResponseBody
		public List<Customer> getCustomersWithCustomSimpleSpec(LastNameSpec spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/anno-iface/customersByLastNameAndFirstName")
		@ResponseBody
		public List<Customer> getCustomersWithCustomSimpleSpecExtenedWithParamSimpleSpec(
				@Spec(path = "firstName", spec = Like.class) LastNameSpec spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/anno-iface/customers", params = "name")
		@ResponseBody
		public List<Customer> getCustomersWithCustomOrSpec(FullNameSpec spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/anno-iface/golden-customers", params = "name")
		@ResponseBody
		public List<Customer> getCustomersWithCustomSpecInheritanceTree(GoldenAndFullNameSpec spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/anno-iface/customers", params = {"name", "address.street"})
		@ResponseBody
		public List<Customer> getCustomersWithCustomOrSpecExtendedWithParamSimpleSpec(
				@Spec(path="address.street", spec = Like.class) FullNameSpec spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/anno-iface/customers", params = { "lastName", "gender" })
		@ResponseBody
		public List<Customer> getCustomersWithCustomAndSpec(NameGenderSpec spec) {
			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void filtersAccordingToAnnotatedSimpleSpecInterface() throws Exception {
		mockMvc.perform(get("/anno-iface/customersByLastName")
				.param("lastName", "im")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}
	
	@Test
	public void filtersAccordingToAnnotatedSimpleSpecInterfaceExtendedWithParamSpecificSpec() throws Exception {
		mockMvc.perform(get("/anno-iface/customersByLastNameAndFirstName")
				.param("lastName", "im")
				.param("firstName", "ar")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void filtersAccordingToParamSpecificSpecIfInterfaceSpecParamIsMissing() throws Exception {
		mockMvc.perform(get("/anno-iface/customersByLastNameAndFirstName")
				.param("firstName", "o")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void filtersAccordingInterfaceSpecIfParamSpecParameterIsMissing() throws Exception {
		mockMvc.perform(get("/anno-iface/customersByLastNameAndFirstName")
				.param("lastName", "lak")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void doesNotFilterAtAllIfBothIfaceAndParamParametersAreMissing() throws Exception {
		mockMvc.perform(get("/anno-iface/customersByLastNameAndFirstName")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[7]").doesNotExist());
	}
	
	@Test
	public void doesNoFilteringIfParametersAreMissing() throws Exception {
		mockMvc.perform(get("/anno-iface/customersByLastName")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[6]").exists());
	}
	
	@Test
	public void filtersAccordingToAnnotatedOrSpecInterface() throws Exception {
		mockMvc.perform(get("/anno-iface/customers")
				.param("name", "o")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[6]").doesNotExist());
	}
	
	@Test
	public void filtersAccordingToAnnotatedOrSpecInterfaceExtendedWithParamSpec() throws Exception {
		mockMvc.perform(get("/anno-iface/customers")
				.param("name", "o")
				.param("address.street", "green")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[5]").doesNotExist());
	}
	
	@Test
	public void filtersAccordingToAnnotatedAndSpecInterface() throws Exception {
		mockMvc.perform(get("/anno-iface/customers")
				.param("lastName", "im")
				.param("gender", "FEMALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void filtersByConjunctionOfAnnotatedInterfaceAndItsParentClass() throws Exception {
		mockMvc.perform(get("/anno-iface/golden-customers")
				.param("name", "er")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist()); // Homer is not marked as gold
	}
}
