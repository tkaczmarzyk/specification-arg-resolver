/**
 * Copyright 2014-2022 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class TimestampE2eTest extends E2eTestBase {
	@Controller
    public static class TimestampSpecsController {

        @Autowired
        CustomerRepository customerRepo;

		@RequestMapping(value = "/customers", params = "lastSeenBefore")
		@ResponseBody
		public Object findCustomersWithLastSeenBefore_defaultTimestampPattern(
				@Spec(path = "lastSeen", params = "lastSeenBefore", spec = LessThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "lastSeenBefore_customFormat")
		@ResponseBody
		public Object findCustomersWithLastSeenBefore_customTimestampPattern(
				@Spec(path = "lastSeen", params = "lastSeenBefore_customFormat", config = "yyyy/MM/dd, HH:mm:ss.SSS", spec = LessThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "lastSeenBefore_customFormatWithDateOnly")
		@ResponseBody
		public Object findCustomersWithLastSeenBefore_customTimestampPatternWithDateOnly(
				@Spec(path = "lastSeen", params = "lastSeenBefore_customFormatWithDateOnly", config = "yyyy-MM-dd", spec = LessThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "lastSeenAfter")
		@ResponseBody
		public Object findCustomersWithLastSeenAfter_defaultTimestampPattern(
				@Spec(path = "lastSeen", params = "lastSeenAfter", spec = GreaterThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "lastSeenAfter_customFormat")
		@ResponseBody
		public Object findCustomersWithLastSeenAfter_customTimestampPattern(
				@Spec(path = "lastSeen", params = "lastSeenAfter_customFormat", config = "yyyy/MM/dd, HH:mm:ss.SSS", spec = GreaterThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "lastSeenAfter_customFormatWithDateOnly")
		@ResponseBody
		public Object findCustomersWithLastSeenAfter_customTimestampPatternWithDateOnly(
				@Spec(path = "lastSeen", params = "lastSeenAfter_customFormatWithDateOnly", config = "yyyy-MM-dd", spec = GreaterThan.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = { "lastSeenAfterTime", "lastSeenBeforeTime" })
		@ResponseBody
		public Object findCustomersWithLastSeenBetween_defaultTimestampPattern(
				@Spec(path = "lastSeen", params = { "lastSeenAfterTime",
						"lastSeenBeforeTime" }, spec = Between.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = { "lastSeenAfterTime_customFormat", "lastSeenBeforeTime_customFormat" })
		@ResponseBody
		public Object findCustomersWithLastSeenBetween_customTimestampPattern(
				@Spec(path = "lastSeen", params = { "lastSeenAfterTime_customFormat", "lastSeenBeforeTime_customFormat" }, config = "yyyy/MM/dd, HH:mm:ss.SSS", spec = Between.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = { "lastSeenAfterTime_customFormatWithDateOnly",
				"lastSeenBeforeTime_customFormatWithDateOnly" })
		@ResponseBody
		public Object findCustomersWithLastSeenBetween_customTimestampPatternWithDateOnly(
				@Spec(path = "lastSeen", params = { "lastSeenAfterTime_customFormatWithDateOnly", "lastSeenBeforeTime_customFormatWithDateOnly" }, config = "yyyy-MM-dd", spec = Between.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = { "lastSeenEqual" })
		@ResponseBody
		public Object findCustomersWithLastSeenEqualToDateWithDefaultTime(
				@Spec(path = "lastSeen", params = { "lastSeenEqual" }, config = "yyyy-MM-dd", spec = Equal.class) Specification<Customer> spec) {
			return customerRepo.findAll(spec);
		}
    }

	@Test
	public void findsByTimestampBeforeWithDefaultFormat() throws Exception {
		mockMvc.perform(get("/customers")
						.param("lastSeenBefore", "2022-10-12T22:17:13.000Z")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].firstName").value("Lisa"))
				.andExpect(jsonPath("$[1].firstName").value("Moe"))
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

    @Test
    public void findsByTimestampBeforeWithCustomFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastSeenBefore_customFormat", "2022/11/01, 09:13:12.000")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

	@Test
	public void findsByTimestampBeforeWithCustomFormatWithDateOnly() throws Exception {
		mockMvc.perform(get("/customers")
						.param("lastSeenBefore_customFormatWithDateOnly", "2022-11-12")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[4]").doesNotExist());
	}

    @Test
    public void findsByTimestampAfterWithDefaultFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastSeenAfter", "2022-10-12T22:17:13.000Z")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[6]").doesNotExist());
    }

    @Test
    public void findsByTimestampAfterWithCustomFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastSeenAfter_customFormat", "2022/12/06, 15:06:01.456")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

	@Test
	public void findsByTimestampAfterWithCustomFormatWithDateOnly() throws Exception {
		mockMvc.perform(get("/customers")
						.param("lastSeenAfter_customFormatWithDateOnly", "2022-12-06")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test
	public void findsByTimestampBetweenWithDefaultFormat() throws Exception {
		mockMvc.perform(get("/customers")
						.param("lastSeenAfterTime", "2022-10-12T22:17:11.000Z")
						.param("lastSeenBeforeTime", "2022-11-01T09:13:12.000Z")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].firstName").value("Bart"))
				.andExpect(jsonPath("$[1].firstName").value("Moe"))
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByTimestampBetweenWithCustomFormat() throws Exception {
		mockMvc.perform(get("/customers")
						.param("lastSeenAfterTime_customFormat", "2022/12/06, 15:06:01.456")
						.param("lastSeenBeforeTime_customFormat", "2022/12/14, 10:23:11.987")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByTimestampBetweenWithCustomFormatWithDateOnly() throws Exception {
		mockMvc.perform(get("/customers")
						.param("lastSeenAfterTime_customFormatWithDateOnly", "2022-12-06")
						.param("lastSeenBeforeTime_customFormatWithDateOnly", "2022-12-14")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void findsByTimestampEqualWithCustomDateFormatWithDateOnly() throws Exception {
		customer("Barry", "Benson")
				.nickName("Bee")
				.lastSeen(Timestamp.valueOf(LocalDateTime.of(2022, 12, 15, 0, 0,0)))
				.build(em);

		mockMvc.perform(get("/customers")
						.param("lastSeenEqual", "2022-12-15")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Barry')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
}
