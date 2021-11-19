/**
 * Copyright 2014-2020 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
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
 * @author Jakub Radlica
 */
public class ConstSpELValE2eTest extends IntegrationTestBaseWithSARConfiguredWithApplicationContext {
	
	@Spec(path = "lastName", params = "lastName", spec = Equal.class, constVal = "Flanders")
	private interface LastNameSpecWithRawString extends Specification<Customer> {
	}
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "#{new String('Sim').concat(new String(T(java.util.Base64).getDecoder().decode('cHNvbg==')))}",
			valueInSpEL = true
	)
	private interface LastNameSpecWithConstValueInSpEL extends Specification<Customer> {
	}
	
	@Spec(
			path = "birthDate",
			spec = GreaterThanOrEqual.class,
			constVal = "#{T(java.time.LocalDate).now()}",
			valueInSpEL = true
	)
	private interface CustomersBornInTheFuture extends Specification<Customer> {
	}
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "#{'${SpEL-support.lastName.prefix}'.concat('ak')}",
			valueInSpEL = true
	)
	private interface LastNameSpecWithConstValueInSpELWithPropertyPlaceholder extends Specification<Customer> {
	}
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "${SpEL-support.lastName.value}",
			valueInSpEL = true
	)
	private	interface LastNameSpecWithConstValueWithPropertyPlaceholder extends Specification<Customer> {
	}

	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "#{${SpEL-support.lastName",
			valueInSpEL = false
	)
	private interface LastNameWithRawSpEL extends Specification<Customer> {
	}
	
	@Controller
	static class TestController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping("/constValRawString")
		@ResponseBody
		public Object listsCustomersUsingRawStringConstValue(LastNameSpecWithRawString spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/constValInSpEL")
		@ResponseBody
		public Object listsCustomersUsingSpecWithConstValueInSpEL(LastNameSpecWithConstValueInSpEL spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/customersBornInTheFutureConstVal")
		@ResponseBody
		public Object listsCustomersWhichCameFromTheFuture(CustomersBornInTheFuture spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/constValInSpELWithPropertyPlaceholder")
		@ResponseBody
		public Object listsCustomersUsingSpecWithConstValueInSpELWithPropertyPlaceholder(LastNameSpecWithConstValueInSpELWithPropertyPlaceholder spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/constValWithPropertyPlaceholder")
		@ResponseBody
		public Object listsCustomersUsingSpecWithConstValueWithPropertyPlaceholder(LastNameSpecWithConstValueWithPropertyPlaceholder spec) {
			return customerRepo.findAll(spec);
		}

		@RequestMapping("/constValWithRawSpEL")
		@ResponseBody
		public Object listsCustomersUsingSpecRawSpELConstValue(LastNameWithRawSpEL spec) {
			return customerRepo.findAll(spec);
		}

	}
	
	@BeforeEach
	public void initializeTestData() {
		customer("Homer", "Simpson").birthDate(LocalDate.of(1970, 03, 21));
		customer("Marge", "Simpson").birthDate(LocalDate.of(1972, 7, 13)).build(em);
		customer("Bart", "Simpson").birthDate(LocalDate.of(1992, 2, 23)).build(em);
		customer("Lisa", "Simpson").birthDate(LocalDate.of(1994, 11, 7)).build(em);
		customer("Maggie", "Simpson").birthDate(LocalDate.of(1966, 4, 1)).build(em);
		customer("Maggie", "Simpson").birthDate(LocalDate.of(1966, 4, 1)).build(em);
		customer("Moe", "Szyslak").build(em);
		customer("Minnie", "Szyslak").build(em);
		customer("Ned", "Flanders").build(em);
		customer("Bart Jr.", "Simpsonx").birthDate(LocalDate.of(3000, 6, 22)).build(em);
		customer("BartWithInvalidLastName", "#{${SpEL-support.lastName").build(em);
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingRawStringConstValue() throws Exception {
		mockMvc.perform(get("/constValRawString")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValInSpEL() throws Exception {
		mockMvc.perform(get("/constValInSpEL")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[5]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValInSpELWithTemporalConstructor() throws Exception {
		mockMvc.perform(get("/customersBornInTheFutureConstVal")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Bart Jr.')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValInSpELWithPropertyPlaceholder() throws Exception {
		mockMvc.perform(get("/constValInSpELWithPropertyPlaceholder")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValWithPropertyPlaceholder() throws Exception {
		mockMvc.perform(get("/constValWithPropertyPlaceholder")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void filtersBySingleSpecWithoutParamUsingRawSpELConstValue() throws Exception {
		mockMvc.perform(get("/constValWithRawSpEL")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='BartWithInvalidLastName')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
