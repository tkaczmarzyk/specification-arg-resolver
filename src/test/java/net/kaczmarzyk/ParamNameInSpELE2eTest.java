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
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithSARConfiguredWithApplicationContext;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Tomasz Kaczmarzyk
 */
public class ParamNameInSpELE2eTest extends IntegrationTestBaseWithSARConfiguredWithApplicationContext {
	
	@Spec(path = "lastName", params = "${SpEL-support.paramName}", spec = Equal.class)
	private	interface LastNameSpecWithSpELNotEnabled extends Specification<Customer> {
	}
	
	@Spec(path = "lastName", params = "${SpEL-support.paramName}", spec = Equal.class, paramsInSpEL = true)
	private	interface LastNameSpecWithHttpParamNameLoadedFromProperties extends Specification<Customer> {
	}
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			params = "#{new String('compl').concat(new String(T(java.util.Base64).getDecoder().decode('ZXhQYXJhbQ==')))}", // complexParam
			paramsInSpEL = true
	)
	private interface LastNameSpecWithPramNameLoadedFromSpELExpression extends Specification<Customer> {
	}
	
	@Controller
	static class TestController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping("/param-spel/not-enabled")
		@ResponseBody
		public Object listCustomersWithInvalidSpec(LastNameSpecWithSpELNotEnabled spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/param-spel/property")
		@ResponseBody
		public Object listCustomersWithParamNameTakenFromProperties(LastNameSpecWithHttpParamNameLoadedFromProperties spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/param-spel/complex-spel")
		@ResponseBody
		public Object listCustomersWithParamNameFromComplexSpELExpression(LastNameSpecWithPramNameLoadedFromSpELExpression spec) {
			return customerRepo.findAll(spec);
		}
	}
	
	@BeforeEach
	public void initializeTestData() {
		customer("Homer", "Simpson").birthDate(LocalDate.of(1970, 03, 21));
		customer("Marge", "Simpson").birthDate(LocalDate.of(1972, 7, 13)).build(em);
		customer("Ned", "Flanders").birthDate(LocalDate.of(1966, 4, 1)).build(em);
	}
	
	@Test
	public void spelDoesNotWorkUnlessExplicityEnabled() throws Exception {
		mockMvc.perform(get("/param-spel/not-enabled")
				.param("parameterLoadedFromSpel", "Flanders")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.lastName=='Simpson')]").exists()); // filtering not applied
	}
	
	@Test
	public void usesHttpParameterSpecifiedInProperties() throws Exception {
		mockMvc.perform(get("/param-spel/property")
				.param("parameterLoadedFromSpel", "Flanders")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Simpson')]").doesNotExist())
				.andExpect(jsonPath("$[0].firstName").value("Ned"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void usesHttpParameterSpecifiedInComplexSpelExpression() throws Exception {
		mockMvc.perform(get("/param-spel/complex-spel")
				.param("complexParam", "Flanders")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Simpson')]").doesNotExist())
				.andExpect(jsonPath("$[0].firstName").value("Ned"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
