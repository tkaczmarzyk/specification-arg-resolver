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
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Joins;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test cases:
 * TC-1. interface with @Joins spec
 * TC-2. interface with @Joins spec extending param spec
 * TC-3. interface without any spec extended by @Joins param spec
 * TC-4. interface without any spec extending interface with @Joins spec
 * TC-5. interface with @Joins spec extending other interface with @Joins spec
 */
public class AnnotatedSpecInterfaceJoinsE2eTest extends E2eTestBase {

	// TC-1. interface with @Joins spec
	@Joins({
			@Join(path = "orders", alias = "o", type = JoinType.LEFT),
			@Join(path = "badges", alias = "b", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "o.itemName", params = "order", spec = Like.class),
			@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
	})
	private static interface ItemNameOrBadgeTypeFilter extends Specification<Customer> {
	}

	// TC-3. interface without any spec extended by param spec
	private static interface EmptyFilter extends Specification<Customer> {
	}

	// TC-4. interface without any spec extending interface with @Joins spec
	@Joins({
			@Join(path = "orders", alias = "o", type = JoinType.LEFT),
			@Join(path = "badges", alias = "b", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "o.itemName", params = "order", spec = Like.class),
			@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
	})
	private static interface EmptyFilterExtendingInterfaceWithJoinsSpec extends ItemNameOrBadgeTypeFilter {

	}

	// TC-5. interface with @Joins spec extending other interface with @Joins spec
	@Joins({
			@Join(path = "badges", alias = "b", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
	})
	private static interface BadgeTypeFilter extends Specification<Customer> {
	}

	@Joins({
			@Join(path = "orders", alias = "o", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "o.itemName", params = "order", spec = Like.class)
	})
	private static interface CustomJoinsFilterExtendingOtherJoinsFilter extends BadgeTypeFilter {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Joins spec
		@RequestMapping(value = "/anno-iface-joins/customersByItemNameOrBadgeTypeFilter")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(ItemNameOrBadgeTypeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface with @Join spec extending param spec
		@RequestMapping(value = "/anno-iface-joins/customersByItemNameOrBadgeTypeFilterExtendingParamSpec")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeTypeAndLastName(
				@Spec(params = "lastName", path = "lastName", spec = Equal.class) ItemNameOrBadgeTypeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extended by param spec
		@RequestMapping(value = "/anno-iface-joins/customersByEmptyFilterExtendingParamSpec")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(
				@Joins({
						@Join(path = "orders", alias = "o", type = JoinType.LEFT),
						@Join(path = "badges", alias = "b", type = JoinType.LEFT)
				})
				@Or({
						@Spec(path = "o.itemName", params = "order", spec = Like.class),
						@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
				}) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface without any spec extending interface with @Joins spec
		@RequestMapping(value = "/anno-iface-joins/customersByEmptyFilterExtendingInterfaceWithJoinsSpec")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(EmptyFilterExtendingInterfaceWithJoinsSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-5. interface with @Joins spec extending other interface with @Joins spec
		@RequestMapping(value = "/anno-iface-joins/customersByCustomJoinsFilterExtendingOtherJoinsFilter")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(CustomJoinsFilterExtendingOtherJoinsFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface without any spec extended by param spec
	public void filtersAccordingToAnnotatedJoinsSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByItemNameOrBadgeTypeFilter")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test //TC-2. interface with @Joins spec extending param spec
	public void filtersAccordingToInterfaceWithJoinSpecExtendingSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByItemNameOrBadgeTypeFilterExtendingParamSpec")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.param("lastName", "Simpson")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extended by param spec
	public void filtersAccordingToEmptyFilterExtendingInterfaceWithJoinsSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByEmptyFilterExtendingParamSpec")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-4. interface without any spec extending interface with @Joins spec
	public void filtersAccordingToEmptyFilterExtendingSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByEmptyFilterExtendingInterfaceWithJoinsSpec")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-5. interface without any spec extended by interface with @Joins spec
	public void filtersAccordingToCustomJoinsFilterExtendingOtherJoinsFilter() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByCustomJoinsFilterExtendingOtherJoinsFilter")
				.param("badge", "Beef Eater")
				.param("order", "Pizza")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

}
