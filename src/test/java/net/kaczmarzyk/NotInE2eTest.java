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
import net.kaczmarzyk.spring.data.jpa.domain.NotIn;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


/**
 * @author Tomasz Kaczmarzyk
 */
public class NotInE2eTest extends E2eTestBase {

	@Controller
	public static class InSpecController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/not-in/customers", params = "firstNameNotIn")
		@ResponseBody
		public Object findCustomersByFirstName(
				@Spec(path = "firstName", params = "firstNameNotIn", spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/not-in/customers-param-separator", params = "firstNameNotIn")
		@ResponseBody
		public Object findCustomersByFirstNameUsingParamSeparator(
				@Spec(path = "firstName", params = "firstNameNotIn", paramSeparator = '!', spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/not-in/customers", params = "idNotIn")
		@ResponseBody
		public Object findCustomersById(
				@Spec(path = "id", params = "idNotIn", spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/not-in/customers-param-separator", params = "idNotIn")
		@ResponseBody
		public Object findCustomersByIdUsingParamSeparator(
				@Spec(path = "id", params = "idNotIn", paramSeparator = '_', spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/not-in/customers", params = "registrationDateNotIn")
		@ResponseBody
		public Object findCustomersByRegistrationDate(
				@Spec(path = "registrationDate", params = "registrationDateNotIn", spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/not-in/customers-param-separator", params = "registrationDateNotIn")
		@ResponseBody
		public Object findCustomersByRegistrationDateUsingParamSeparator(
				@Spec(path = "registrationDate", params = "registrationDateNotIn", paramSeparator = ',', spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}


		@RequestMapping(value = "/not-in/customers", params = "genderNotIn")
		@ResponseBody
		public Object findCustomersByGender(
				@Spec(path = "gender", params = "genderNotIn", spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/not-in/customers-param-separator", params = "genderNotIn")
		@ResponseBody
		public Object findCustomersByGenderUsingParamSeparator(
				@Spec(path = "gender", params = "genderNotIn", paramSeparator = '\'', spec = NotIn.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}

	@Test
	public void findsByListOfNotAllowedStringValues() throws Exception {
		mockMvc.perform(get("/not-in//customers")
				.param("firstNameNotIn", "Homer", "Marge")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Bart"))
			.andExpect(jsonPath("$[1].firstName").value("Lisa"))
			.andExpect(jsonPath("$[2].firstName").value("Maggie"))
			.andExpect(jsonPath("$[3].firstName").value("Moe"))
			.andExpect(jsonPath("$[4].firstName").value("Minnie"))
			.andExpect(jsonPath("$[5].firstName").value("Ned"))
			.andExpect(jsonPath("$[6]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedStringValuesUsingParamSeparator() throws Exception {
		mockMvc.perform(get("/not-in/customers-param-separator?firstNameNotIn=Homer!Marge")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Bart"))
			.andExpect(jsonPath("$[1].firstName").value("Lisa"))
			.andExpect(jsonPath("$[2].firstName").value("Maggie"))
			.andExpect(jsonPath("$[3].firstName").value("Moe"))
			.andExpect(jsonPath("$[4].firstName").value("Minnie"))
			.andExpect(jsonPath("$[5].firstName").value("Ned"))
			.andExpect(jsonPath("$[6]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedLongValues() throws Exception {
		mockMvc.perform(get("/not-in/customers")
				.param("idNotIn",
						homerSimpson.getId().toString(),
						moeSzyslak.getId().toString(),
						margeSimpson.getId().toString(),
						nedFlanders.getId().toString())
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Bart"))
			.andExpect(jsonPath("$[1].firstName").value("Lisa"))
			.andExpect(jsonPath("$[2].firstName").value("Maggie"))
			.andExpect(jsonPath("$[3].firstName").value("Minnie"))
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedLongValuesUsingParamSeparator() throws Exception {
		String idNotInParam = "?idNotIn="
				+ homerSimpson.getId().toString() + "_"
				+ moeSzyslak.getId().toString() + "_"
				+ margeSimpson.getId().toString() + "_"
				+ nedFlanders.getId().toString();

		mockMvc.perform(get("/not-in/customers-param-separator" + idNotInParam)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Bart"))
			.andExpect(jsonPath("$[1].firstName").value("Lisa"))
			.andExpect(jsonPath("$[2].firstName").value("Maggie"))
			.andExpect(jsonPath("$[3].firstName").value("Minnie"))
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedDateValues() throws Exception {
		mockMvc.perform(get("/not-in//customers")
				.param("registrationDateNotIn", "2014-03-30", "2014-03-31")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Marge"))
			.andExpect(jsonPath("$[2].firstName").value("Bart"))
			.andExpect(jsonPath("$[3].firstName").value("Moe"))
			.andExpect(jsonPath("$[4].firstName").value("Minnie"))
			.andExpect(jsonPath("$[5].firstName").value("Ned"))
			.andExpect(jsonPath("$[6]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedDateValuesUsingParamSeparator() throws Exception {
		mockMvc.perform(get("/not-in//customers-param-separator?registrationDateNotIn=2014-03-30,2014-03-31")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Marge"))
			.andExpect(jsonPath("$[2].firstName").value("Bart"))
			.andExpect(jsonPath("$[3].firstName").value("Moe"))
			.andExpect(jsonPath("$[4].firstName").value("Minnie"))
			.andExpect(jsonPath("$[5].firstName").value("Ned"))
			.andExpect(jsonPath("$[6]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedEnumValues() throws Exception {
		mockMvc.perform(get("/not-in/customers")
				.param("genderNotIn", "MALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test
	public void findsByListOfNotAllowedEnumValuesUsingParamSeparator() throws Exception {
		mockMvc.perform(get("/not-in/customers-param-separator?genderNotIn=MALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test
	public void ignoresUnparseableIntsWhenFilteringOnIntProperty() throws Exception {
		mockMvc.perform(get("/not-in//customers")
				.param("idNotIn", homerSimpson.getId().toString(), "abc")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Marge"))
			.andExpect(jsonPath("$[1].firstName").value("Bart"))
			.andExpect(jsonPath("$[2].firstName").value("Lisa"))
			.andExpect(jsonPath("$[3].firstName").value("Maggie"))
			.andExpect(jsonPath("$[4].firstName").value("Moe"))
			.andExpect(jsonPath("$[5].firstName").value("Minnie"))
			.andExpect(jsonPath("$[6].firstName").value("Ned"))
			.andExpect(jsonPath("$[7]").doesNotExist());
	}
}
