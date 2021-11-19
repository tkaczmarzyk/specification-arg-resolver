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
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
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
 * TC-1. interface with @Join spec
 * TC-2. interface with @Join spec extending param spec
 * TC-3. interface without any spec extending param @Join spec
 * TC-4. interface without any spec extending interface with @Join spec
 * TC-5. interface with @Join spec extending other interface with @Join spec
 */
public class AnnotatedSpecInterfaceJoinE2eTest extends E2eTestBase {

	// TC-1. interface with @Join spec
	@Join(path = "badges", alias = "b")
	@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class)
	private interface BadgeFilter extends Specification<Customer> {
	}

	// TC-3. interface without any spec extending param @Join spec
	private static interface EmptyFilter extends Specification<Customer> {
	}

	// TC-4. interface without any spec extending interface with @Join spec
	private static interface EmptyFilterExtendingInterfaceWithJoinSpec extends BadgeFilter {}

	// TC-5. interface with @Join spec extending other interface with @Join spec
	@Join(path = "orders", alias = "o")
	@Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
	private static interface ItemNameBadgeTypeFilter extends BadgeFilter {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByBadgeType")
		@ResponseBody
		public List<Customer> getCustomersWithCustomJoinFilter(BadgeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface with @Join spec extending param spec
		@RequestMapping(value = "/anno-iface-join/customersByBadgeTypeAndItemName")
		@ResponseBody
		public List<Customer> getCustomersWithCustomAndFilterExtendedByJoinFilter(
				@Join(path = "orders", alias = "o")
				@Spec(path = "o.itemName", params = "itemName", spec = Equal.class) BadgeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extending param @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByEmptyFilterExtendingParamSpec")
		@ResponseBody
		public List<Customer> getcustomersByEmptyFilterExtendingParamSpec(
				@Join(path = "badges", alias = "b")
				@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface without any spec extending interface with @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByEmptyFilterExtendingInterfaceWithJoinSpec")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendingInterfaceWithJoinSpec(EmptyFilterExtendingInterfaceWithJoinSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-5. interface with @Join spec extending other interface with @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByItemNameBadgeTypeFilter")
		@ResponseBody
		public List<Customer> getCustomersByItemNameBadgeTypeFilter(ItemNameBadgeTypeFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @Join spec
	public void filtersAccordingToAnnotatedJoinSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByBadgeType")
				.param("badgeType", "Beef Eater")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-2. interface with @Join spec extending param spec
	public void filtersAccordingToInterfaceWithJoinSpecExtendingSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByBadgeTypeAndItemName")
				.param("badgeType", "Tomacco Eater")
				.param("itemName", "Tomacco")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extending param @Join spec
	public void filtersAccordingToEmptyFilterExtendingSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByBadgeType")
				.param("badgeType", "Beef Eater")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-4. interface without any spec extending interface with @Join spec
	public void filtersAccordingToEmptyFilterExtendingInterfaceWithJoinSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByEmptyFilterExtendingInterfaceWithJoinSpec")
				.param("badgeType", "Beef Eater")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-5. interface with @Join spec extending other interface with @Join spec
	public void filtersAccordingToItemNameBadgeTypeFilter() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByItemNameBadgeTypeFilter")
				.param("badgeType", "Tomacco Eater")
				.param("itemName", "Tomacco")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}


}
