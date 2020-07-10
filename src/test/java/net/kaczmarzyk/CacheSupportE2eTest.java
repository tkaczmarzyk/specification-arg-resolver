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
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CacheSupportE2eTest extends IntegrationTestBaseWithConfiguredCache {

	@Spec(path = "lastName", params = "lastName", spec = Equal.class)
	@JoinFetch(paths = {"orders", "badges"})
	private static interface SimpsonSpec extends Specification<Customer> {
	}

	@Controller
	public static class TestController {

		@Autowired
		CustomerRepositoryWithCache customerRepoWithCacheSupport;

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping("/cache/simpsons")
		@ResponseBody
		public Object listSimpsonsUsingRepositoryWithCacheSupport(SimpsonSpec spec) {
			return customerRepoWithCacheSupport.findAll(spec);
		}

		@RequestMapping("/non-cache/simpsons")
		@ResponseBody
		public Object listSimpsons(SimpsonSpec spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Autowired
	CacheManager customersCacheManager;

	@After
	public void clearCache() {
		customersCacheManager.getCache("customers").clear();
	}

	@Test
	public void specificationResultShouldBeCached() throws Exception {

		mockMvc.perform(post("/cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk());
		Assertions.assertThat(cacheEntries().size()).isEqualTo(1);

		mockMvc.perform(post("/cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk());
		Assertions.assertThat(cacheEntries().size()).isEqualTo(1);

		mockMvc.perform(post("/cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk());
		Assertions.assertThat(cacheEntries().size()).isEqualTo(1);

		mockMvc.perform(post("/cache/simpsons")
				.param("lastName", "Szyslak"))
				.andExpect(status().isOk());
		Assertions.assertThat(cacheEntries().size()).isEqualTo(2);
	}

	@Test
	public void specificationResultShouldBeCached2() throws Exception {
		Customer homer = customer("Homer", "Simpson").build(em);

		mockMvc.perform(post("/cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());

		Assertions.assertThat(cacheEntries().size()).isEqualTo(1);

		Assertions.assertThat(cachedResults()).containsExactly(homer);

		Customer marge = customer("Marge", "Simpson").build(em);

		mockMvc.perform(post("/non-cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());

		mockMvc.perform(post("/cache/simpsons")
				.param("lastName", "Simpson"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());

		Assertions.assertThat(cacheEntries().size()).isEqualTo(1);
		Assertions.assertThat(cachedResults()).containsExactly(homer);

	}

	private List<Customer> cachedResults(){
		return (List<Customer>) cacheEntries().values().iterator().next();
	}

	@SuppressWarnings("unchecked")
	public ConcurrentMap<Object, Object> cacheEntries() {
		ConcurrentMapCache concurrentMapCache = (ConcurrentMapCache) customersCacheManager.getCache("customers");
		return (ConcurrentMap<Object, Object>) ReflectionUtils.get(concurrentMapCache, "store");
	}

}
