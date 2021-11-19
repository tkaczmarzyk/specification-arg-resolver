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
import net.kaczmarzyk.spring.data.jpa.domain.Null;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class NullE2eTest extends E2eTestBase {

	@Controller
	public static class TestController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping("/characters")
		@ResponseBody
		public Object findCharacters(
				@Spec(path = "nickName", params = "nickNameNull", spec = Null.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}
	}

	@Test
	public void findsEntitiesWithNullAttributeValue() throws Exception {
		mockMvc.perform(get("/characters?nickNameNull=true")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").doesNotExist())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").doesNotExist())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").doesNotExist())
				.andExpect(jsonPath("$[5]").doesNotExist());
	}

	@Test
	public void findsEntitiesWithNotNullAttributeValue() throws Exception {
		mockMvc.perform(get("/characters?nickNameNull=false")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").doesNotExist())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").doesNotExist())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").doesNotExist())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").doesNotExist())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test
	public void findsEntitiesWithNoFiltering() throws Exception {
		mockMvc.perform(get("/characters")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists());
	}
}
