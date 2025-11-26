/**
 * Copyright 2014-2025 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.domain.NotEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static java.util.stream.Collectors.toList;
import static net.kaczmarzyk.utils.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JoinFetchDistinctE2eTest extends E2eTestBase {

	@RestController
	public static class TestController {

		@Autowired
		private CustomerRepository customerRepository;

		@RequestMapping("/join-fetch-distinct/customers")
		public Object joinFetchCountSpecification(
				@JoinFetch(paths = "badges", alias = "b", joinType = LEFT)
				@And({
						@Spec(path = "b.badgeType", constVal = "CyberMondayBadge", spec = NotEqual.class),
						@Spec(path = "lastName", spec = Equal.class)
				}) Specification<Customer> spec) {

			return customerRepository.findAll(spec).stream()
					.map(CustomerDto::from)
					.collect(toList());
		}

		@RequestMapping("/join-fetch-distinct/customers/distinct-false")
		public Object joinFetchCountSpecification_distinctAttributeSetToFalse(
				@JoinFetch(paths = "badges", alias = "b", distinct = false, joinType = LEFT)
				@And({
						@Spec(path = "b.badgeType", constVal = "CyberMondayBadge", spec = NotEqual.class),
						@Spec(path = "lastName", spec = Equal.class)
				}) Specification<Customer> spec) {

			return customerRepository.findAll(spec).stream()
					.map(CustomerDto::from)
					.collect(toList());
		}

		@RequestMapping("/join-fetch-distinct/customers/paged")
		public Object joinFetchPagedQuery(
				@JoinFetch(paths = "badges", alias = "b", joinType = LEFT)
				@And({
						@Spec(path = "b.badgeType", constVal = "CyberMondayBadge", spec = NotEqual.class),
						@Spec(path = "lastName", spec = Equal.class)
				}) Specification<Customer> spec, Pageable pageable) {

			return customerRepository.findAll(spec, pageable).stream()
					.map(CustomerDto::from)
					.collect(toList());
		}

		@RequestMapping("/join-fetch-distinct/customers/paged/distinct-false")
		public Object joinFetchPagedQuery_distinctAttributeSetToFalse(
				@JoinFetch(paths = "badges", alias = "b", distinct = false, joinType = LEFT)
				@And({
						@Spec(path = "b.badgeType", constVal = "CyberMondayBadge", spec = NotEqual.class),
						@Spec(path = "lastName", spec = Equal.class)
				}) Specification<Customer> spec, Pageable pageable) {

			return customerRepository.findAll(spec, pageable);
		}

		@RequestMapping("/join-fetch-distinct/customers/count")
		public Object joinFetchCountQuery(
				@JoinFetch(paths = "badges", alias = "b", joinType = LEFT)
				@And({
						@Spec(path = "b.badgeType", constVal = "CyberMondayBadge", spec = NotEqual.class),
						@Spec(path = "lastName", spec = Equal.class)
				}) Specification<Customer> spec) {

			return customerRepository.count(spec);
		}

		@RequestMapping("/join-fetch-distinct/customers/count/distinct-false")
		public Object joinFetchCountQuery_distinctAttributeSetToFalse(
				@JoinFetch(paths = "badges", alias = "b", distinct = false, joinType = LEFT)
				@And({
						@Spec(path = "b.badgeType", constVal = "CyberMondayBadge", spec = NotEqual.class),
						@Spec(path = "lastName", spec = Equal.class)
				}) Specification<Customer> spec) {
			return customerRepository.count(spec);
		}
	}

	@Test
	public void createsDistinctQueryByDefault() throws Exception {
		mockMvc.perform(get("/join-fetch-distinct/customers")
						.param("lastName", "Simpson")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[3]").doesNotExist());


		assertThatInterceptedStatements()
				.hasOneClause("distinct");
	}

	@Test
	public void returnsTheDeduplicatedEntitiesDespiteTheFactThatDistinctAttributeIsSetToFalse() throws Exception {
		mockMvc.perform(get("/join-fetch-distinct/customers/distinct-false")
						.param("lastName", "Simpson")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[3]").doesNotExist());

		assertThatInterceptedStatements()
				.doesNotHaveClause("distinct");
	}

	@Test
	public void createsDistinctQueryByDefaultForPagedQuery() throws Exception {
		mockMvc.perform(get("/join-fetch-distinct/customers/paged")
						.param("lastName", "Simpson")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1].firstName").value("Marge"))
				.andExpect(jsonPath("$[2].firstName").value("Bart"))
				.andExpect(jsonPath("$[3]").doesNotExist());

		assertThatInterceptedStatements()
				.hasOneClause("distinct");
	}

	/**
	 * Warning: Hibernate makes all queries distinct.
	 * Also, it discourages from using join fetches in paginated queries: https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#hql-limit-offset
	 * This in combination with Spring Data causes inconsistencies (as some queries are executed with distinct true and other with distinct false as a result)
	 * @throws Exception
	 */
	@Test
	public void returnsTheDeduplicatedEntitiesDespiteTheFactThatQueryIsNotDistinctForPagedQueryWhenDistinctAttributeIsSetToFalse() throws Exception {
		mockMvc.perform(get("/join-fetch-distinct/customers/paged/distinct-false")
						.param("page", "0")
						.param("size", "2")
						.param("lastName", "Simpson")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalPages").value(3))
				.andExpect(jsonPath("$.totalElements").value(5))
				.andExpect(jsonPath("$.last").value(false))
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].firstName").value("Homer"))
				.andExpect(jsonPath("$.content[1].firstName").value("Marge"))
				.andExpect(jsonPath("$.content[2]").doesNotExist());

		assertThatInterceptedStatements()
				.doesNotHaveClause("distinct");
	}

	@Test
	public void createsDistinctQueryByDefaultForTheCountQuery() throws Exception {
		mockMvc.perform(get("/join-fetch-distinct/customers/count")
						.param("lastName", "Simpson")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(3));
		//Simpson 1x, Marge 1x, Bart 1x

		assertThatInterceptedStatements()
				.hasClause("distinct", 2)
				.hasOneClause("select distinct count(distinct c1_0.id) from customer c1_0 left join badges b1_0 on c1_0.id=b1_0.customer_id where b1_0.badge_type!=? and c1_0.last_name=?");
	}


	@Test
	public void doesNotCreatesDistinctQueryForCountQueryWhenDistinctAttributeIsSetToFalse() throws Exception {
		mockMvc.perform(get("/join-fetch-distinct/customers/count/distinct-false")
						.param("lastName", "Simpson")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(5));
		//Homer 3x, Marge 1x, Bart 1x

		assertThatInterceptedStatements()
				.doesNotHaveClause("distinct")
				.hasOneClause("select count(c1_0.id) from customer c1_0 left join badges b1_0 on c1_0.id=b1_0.customer_id where b1_0.badge_type!=? and c1_0.last_name=?");
	}

}
