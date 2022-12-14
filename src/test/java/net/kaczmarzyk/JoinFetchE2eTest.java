/**
 * Copyright 2014-2022 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.CustomerDto;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInspector;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static jakarta.persistence.criteria.JoinType.INNER;
import static jakarta.persistence.criteria.JoinType.LEFT;
import static java.util.stream.Collectors.toList;
import static net.kaczmarzyk.utils.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JoinFetchE2eTest extends E2eTestBase {

	@RestController
	public static class TestController {

		@Autowired
		private CustomerRepository customerRepository;

		@RequestMapping("/join-fetch/customers")
		public Object findByFirstName(

				@JoinFetch(paths = "badges")
				@Spec(path = "firstName", spec = Equal.class) Specification<Customer> spec) {

			return customerRepository.findAll(spec);
		}

		@RequestMapping("/join-fetch-pageable/customers")
		public Object findByLastNameWithPagination(

				@JoinFetch(paths = "badges")
				@Spec(path = "lastName", spec = Equal.class) Specification<Customer> spec,
				Pageable pageable) {

			return customerRepository.findAll(spec, pageable);
		}

		@JoinFetch(paths = "orders", alias = "o")
		@Spec(path = "o.itemName", params = "orderIn", spec = In.class)
		private interface OrderInSpecification extends Specification<Customer> {
		}

		@RequestMapping(value = "/join-fetch-interface/customers", params = { "orderIn" })
		@ResponseBody
		public Object findByOrderIn(OrderInSpecification spec) {
			return customerRepository.findAll(spec, Sort.by("id"));
		}

		@RequestMapping(value = "/multi-join-fetch/customers", params = { "order", "badge" })
		@ResponseBody
		public Object findByOrdersAndBadges(

				@JoinFetch(paths = "orders", alias = "o")
				@JoinFetch(paths = "o.tags", alias = "t")
				@JoinFetch(paths = "badges", alias = "b")
				@Or({
						@Spec(path = "o.itemName", params = "order", spec = Like.class),
						@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
				}) Specification<Customer> spec) {

			return customerRepository.findAll(spec, Sort.by("id")).stream()
					.map(CustomerDto::from)
					.collect(toList());
		}

	}

	@Test
	public void findsByLastNameWithPagination() throws Exception {
		mockMvc.perform(get("/join-fetch-pageable/customers")
				.param("lastName", "Simpson")
				.param("page", "0")
				.param("size", "1")
				.param("sort", "id")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].firstName").value("Homer"))
			.andExpect(jsonPath("$.totalPages").value(5))
			.andExpect(jsonPath("$.totalElements").value(5))
			.andExpect(jsonPath("$.size").value(1));
	}

	@Test
	public void resolvesJoinProperlyFromAnnotatedCustomInterface() throws Exception {
		mockMvc.perform(get("/join-fetch-interface/customers")
				.param("orderIn", "Pizza")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void filtersByAttributesOfMultipleJoins() throws Exception {
		HibernateStatementInspector.clearInterceptedStatements();

		mockMvc.perform(get("/multi-join-fetch/customers")
				.param("order", "Pizza")
				.param("badge", "Troll Face")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Moe"))
			.andExpect(jsonPath("$[2]").doesNotExist());

		assertThatInterceptedStatements()
				.hasSelects(1)
				.hasNumberOfJoins(4)
				.hasNumberOfTableJoins("badges", LEFT, 1)
				.hasNumberOfTableJoins("orders", LEFT, 1)
				.hasNumberOfTableJoins("orders_tags", LEFT, 1)
				.hasNumberOfTableJoins("item_tags", INNER, 1);
	}

}
