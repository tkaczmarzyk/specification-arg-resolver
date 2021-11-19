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
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.ItemTag;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.NotEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.JoinType;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Radlica
 */
public class MultiLevelJoinE2eTest extends IntegrationTestBase {

	@RestController
	public static class MultiLevelJoinController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/findCustomersByOrderedItemTag")
		@PostMapping
		public Object findCustomersByOrderedItemTag(
				@Join(path = "orders", alias = "o")
				@Join(path = "o.tags", alias = "t")
				@Spec(path = "t.name", params = "tag", spec = Equal.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id"));
		}

		@RequestMapping(value = "/findCustomersWithOrderedItemTaggedDifferentlyThan")
		@PostMapping
		public Object findCustomersWithOrderedItemTaggedWithDifferentTagThan(
				@Join(path = "orders", alias = "o", type = JoinType.INNER)
				@Join(path = "o.tags", alias = "t", type = JoinType.INNER)
				@Spec(path = "t.name", params = "tag", spec = NotEqual.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id"));
		}

		@RequestMapping(value = "/findCustomersWithOrderedItemTaggedDifferentlyThan_withPagination")
		@PostMapping
		public Object findCustomersWithOrderedItemTaggedWithDifferentTagThan_withPagination(
				@Join(path = "orders", alias = "o", type = JoinType.INNER)
				@Join(path = "o.tags", alias = "t", type = JoinType.INNER)
				@Spec(path = "t.name", params = "tag", spec = NotEqual.class) Specification<Customer> spec,
				Pageable pageable) {
			return customerRepo.findAll(spec, pageable);
		}

	}

	@BeforeEach
	public void initializeTestData() {
		ItemTag homerApprovedTag = itemTag("#homerApproved").build(em);
		ItemTag snacksTag = itemTag("#snacks").build(em);
		ItemTag fruitsTag = itemTag("#fruits").build(em);

		customer("Homer", "Simpson")
				.orders(
						order("Duff Beer").withTags(homerApprovedTag, snacksTag),
						order("Donuts").withTags(homerApprovedTag, snacksTag)).build(em);

		customer("Marge", "Simpson")
				.orders(
						order("Apple").withTags(fruitsTag)).build(em);

		customer("Bart", "Simpson")
				.orders(
						order("Pizza").withTags(snacksTag)).build(em);

		customer("Lisa", "Simpson")
				.orders(
						order("Jazz music disc")).build(em);

		customer("Ned", "Szyslak").build(em);
	}

	@Test
	public void shouldFindCustomersUsingMultilevelJoin() throws Exception {
		mockMvc.perform(post("/findCustomersByOrderedItemTag")
				.param("tag", "#snacks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Bart"))
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void shouldFindCustomersWithOrderedItemTaggedDifferentlyThanSnacks() throws Exception {
		mockMvc.perform(post("/findCustomersWithOrderedItemTaggedDifferentlyThan")
				.param("tag", "#snacks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Marge"))
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void shouldFindCustomersWithOrderedItemTaggedDifferentlyThanSnacks_withPagination() throws Exception {
		mockMvc.perform(post("/findCustomersWithOrderedItemTaggedDifferentlyThan_withPagination")
				.param("tag", "#snacks")
				.param("page", "0")
				.param("size", "2")
				.param("sort", "id"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.pageable").exists())
			.andExpect(jsonPath("$.pageable.sort.sorted").value("true"))
			.andExpect(jsonPath("$.pageable.pageNumber").value("0"))
			.andExpect(jsonPath("$.pageable.pageSize").value("2"))
			.andExpect(jsonPath("$.content[0].firstName").value("Homer"))
			.andExpect(jsonPath("$.content[1].firstName").value("Marge"))
			.andExpect(jsonPath("$.content[2]").doesNotExist());
	}

}
