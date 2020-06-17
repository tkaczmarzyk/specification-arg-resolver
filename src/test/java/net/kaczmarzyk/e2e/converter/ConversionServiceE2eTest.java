package net.kaczmarzyk.e2e.converter;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConversionServiceE2eTest extends E2eTestBase {
	
	@Controller
	@RequestMapping("/customers")
	public static class InstantSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(params = "address")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_defaultInstantFormat(
				@Spec(path = "address", params = "address", spec = Equal.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
	}
	
	@Autowired
	List<HandlerMethodArgumentResolver> resolvers;
	
	@Test
	public void findsByAddressUsingConversionServiceWithCustomConverter() throws Exception {
		mockMvc.perform(get("/customers")
				.param("address", "Evergreen Terrace"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[6]").doesNotExist());
	}
	
}
