package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ParamSeparatorE2eTest extends E2eTestBase {

	@Controller
	@RequestMapping("/param-separator")
	public static class ParamSeparatorSpecController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/firstNameIn")
		@ResponseBody
		public Object findCustomersByFirstNameUsingParamSeparator(
				@Spec(path = "firstName", params = "firstNameIn", paramSeparator = ',', spec = In.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}
	}

	@Test
	public void doesNoFilteringIfParameterUsingParamSeparatorIsMissing() throws Exception {
		mockMvc.perform(get("/param-separator/firstNameIn")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$[8]").doesNotExist());
	}
}
