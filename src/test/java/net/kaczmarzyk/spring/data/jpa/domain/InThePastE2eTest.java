/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.OffsetDateTime;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class InThePastE2eTest extends E2eTestBase {

	@Controller
	private static class InThePastSpecController {

		@Autowired
		CustomerRepository customerRepo;

		@ResponseBody
		@RequestMapping(value = "/customersWithExpiredSpecialOffer")
		public Object findByCustomersWithExpiredSpecialOffer(
			@Spec(path="dateOfNextSpecialOffer", spec=InThePast.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}

	@Before
	public void initData() {
		customer("Barry", "Benson")
			.nextSpecialOffer(OffsetDateTime.now().plusDays(7))
			.build(em);
		customer("Vanessa", "Bloom")
			.nextSpecialOffer(OffsetDateTime.now().minusDays(7))
			.build(em);
	}

	@Test
	public void findsByNextSpecialOfferFromPast() throws Exception {
		mockMvc.perform(get("/customersWithExpiredSpecialOffer")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1].firstName").value("Marge"))
			.andExpect(jsonPath("$[2].firstName").value("Bart"))
			.andExpect(jsonPath("$[3].firstName").value("Lisa"))
			.andExpect(jsonPath("$[4].firstName").value("Maggie"))
			.andExpect(jsonPath("$[5].firstName").value("Moe"))
			.andExpect(jsonPath("$[6].firstName").value("Minnie"))
			.andExpect(jsonPath("$[7].firstName").value("Ned"))
			.andExpect(jsonPath("$[8].firstName").value("Vanessa"))
			.andExpect(jsonPath("$[9]").doesNotExist());
	}
}
