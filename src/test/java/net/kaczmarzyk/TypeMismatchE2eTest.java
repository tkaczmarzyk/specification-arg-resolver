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
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.NestedServletException;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Tomasz Kaczmarzyk
 */
public class TypeMismatchE2eTest extends E2eTestBase {
	
	@Controller
	public static class TestController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/poly/customers", params = { "id" })
		@ResponseBody
		public Object findById(
				@Spec(path = "id", params = "id", spec = Equal.class, onTypeMismatch=EMPTY_RESULT) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/poly/customers", params = { "query" })
		@ResponseBody
		public Object findByIdOrFirstName(@Or({
				@Spec(path = "id", params = "query", spec = Equal.class, onTypeMismatch=EMPTY_RESULT),
				@Spec(path = "firstName", params = "query", spec = LikeIgnoreCase.class) }) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/poly/customers", params = { "genderEmpty" })
		@ResponseBody
		public Object findByGenderIn_emptyOnTypeMismatch(
				@Spec(path = "gender", params = "genderEmpty", spec = In.class, onTypeMismatch = EMPTY_RESULT) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
		
		@RequestMapping(value = "/poly/customers", params = { "genderException" })
		@ResponseBody
		public Object findByGenderIn_exceptionOnTypeMismatch(
				@Spec(path = "gender", params = "genderException", spec = In.class, onTypeMismatch = EXCEPTION) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}
	
	@Test
	public void returnsEmptyResultIfStringProvidedInsteadOfNumericalId() throws Exception {
		mockMvc.perform(get("/poly/customers")
				.param("id", "test")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0]").doesNotExist());
	}
	
	@Test
	public void returnsEmptyResultIfNoneOfTheValuesAreValidEnums() throws Exception {
		mockMvc.perform(get("/poly/customers")
				.param("genderEmpty", "ROBOT", "ALIEN")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0]").doesNotExist());
	}

	@Test
	public void filtersOnlyByValidEnumValuesIfEmptyResultOnTypeMismatchBehaviourSpecified() throws Exception {
		mockMvc.perform(get("/poly/customers")
				.param("genderEmpty", "MALE", "ROBOT", "ALIEN")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}
	
	@Test
	public void throwsExceptionIfOneOfSpecifiedEnumValuesIsInvalid() throws Exception {
		
		assertThrows(NestedServletException.class,
				() -> mockMvc.perform(get("/poly/customers")
				.param("genderException", "MALE", "FEMALE", "ALIEN")
				.accept(MediaType.APPLICATION_JSON)));
	}

	@Test
	public void findsByEitherIdOrNameEvenThoughTheirTypesAreDifferent() throws Exception {
		mockMvc.perform(get("/poly/customers")
				.param("query", "o")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
		
		mockMvc.perform(get("/poly/customers")
				.param("query", homerSimpson.getId().toString())
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
