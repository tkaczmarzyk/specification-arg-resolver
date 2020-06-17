package net.kaczmarzyk.e2e.converter;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.NotEqual;
import net.kaczmarzyk.spring.data.jpa.domain.NotIn;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UUIDE2eTest extends E2eTestBase {
	
	@Controller
	public static class UUIDSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(value = "/customers", params = "refCode")
		@ResponseBody
		public Object findCustomersByRefCode(
				@Spec(path = "refCode", params = "refCode", spec = Equal.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "refCodeNotEqual")
		@ResponseBody
		public Object findCustomersByRefCodeNotEqual(
				@Spec(path = "refCode", params = "refCodeNotEqual", spec = NotEqual.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "refCodeIn")
		@ResponseBody
		public Object findCustomersByRefCodeIn(
				@Spec(path = "refCode", params = "refCodeIn", spec = In.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(value = "/customers", params = "refCodeNotIn")
		@ResponseBody
		public Object findCustomersByRefCodeNotIn(
				@Spec(path = "refCode", params = "refCodeNotIn", spec = NotIn.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
	}
	
	@Test
	public void findsByUUIDEqual() throws Exception {
		mockMvc.perform(get("/customers")
				.param("refCode", "05B79D32-7A97-44D9-9AD7-93FB0CBECC80"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void findsByUUIDNotEqual() throws Exception {
		mockMvc.perform(get("/customers")
				.param("refCodeNotEqual", "05B79D32-7A97-44D9-9AD7-93FB0CBECC80"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void findByUUIDIn() throws Exception {
		mockMvc.perform(get("/customers")
				.param("refCodeIn", "05B79D32-7A97-44D9-9AD7-93FB0CBECC80", "31CFE6A0-7450-48B0-BB0E-5E6CD5071131"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void findByUUIDNotIn() throws Exception {
		mockMvc.perform(get("/customers")
				.param("refCodeNotIn", "05B79D32-7A97-44D9-9AD7-93FB0CBECC80", "31CFE6A0-7450-48B0-BB0E-5E6CD5071131"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
}
