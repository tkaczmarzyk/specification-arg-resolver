package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.DateBetween;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocalDateE2eTest extends E2eTestBase {
    @Controller
    public static class LocalDateSpecsController {

        @Autowired
        CustomerRepository customerRepo;

        @RequestMapping(value = "/customers", params = "birthDateBefore")
        @ResponseBody
        public Object findCustomersRegisteredBefore(
                @Spec(path="birthDate", params="birthDateBefore", config="dd-MM-yyyy", spec= LessThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

        @RequestMapping(value = "/customers", params = "birthDateAfter")
        @ResponseBody
        public Object findCustomersRegisteredAfter(
                @Spec(path="birthDate", params="birthDateAfter", spec= GreaterThan.class) Specification<Customer> spec) {

            return customerRepo.findAll(spec);
        }

    }

    @Test
    public void findsByDateBeforeWithCustomDateFormat() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("birthDateBefore", "16-03-1996")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Homer"))
                .andExpect(jsonPath("$[1].firstName").value("Marge"));
    }

    @Test
    public void findsByDateAfter() throws Exception {
        mockMvc.perform(get("/customers")
                                .param("birthDateAfter", "1996-03-26")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Maggie"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

}
