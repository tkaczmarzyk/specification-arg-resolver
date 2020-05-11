package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JoinFetchE2eTest extends E2eTestBase {

	@RestController
	public static class TestController {

		@Autowired
		private CustomerRepository customerRepository;

		@RequestMapping("/join-fetch/customers")
		public Object findByName(

				@JoinFetch(paths = "badges")
				@Spec(path = "firstName", spec = Equal.class) Specification<Customer> spec) {

			return customerRepository.findAll(spec);
		}

	}

	@Test
	public void createsDistinctQueryByDefault() throws Exception {
		mockMvc.perform(get("/join-fetch/customers")
				.param("firstName", "Homer")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

}