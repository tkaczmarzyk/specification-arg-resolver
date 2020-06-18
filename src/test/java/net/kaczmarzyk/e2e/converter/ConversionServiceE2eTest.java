package net.kaczmarzyk.e2e.converter;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithConfiguredConversionService;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConversionServiceE2eTest extends IntegrationTestBaseWithConfiguredConversionService {
	
	@Controller
	@RequestMapping("/customers")
	public static class InstantSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(params = "address")
		@ResponseBody
		public Object findCustomersByAddressUsingCustomConverter(
				@Spec(path = "address", params = "address", spec = Equal.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

	}
	
	@Before
	public void initializeTestData() {
		customer("Homer", "Simpson")
				.street("Evergreen Terrace").build(em);
		
		customer("Marge", "Simpson")
				.street("Evergreen Terrace").build(em);
		
		customer("Moe", "Szyslak")
				.street("Unknown").build(em);
	}
	
	@Test
	public void findsByAddressUsing() throws Exception {
		mockMvc.perform(get("/customers")
				.param("address", "Evergreen Terrace"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

}
