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
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.CustomerRepositoryWithCache;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithConfiguredCache;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CacheSupportE2eTest extends IntegrationTestBaseWithConfiguredCache {

	@Spec(path = "lastName", params = "lastName", spec = Equal.class)
	@JoinFetch(paths = {"orders", "badges"})
	private static interface SimpsonSpec extends Specification<Customer> {
	}

	@Controller
	@RequestMapping("/cache-test")
	public static class TestController {

		@Autowired
		CustomerRepositoryWithCache customerRepoWithCacheSupport;

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping("/interface/simpsons")
		@ResponseBody
		public Object listSimpsonsUsingRepositoryWithCacheSupport_annotatedInterface(SimpsonSpec spec) {
			return customerRepoWithCacheSupport.findAll(spec);
		}

		@RequestMapping("/interface/non-cache/simpsons")
		@ResponseBody
		public Object listSimpsons_annotatedInterface(SimpsonSpec spec) {
			return customerRepo.findAll(spec);
		}

		@RequestMapping("/simpsons")
		@ResponseBody
		public Object listSimpsonsUsingRepositoryWithCacheSupport(
				@Spec(path = "lastName", params = "lastName", spec = Equal.class)
				@JoinFetch(paths = {"orders", "badges"}) Specification<Customer> spec) {
			return customerRepoWithCacheSupport.findAll(spec);
		}

		@RequestMapping("/non-cache/simpsons")
		@ResponseBody
		public Object listSimpsons(
				@Spec(path = "lastName", params = "lastName", spec = Equal.class)
				@JoinFetch(paths = {"orders", "badges"}) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Autowired
	CacheManager customersCacheManager;

	@AfterEach
	public void clearCache() {
		customersCacheManager.getCache("customers").clear();
	}

	@Test
	public void specificationSearchResultsShouldBeCached_annotatedInterface() throws Exception {

		mockMvc.perform(post("/cache-test/interface/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk());

		assertThat(cacheEntries().size()).isEqualTo(1);

		mockMvc.perform(post("/cache-test/interface/simpsons")
				.param("lastName", "Szyslak"))
				.andExpect(status().isOk());

		assertThat(cacheEntries().size()).isEqualTo(2);
	}

	@Test
	public void specificationSearchResultsShouldBeCached() throws Exception {

		mockMvc.perform(post("/cache-test/simpsons")
						.param("lastName", "Simpson"))
				.andExpect(status().isOk());

		assertThat(cacheEntries().size()).isEqualTo(1);

		mockMvc.perform(post("/cache-test/simpsons")
						.param("lastName", "Szyslak"))
				.andExpect(status().isOk());

		assertThat(cacheEntries().size()).isEqualTo(2);
	}

	@Test
	public void specificationSearchResultsShouldBeReturnedFromCache_annotatedInterface() throws Exception {
		Customer homer = customer("Homer", "Simpson").build(em);
		Customer moe = customer("Moe", "Szyslak").build(em);

		mockMvc.perform(post("/cache-test/interface/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
		assertThat(cacheEntries().size()).isEqualTo(1);
		assertThat(cachedResults()).containsExactlyInAnyOrder(singletonList(homer));

		mockMvc.perform(post("/cache-test/interface/simpsons")
						.param("lastName", "Szyslak"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());

		assertThat(cacheEntries().size())
				.isEqualTo(2);
		assertThat(cachedResults())
				.containsExactlyInAnyOrder(singletonList(homer), singletonList(moe));

		Customer marge = customer("Marge", "Simpson").build(em);

		mockMvc.perform(post("/cache-test/interface/non-cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());

		assertThat(cacheEntries().size())
				.isEqualTo(2);
		assertThat(cachedResults())
				.containsExactlyInAnyOrder(singletonList(homer), singletonList(moe));

		mockMvc.perform(post("/cache-test/interface/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());

		assertThat(cacheEntries().size())
				.isEqualTo(2);
		assertThat(cachedResults())
				.containsExactlyInAnyOrder(singletonList(homer), singletonList(moe));

	}

	@Test
	public void specificationSearchResultsShouldBeReturnedFromCache() throws Exception {
		Customer homer = customer("Homer", "Simpson").build(em);
		Customer moe = customer("Moe", "Szyslak").build(em);

		mockMvc.perform(post("/cache-test/simpsons")
						.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
		assertThat(cacheEntries().size()).isEqualTo(1);
		assertThat(cachedResults()).containsExactlyInAnyOrder(singletonList(homer));

		mockMvc.perform(post("/cache-test/simpsons")
						.param("lastName", "Szyslak"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());

		assertThat(cacheEntries().size())
				.isEqualTo(2);
		assertThat(cachedResults())
				.containsExactlyInAnyOrder(singletonList(homer), singletonList(moe));

		Customer marge = customer("Marge", "Simpson").build(em);

		mockMvc.perform(post("/cache-test/non-cache/simpsons")
						.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());

		assertThat(cacheEntries().size())
				.isEqualTo(2);
		assertThat(cachedResults())
				.containsExactlyInAnyOrder(singletonList(homer), singletonList(moe));

		mockMvc.perform(post("/cache-test/simpsons")
						.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());

		assertThat(cacheEntries().size())
				.isEqualTo(2);
		assertThat(cachedResults())
				.containsExactlyInAnyOrder(singletonList(homer), singletonList(moe));

	}

	private List<List<Customer>> cachedResults(){
		List<List<Customer>> cachedResults = new ArrayList<>();

		Iterator<Object> cachedResultsIterator = cacheEntries().values().iterator();
		while (cachedResultsIterator.hasNext()) {
			cachedResults.add((List<Customer>) cachedResultsIterator.next());
		}

		return cachedResults;
	}

	@SuppressWarnings("unchecked")
	public ConcurrentMap<Object, Object> cacheEntries() {
		ConcurrentMapCache concurrentMapCache = (ConcurrentMapCache) customersCacheManager.getCache("customers");
		return (ConcurrentMap<Object, Object>) ReflectionUtils.get(concurrentMapCache, "store");
	}

}
