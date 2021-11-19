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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * @author Tomasz Kaczmarzyk
 */
public class PathVariableHandlingE2eTest extends E2eTestBase {

	@Controller
	public static class TestController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/pathVar/customers/{customerId}")
		@ResponseBody
		public Object findById(
				@Spec(path = "id", pathVars = "customerId", spec = Equal.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/pathVar/customers/{customerId:[0-9]+}/regexp")
		@ResponseBody
		public Object findByIdWithRegex(
				@Spec(path = "id", pathVars = "customerId", spec = Equal.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/pathVar/customers/{customerLastName}", params = "gender")
		@ResponseBody
		public Object findCustomerOrdersByLastNameAndGender(
				@And({
						@Spec(path = "lastName", pathVars = "customerLastName", spec = Equal.class),
						@Spec(path = "gender", params = "gender", spec = Equal.class)
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@Controller
		@RequestMapping(value = "/{pathName:[a-zA-Z]+}/pathVar2")
		public static class TestControllerWithClassRequestMapping {

			@Autowired
			CustomerRepository customerRepo;

			@RequestMapping(value = "/customers/{customerId:[0-9]+}/regexp")
			@ResponseBody
			public Object findById(
					@Spec(path = "id", pathVars = "customerId", spec = Equal.class) Specification<Customer> spec) {
				return customerRepo.findAll(spec);
			}
		}
	}

	@Test
	public void findsByIdProvidedInPathVariable() throws Exception {
		mockMvc.perform(get("/pathVar/customers/" + homerSimpson.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByIdProvidedInPathVariableWithRegex() throws Exception {
		mockMvc.perform(get("/pathVar/customers/" + homerSimpson.getId()+"/regexp")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByIdProviedInPathVariableAndByRegularSpec() throws Exception {
		mockMvc.perform(get("/pathVar/customers/Simpson?gender=FEMALE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());

		mockMvc.perform(get("/pathVar/customers/Simpson?gender=MALE")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByIdProvidedInPathVariableWithRegexpWithRequestMappingAtClassLevel() throws Exception {
		mockMvc.perform(get("/rootPath/pathVar2/customers/" + homerSimpson.getId()+"/regexp")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].firstName").value("Homer"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void returnsHttp404WhenSentRequestContainsPathVarInInvalidFormat() throws Exception {
		mockMvc.perform(get("/rootPath/pathVar2/customers/invalidCustomerIdFormat/regexp")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
}
