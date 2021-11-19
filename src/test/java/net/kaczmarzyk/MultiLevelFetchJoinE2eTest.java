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
import net.kaczmarzyk.spring.data.jpa.ItemTagRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static net.kaczmarzyk.utils.interceptor.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Radlica
 */
public class MultiLevelFetchJoinE2eTest extends IntegrationTestBase {

	@RestController
	@RequestMapping("/multilevel-join-fetch")
	public static class MultiLevelJoinFetchController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/findAll")
		@PostMapping
		public Object findAllCustomers(
				@JoinFetch(paths = "orders", alias = "o")
				@JoinFetch(paths = "o.tags") Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id")).stream()
					.map(this::mapToCustomerDto)
					.collect(toList());
		}

		@RequestMapping(value = "/findAllWthUsingInnerFetchJoins")
		@PostMapping
		public Object findAllCustomersUsingInnerFetchJoins(
				@JoinFetch(paths = "orders", alias = "o", joinType = JoinType.INNER)
				@JoinFetch(paths = "o.tags", joinType = JoinType.INNER) Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id")).stream()
					.map(this::mapToCustomerDto)
					.collect(toList());
		}

		@RequestMapping(value = "/findAllWithoutFetchJoins")
		@PostMapping
		public Object findAllWithoutFetchJoins(
				@Spec(params = "ignoredParam", path = "notExistingAttribute", spec = Equal.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec).stream()
					.map(this::mapToCustomerDto)
					.collect(toList());
		}

		@RequestMapping(value = "/findAllCustomersFetchOrdersWithTagsAndNotes")
		@PostMapping
		public Object findAllCustomersFetchOrdersWithTagsAndNotes(
				@JoinFetch(paths = "orders", alias = "o")
				@JoinFetch(paths = "o.tags")
				@JoinFetch(paths = "o.note")
						Specification<Customer> spec) {
			return customerRepo.findAll(spec, Sort.by("id")).stream()
					.map(this::mapToCustomerWithOrdersWithTagsAndNotes)
					.collect(toList());
		}

		@RequestMapping(value = "/findAllCustomersFetchOrdersWithTagsAndNotes_withPagination")
		@PostMapping
		public Object findAllCustomersFetchOrdersWithTagsAndNotes_withPagination(
				@JoinFetch(paths = "orders", alias = "o")
				@JoinFetch(paths = "o.tags")
				@JoinFetch(paths = "o.note") Specification<Customer> spec,
				Pageable pageable) {
			return customerRepo.findAll(spec, pageable);
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

		private CustomerWithOrdersWithTagsAndNotes mapToCustomerWithOrdersWithTagsAndNotes(Customer customer) {
			return new CustomerWithOrdersWithTagsAndNotes(
					customer.getFirstName(),
					customer.getOrders().stream().map(order -> new OrdersWithTagsAndNotes(
							order.getItemName(),
							order.getTags().stream().map(ItemTag::getName).sorted(StringUtils::compare).collect(Collectors.joining(",")),
							order.getNote().getTitle()
					)).sorted((o1, o2) -> StringUtils.compare(o1.itemName, o2.itemName)).collect(Collectors.toList())
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

		class OrdersWithTagsAndNotes {
			private String itemName;
			private String tags;
			private String notes;

			public OrdersWithTagsAndNotes(String itemName, String tags, String notes) {
				this.itemName = itemName;
				this.tags = tags;
				this.notes = notes;
			}

			public String getItemName() {
				return itemName;
			}

			public String getTags() {
				return tags;
			}

			public String getNotes() {
				return notes;
			}
		}

		class CustomerWithOrdersWithTagsAndNotes {
			private String firstName;
			private List<OrdersWithTagsAndNotes> orders;

			public CustomerWithOrdersWithTagsAndNotes(String firstName, List<OrdersWithTagsAndNotes> orders) {
				this.firstName = firstName;
				this.orders = orders;
			}

			public String getFirstName() {
				return firstName;
			}

			public List<OrdersWithTagsAndNotes> getOrders() {
				return orders;
			}
		}
	}

	@BeforeEach
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

	@AfterEach
	public void cleanupDb() {
		doInNewTransaction(() -> {
			customerRepo.deleteAll();
			itemTagRepository.deleteAll();
		});
	}

	@Test
	public void shouldFindCustomersUsingLeftFetchJoins() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(post("/multilevel-join-fetch/findAll")
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
	public void shouldFindAllCustomersWithoutUsingJoinFetches() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(post("/multilevel-join-fetch/findAllWithoutFetchJoins")
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

	@Test
	public void shouldFindCustomersWithFetchedOrderWithTagsAndNotes() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(post("/multilevel-join-fetch/findAllCustomersFetchOrdersWithTagsAndNotes")
				.param("tag", "#snacks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[0].orders").isArray())

				.andExpect(jsonPath("$[0].orders[0].itemName").value("Donuts"))
				.andExpect(jsonPath("$[0].orders[0].tags").value("#homerApproved,#snacks"))
				.andExpect(jsonPath("$[0].orders[0].notes").value("NoteDonuts"))

				.andExpect(jsonPath("$[0].orders[1].itemName").value("Duff Beer"))
				.andExpect(jsonPath("$[0].orders[1].tags").value("#homerApproved,#snacks"))
				.andExpect(jsonPath("$[0].orders[1].notes").value("NoteDuff Beer"))

				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[1].orders").isArray())
				.andExpect(jsonPath("$[1].orders[0].itemName").value("Apple"))
				.andExpect(jsonPath("$[1].orders[0].tags").value("#fruits"))
				.andExpect(jsonPath("$[1].orders[0].notes").value("NoteApple"))

				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[2].orders").isArray())
				.andExpect(jsonPath("$[2].orders[0].itemName").value("Pizza"))
				.andExpect(jsonPath("$[2].orders[0].tags").value("#snacks"))
				.andExpect(jsonPath("$[2].orders[0].notes").value("NotePizza"))

				.andExpect(jsonPath("$[3].firstName").value("Lisa"))
				.andExpect(jsonPath("$[3].orders").isArray())
				.andExpect(jsonPath("$[3].orders[0].itemName").value("Jazz music disc"))
				.andExpect(jsonPath("$[3].orders[0].tags").value(""))
				.andExpect(jsonPath("$[3].orders[0].notes").value("NoteJazz music disc"))

				.andExpect(jsonPath("$[4].firstName").value("Maggie"))

				.andExpect(jsonPath("$[5]").doesNotExist());

		assertThatInterceptedStatements()
				.hasSelects(1);
	}

	@Test
	public void shouldFindCustomersWithFetchedOrderWithTagsAndNotes_withPagination() throws Exception {
		mockMvc.perform(post("/multilevel-join-fetch/findAllCustomersFetchOrdersWithTagsAndNotes_withPagination")
				.param("tag", "#snacks")
				.param("page", "0")
				.param("size", "1")
				.param("sort", "id"))
			.andExpect(status().isOk())

			.andExpect(jsonPath("$.pageable").exists())
			.andExpect(jsonPath("$.pageable.sort.sorted").value("true"))
			.andExpect(jsonPath("$.pageable.pageNumber").value("0"))
			.andExpect(jsonPath("$.pageable.pageSize").value("1"))

			.andExpect(jsonPath("$.content[0].firstName").value("Homer"))
			.andExpect(jsonPath("$.content[0].orders").isArray())

			.andExpect(jsonPath("$.content[1]").doesNotExist());
	}


}
