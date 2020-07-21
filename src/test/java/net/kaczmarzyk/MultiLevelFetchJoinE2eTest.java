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

import net.kaczmarzyk.spring.data.jpa.*;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.JoinType;

import static java.util.stream.Collectors.toList;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static net.kaczmarzyk.utils.interceptor.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MultiLevelFetchJoinE2eTest extends IntegrationTestBase {

	@RestController
	@RequestMapping("/multilevel-join-fetch")
	public static class MultiLevelJoinFetchController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/findAllUsingLeftFetchJoins")
		@PostMapping
		public Object findAllCustomersWithJoinFetch(
				@JoinFetch(paths = "orders", alias = "o")
				@JoinFetch(paths = "o.tags") Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id")).stream()
					.map(this::mapToCustomerDto)
					.collect(toList());
		}

		@RequestMapping(value = "/findAllWthUsingInnerFetchJoins")
		@PostMapping
		public Object findAllCustomersWithInnerFetchJoins(
				@JoinFetch(paths = "orders", alias = "o", joinType = JoinType.INNER)
				@JoinFetch(paths = "o.tags", joinType = JoinType.INNER) Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id")).stream()
					.map(this::mapToCustomerDto)
					.collect(toList());
		}

		@RequestMapping(value = "/findAll-without-fetch-joins")
		@PostMapping
		public Object findCustomersByOrderedItemTag(
				@Spec(params = "ignoredParam", path = "notExistingAttribute", spec = Equal.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec).stream()
					.map(this::mapToCustomerDto)
					.collect(toList());
		}

		private CustomerDto mapToCustomerDto(Customer customer) {
			String tagOfFirstOrderedItem = customer.getOrders().stream()
					.flatMap(order -> order.getTags().stream())
					.map(ItemTag::getName)
					.findFirst().orElse(null);

			return new CustomerDto(
					customer.getFirstName(),
					tagOfFirstOrderedItem
			);
		}

		class CustomerDto {
			private String firstName;
			private String tagOfFirstCustomerItem;

			public CustomerDto(String firstName, String tagOfFirstCustomerItem) {
				this.firstName = firstName;
				this.tagOfFirstCustomerItem = tagOfFirstCustomerItem;
			}

			public String getFirstName() {
				return firstName;
			}

			public String getTagOfFirstCustomerItem() {
				return tagOfFirstCustomerItem;
			}
		}
	}

	@Before
	public void initializeTestData() {
		doInNewTransaction(() -> {
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

			customer("Maggie", "Simpson").build(em);
		});
	}

	@Autowired
	ItemTagRepository itemTagRepository;

	@After
	public void cleanupDb() {
		doInNewTransaction(() -> {
			customerRepo.deleteAll();
			itemTagRepository.deleteAll();
		});
	}

	@Test
	public void shouldFindCustomersUsingLeftFetchJoins() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(post("/multilevel-join-fetch/findAllUsingLeftFetchJoins")
				.param("tag", "#snacks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[3].firstName").value("Lisa"))
				.andExpect(jsonPath("$[4].firstName").value("Maggie"))
				.andExpect(jsonPath("$[5]").doesNotExist());

		assertThatInterceptedStatements()
				.hasSelects(1);
	}

	@Test
	public void shouldFindCustomersUsingInnerFetchJoins() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(post("/multilevel-join-fetch/findAllWthUsingInnerFetchJoins")
				.param("tag", "#snacks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[3]").doesNotExist());

		assertThatInterceptedStatements()
				.hasSelects(1);
	}

	@Test
	public void shouldFindCustomersUsingMultilevelFetchJoin() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(post("/multilevel-join-fetch/findAll-without-fetch-joins")
				.param("tag", "#snacks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[3].firstName").value("Lisa"))
				.andExpect(jsonPath("$[4].firstName").value("Maggie"))
				.andExpect(jsonPath("$[5]").doesNotExist());

		assertThatInterceptedStatements()
				.hasSelects(10)
				.hasSelectsFromSingleTableWithWhereClause(9);
	}

}
