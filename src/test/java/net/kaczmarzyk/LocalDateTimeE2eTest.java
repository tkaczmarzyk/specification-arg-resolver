package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocalDateTimeE2eTest extends E2eTestBase{
    @Controller
    public static class LocalDateSpecsController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "lastOrderTimeBefore")
        @ResponseBody
        public Object findCustomersRegisteredBefore(
                @Spec(path="lastOrderTime", params="lastOrderTimeBefore", config="yyyy-MM-dd\'T\'HH:mm:ss", spec= LessThan.class) Specification<Customer> spec) {

            customerRepo.findByLastOrderTimeBefore(LocalDateTime.parse("2016-09-01T00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd\'T\'HH:mm:ss")));
            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "lastOrderTimeAfter")
        @ResponseBody
        public Object findCustomersRegisteredAfter(
                @Spec(path="lastOrderTime", params="lastOrderTimeAfter", spec= GreaterThan.class) Specification<Customer> spec) {
            return customerRepo.findAll(spec);
        }

    }

    /*The test will fail, there is a bug in hibernate-core v5.0.12 regarding the conversion of LocalDateTime to TIMESTAMP for H2 database
    * See https://stackoverflow.com/questions/44676732/how-to-map-java-time-localdatetime-to-timestamp-in-h2-database-with-hibernate*/
    @Test
    public void findsByDateTimeBeforeWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeBefore", "2016-09-01T00:00:00")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


    }

    @Test
    public void findsByDateTimeAfter() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("lastOrderTimeAfter", "2017-08-22T10:00:00")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
