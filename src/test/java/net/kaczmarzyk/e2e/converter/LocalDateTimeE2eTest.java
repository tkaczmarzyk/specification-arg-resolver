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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocalDateTimeE2eTest extends E2eTestBase {
    @Controller
    public static class LocalDateSpecsController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "lastOrderTimeBefore")
        @ResponseBody
        public Object findCustomersWIthLastOrderBefore_defaultDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeBefore", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = "lastOrderTimeBefore_customFormat")
        @ResponseBody
        public Object findCustomersWIthLastOrderBefore_customDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeBefore_customFormat", config="yyyy/MM/dd', 'HH:mm", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "lastOrderTimeBefore_customFormatWithDateOnly")
        @ResponseBody
        public Object findCustomersWithLastOrderBefore_customDateTimePatternWithDateOnly(
                @Spec(path="lastOrderTime", params="lastOrderTimeBefore_customFormatWithDateOnly", config="yyyy-MM-dd", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "lastOrderTimeAfter")
        @ResponseBody
        public Object findCustomersWIthLastOrderAfter_defaultDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeAfter", spec= GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = "lastOrderTimeAfter_customFormat")
        @ResponseBody
        public Object findCustomersWithLastOrderAfter_customDateTimePattern(
                @Spec(path="lastOrderTime", params="lastOrderTimeAfter_customFormat", config="yyyy/MM/dd', 'HH:mm", spec= GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "lastOrderTimeAfter_customFormatWithDateOnly")
        @ResponseBody
        public Object findCustomersWithLastOrderAfter_customDateTimePatternWithDateOnly(
                @Spec(path="lastOrderTime", params="lastOrderTimeAfter_customFormatWithDateOnly", config="yyyy-MM-dd", spec= GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = { "lastOrderTimeAfter", "lastOrderTimeBefore" })
        @ResponseBody
        public Object findCustomersWithLastOrderTimeBetween_defaultDateTimePattern(
                @Spec(path="lastOrderTime", params={ "lastOrderTimeAfter", "lastOrderTimeBefore" }, spec= Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }
        
        @RequestMapping(value = "/customers", params = { "lastOrderTimeAfter_customFormat", "lastOrderTimeBefore_customFormat" })
        @ResponseBody
        public Object findCustomersWithLastOrderTimeBetween_customDateTimePattern(
                @Spec(path="lastOrderTime", params={ "lastOrderTimeAfter_customFormat", "lastOrderTimeBefore_customFormat" }, config="yyyy/MM/dd', 'HH:mm", spec= Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = { "lastOrderTimeAfter_customFormatWithDateOnly", "lastOrderTimeBefore_customFormatWithDateOnly" })
        @ResponseBody
        public Object findCustomersWithLastOrderTimeBetween_customDateTimePatternWithDateOnly(
                @Spec(path="lastOrderTime", params={ "lastOrderTimeAfter_customFormatWithDateOnly", "lastOrderTimeBefore_customFormatWithDateOnly" }, config="yyyy-MM-dd", spec= Between.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = { "lastOrderTimeEqual" })
        @ResponseBody
        public Object findCustomersWithLastOrderTimeEqualToDateWithDefaultTime(
                @Spec(path="lastOrderTime", params={ "lastOrderTimeEqual" }, config="yyyy-MM-dd", spec= Equal.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

		@RequestMapping(value = "/customers", params = "lastOrderEqualDay")
		@ResponseBody
		public Object findCustomersWithLastOrderInParticularDay(
				@Spec(path="lastOrderTime", params="lastOrderEqualDay", spec=EqualDay.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/customers", params = "lastOrderEqualDay_customPattern")
		@ResponseBody
		public Object findCustomersWithLastOrderInParticularDay_customPatternWithDateOnly(
				@Spec(path="lastOrderTime", params="lastOrderEqualDay_customPattern", config = "yyyy/MM/dd", spec=EqualDay.class) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

	    @ResponseBody
	    @RequestMapping(value = "/customersWithSpecialOffer")
	    public Object findByCustomersWithAvailableSpecialOffer(
		    @Spec(path="dateOfNextSpecialOffer", spec=InTheFuture.class) Specification<Customer> spec) {

		    return customerRepo.findAll(spec);
	    }

	    @ResponseBody
	    @RequestMapping(value = "/customersWithExpiredSpecialOffer")
	    public Object findByCustomersWithExpiredSpecialOffer(
		    @Spec(path="dateOfNextSpecialOffer", spec=InThePast.class) Specification<Customer> spec) {

		    return customerRepo.findAll(spec);
	    }
    }

    @Test
    public void findsByDateTimeBeforeWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeBefore_customFormat", "2017/08/22, 09:17")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByDateTimeBeforeWithCustomDateFormatWithDateOnly() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("lastOrderTimeBefore_customFormatWithDateOnly", "2017-08-22")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeBeforeWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeBefore", "2016-10-17T18:29:00")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void findsByDateTimeAfterWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter", "2017-11-21T11:12:59")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeAfterWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter_customFormat", "2017/11/21, 11:13")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }

    @Test
    public void findsByDateTimeAfterWithCustomDateFormatWithDateOnly() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("lastOrderTimeAfter_customFormatWithDateOnly", "2017-11-21")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeBetweenWithDefaultDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter", "2017-11-21T11:12:59")
                                .param("lastOrderTimeBefore", "2017-12-14T11:12:59")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
    
    @Test
    public void findsByDateTimeBetweenWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter_customFormat", "2017/11/21, 11:12")
                                .param("lastOrderTimeBefore_customFormat", "2017/12/14, 11:12")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByDateTimeBetweenWithCustomDateFormatWithDateOnly() throws Exception {
        mockMvc.perform(get("/customers")
                        .param("lastOrderTimeAfter_customFormatWithDateOnly", "2017-11-21")
                        .param("lastOrderTimeBefore_customFormatWithDateOnly", "2017-12-14")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
                .andExpect(jsonPath("$.[?(@.firstName=='Moe')]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByDateTimeEqualWithCustomDateFormatWithDateOnly() throws Exception {
        customer("Barry", "Benson")
                .nickName("Bee")
                .lastOrderTime(LocalDateTime.of(2022, 12, 13, 0, 0,0))
                .build(em);

		mockMvc.perform(get("/customers")
						.param("lastOrderTimeEqual", "2022-12-13")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Barry')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByLastOrderInParticularDayWithDefaultConfig() throws Exception {
		customer("Barry", "Benson")
				.lastOrderTime(LocalDateTime.of(2017, 12, 19, 23, 59,59))
				.build(em);
		customer("Adam", "Flayman")
				.lastOrderTime(LocalDateTime.of(2017, 12, 21, 0, 0,0))
				.build(em);

		mockMvc.perform(get("/customers")
						.param("lastOrderEqualDay", "2017-12-20T08:45:57")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByLastOrderInParticularDayWithCustomConfigIgnoringTime() throws Exception {
		customer("Barry", "Benson")
				.lastOrderTime(LocalDateTime.of(2017, 12, 19, 23, 59,59))
				.build(em);
		customer("Adam", "Flayman")
				.lastOrderTime(LocalDateTime.of(2017, 12, 21, 0, 0,0))
				.build(em);

		mockMvc.perform(get("/customers")
						.param("lastOrderEqualDay_customPattern", "2017/12/20")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByNextSpecialOfferFromFuture() throws Exception {
		customer("Barry", "Benson")
			.nextSpecialOffer(OffsetDateTime.now().plusDays(7))
			.build(em);
		customer("Vanessa", "Bloom")
			.nextSpecialOffer(OffsetDateTime.now().minusDays(7))
			.build(em);

		mockMvc.perform(get("/customersWithSpecialOffer")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Barry"))
			.andExpect(jsonPath("$[0].lastName").value("Benson"))
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void findsByNextSpecialOfferFromPast() throws Exception {
		customer("Barry", "Benson")
			.nextSpecialOffer(OffsetDateTime.now().plusDays(7))
			.build(em);
		customer("Vanessa", "Bloom")
			.nextSpecialOffer(OffsetDateTime.now().minusDays(7))
			.build(em);

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
