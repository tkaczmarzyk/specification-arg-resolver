package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInterceptor;
import net.kaczmarzyk.utils.interceptor.InterceptedStatementsAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JoinAndJoinFetchReferredToTheSameTableTest extends E2eTestBase {

	@Controller
	public static class TestController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/join-and-join-fetch/customers")
		@ResponseBody
		public Object findByNameAndOrders(

				@Join(path = "orders", alias = "o")
				@JoinFetch(paths = "orders", alias = "o")
				@And({
						@Spec(path = "firstName", spec = Equal.class),
						@Spec(path = "o.itemName", params = "order", spec = LikeIgnoreCase.class)
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec, Sort.by("id"));
		}
	}

	@Test
	public void findsByOrdersAndName() throws Exception {
		HibernateStatementInterceptor.clearInterceptedStatements();

		mockMvc.perform(get("/join-and-join-fetch/customers")
				.param("firstName", "Homer")
				.param("order", "Duff Beer")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].firstName").value("Homer"))
			.andExpect(jsonPath("$[1]").doesNotExist());

		InterceptedStatementsAssert.assertThatInterceptedStatements()
			.hasSelects(1)
			.hasJoins(2);
	}


}
