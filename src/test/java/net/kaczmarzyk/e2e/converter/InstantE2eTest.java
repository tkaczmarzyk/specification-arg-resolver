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
package net.kaczmarzyk.e2e.converter;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.OffsetDateTime;

import static java.time.ZoneOffset.ofHours;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InstantE2eTest extends E2eTestBase {
	
	@Controller
	@RequestMapping("/customers")
	public static class InstantSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantBefore")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantBefore", spec = LessThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantBefore_customFormat")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantBefore_customFormat", config = "yyyy-MM-dd\'T\' HH:mm:ss.SSS_XXX", spec = LessThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = "dateOfNextSpecialOfferInstantBefore_customFormatWithDateOnly")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_customInstantFormatWithDateOnly(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantBefore_customFormatWithDateOnly", config = "yyyy-MM-dd", spec = LessThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantAfter")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferAfter_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantAfter", spec = GreaterThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = "dateOfNextSpecialOfferInstantAfter_customFormat")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferAfter_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantAfter_customFormat", config = "yyyy-MM-dd\'T\' HH:mm(ss.SSS XXX)", spec = GreaterThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = "dateOfNextSpecialOfferInstantAfter_customFormatWithDateOnly")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferAfter_customInstantFormatWithDateOnly(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantAfter_customFormatWithDateOnly", config = "yyyy-MM-dd", spec = GreaterThan.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = {"dateOfNextSpecialOfferInstantAfter", "dateOfNextSpecialOfferInstantBefore"})
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBetween_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = {"dateOfNextSpecialOfferInstantAfter", "dateOfNextSpecialOfferInstantBefore"}, spec = Between.class) Specification<Customer> spec) {
 			return customerRepository.findAll(spec);
		}
		
		@RequestMapping(params = {"dateOfNextSpecialOfferInstantBefore_customFormat", "dateOfNextSpecialOfferInstantAfter_customFormat"})
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBetween_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = {"dateOfNextSpecialOfferInstantBefore_customFormat", "dateOfNextSpecialOfferInstantAfter_customFormat"}, config = "yyyy-MM-dd\'T\'HH:mm:ss.SSS (XXX)", spec = Between.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = {"dateOfNextSpecialOfferInstantAfter_customFormatWithDateOnly", "dateOfNextSpecialOfferInstantBefore_customFormatWithDateOnly"})
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBetween_customInstantFormatWithDateOnly(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = {"dateOfNextSpecialOfferInstantAfter_customFormatWithDateOnly", "dateOfNextSpecialOfferInstantBefore_customFormatWithDateOnly"}, config = "yyyy-MM-dd", spec = Between.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = "dateOfNextSpecialOfferInstantEqual_customFormat")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferEqual_customInstantFormatWithDateOnly(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantEqual_customFormat", config = "yyyy-MM-dd", spec = Equal.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = "dateOfNextSpecialOfferInstantEqualDay")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferEqualDay_defaultInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantEqualDay", spec = EqualDay.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}

		@RequestMapping(params = "dateOfNextSpecialOfferInstantEqualDay_customFormat")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferEqualDay_customInstantFormat(
				@Spec(path = "dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstantEqualDay_customFormat", config = "yyyy-MM-dd", spec = EqualDay.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
	}
	
	@Test
	public void findsByInstantBeforeWithDefaultInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantBefore", "2020-07-16T16:17:00.000+04:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void findsByInstantBeforeWithCustomInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantBefore_customFormat", "2020-07-16T 16:17:00.000_+03:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test
	public void findsByInstantBeforeWithCustomInstantFormatWithDateOnly() throws Exception {
		mockMvc.perform(get("/customers")
						.param("dateOfNextSpecialOfferInstantBefore_customFormatWithDateOnly", "2020-07-17"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[5]").doesNotExist());
	}
	
	@Test
	public void findsByInstantAfterWithDefaultInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter", "2020-07-16T16:17:00.000+00:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void findsByInstantAfterWithCustomInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter_customFormat", "2020-07-17T 16:17(00.000 +04:00)"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByInstantAfterWithCustomInstantFormatWithDateOnly() throws Exception {
		mockMvc.perform(get("/customers")
						.param("dateOfNextSpecialOfferInstantAfter_customFormatWithDateOnly", "2020-07-17"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());
	}
	
	@Test
	public void findsByInstantBetweenWithDefaultInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter", "2020-07-16T16:17:00.000+00:00")
				.param("dateOfNextSpecialOfferInstantBefore", "2020-07-19T16:17:00.000+04:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}
	
	@Test
	public void findsByInstantBetweenWithCustomInstantFormat() throws Exception {
		mockMvc.perform(get("/customers")
				.param("dateOfNextSpecialOfferInstantAfter", "2020-07-16T16:17:00.000+00:00")
				.param("dateOfNextSpecialOfferInstantBefore", "2020-07-19T16:17:00.000+04:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Minnie')]").exists())
			.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[4]").doesNotExist());
	}

	@Test
	public void findsByInstantBetweenWithCustomInstantFormatWithDateOnly() throws Exception {
		mockMvc.perform(get("/customers")
						.param("dateOfNextSpecialOfferInstantAfter_customFormatWithDateOnly", "2020-07-16")
						.param("dateOfNextSpecialOfferInstantBefore_customFormatWithDateOnly", "2020-07-17"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test
	public void findsByInstantEqualWithCustomDateFormatWithDateOnly() throws Exception {
		customer("Barry", "Benson")
				.nickName("Bee")
				.nextSpecialOffer(OffsetDateTime.of(2022, 12, 13, 0, 0, 0, 0, ofHours(0)))
				.build(em);

		mockMvc.perform(get("/customers")
						.param("dateOfNextSpecialOfferInstantEqual_customFormat", "2022-12-13")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Barry')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByInstantEqualDayWithDefaultInstantFormat() throws Exception {
		customer("Barry", "Benson")
				.nextSpecialOffer(OffsetDateTime.of(2020, 7, 20, 0, 0, 0, 0, ofHours(0)))
				.build(em);
		customer("Adam", "Flayman")
				.nextSpecialOffer(OffsetDateTime.of(2020, 7, 18, 23, 59, 59, 999999000, ofHours(0))) //date as close to midnight as possible. Due to the test database datetime precision specifying more nanoseconds will cause rounding to the next hour
				.build(em);

		mockMvc.perform(get("/customers")
						.param("dateOfNextSpecialOfferInstantEqualDay", "2020-07-19T14:11:00.000+04:00"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByInstantEqualDayWithCustomInstantFormatIgnoringTime() throws Exception {
		customer("Barry", "Benson")
				.nextSpecialOffer(OffsetDateTime.of(2020, 7, 20, 0, 0, 0, 0, ofHours(0)))
				.build(em);
		customer("Adam", "Flayman")
				.nextSpecialOffer(OffsetDateTime.of(2020, 7, 18, 23, 59, 59, 999999000, ofHours(0))) //date as close to midnight as possible. Due to the test database datetime precision specifying more nanoseconds will cause rounding to the next hour
				.build(em);

		mockMvc.perform(get("/customers")
						.param("dateOfNextSpecialOfferInstantEqualDay_customFormat", "2020-07-19"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
