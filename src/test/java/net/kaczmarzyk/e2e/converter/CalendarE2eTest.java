package net.kaczmarzyk.e2e.converter;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.EqualDay;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class CalendarE2eTest extends E2eTestBase {
    @Controller
    public static class CalendarSpecController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "registeredEqualDay")
        @ResponseBody
        public Object findCustomersRegisteredDayEqualWithDefaultConfig(
                @Spec(path="registrationDate", params="registeredEqualDay", spec=EqualDay.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "registeredEqualDayCustomConfig")
        @ResponseBody
        public Object findCustomersRegisteredDayEqualWithCustomConfigContainingTime(
                @Spec(path="registrationDate", params="registeredEqualDayCustomConfig", config="yyyy-MM-dd\'T\'HH:mm:ss", spec=EqualDay.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }
    }

    @Test
    public void findsByEqualDayUsingDefaultConfig() throws Exception {
        customer("Barry", "Benson")
                .registrationDate(2014, 3, 16)
                .build(em);
        customer("Adam", "Flayman")
                .registrationDate(2014, 3, 14, 23, 59, 59, 999)
                .build(em);

        mockMvc.perform(get("/customers")
                        .param("registeredEqualDay", "2014-03-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    public void findsByEqualDayUsingCustomConfigIgnoringTime() throws Exception {
        customer("Barry", "Benson")
                .registrationDate(2014, 3, 16)
                .build(em);
        customer("Adam", "Flayman")
                .registrationDate(2014, 3, 14, 23, 59, 59, 999)
                .build(em);

        mockMvc.perform(get("/customers")
                        .param("registeredEqualDayCustomConfig", "2014-03-15T12:34:19")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Moe"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
}
