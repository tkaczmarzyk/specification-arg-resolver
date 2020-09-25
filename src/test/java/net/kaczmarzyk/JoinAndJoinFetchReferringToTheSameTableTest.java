package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerDto;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInterceptor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.JoinType;

import static java.util.stream.Collectors.toList;
import static javax.persistence.criteria.JoinType.INNER;
import static net.kaczmarzyk.utils.interceptor.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JoinAndJoinFetchReferringToTheSameTableTest extends E2eTestBase {

	@Controller
	public static class TestController {

		@Autowired
		CustomerRepository customerRepository;

		@RequestMapping(value = "/join-and-join-fetch/customers")
		@ResponseBody
		public Object findByNameAndOrders(
				@Join(path = "orders", alias = "o", type = INNER)
				@JoinFetch(paths = "orders", alias = "o")
				@And({
						@Spec(path = "firstName", spec = Equal.class),
						@Spec(path = "o.itemName", params = "order", spec = LikeIgnoreCase.class)
				}) Specification<Customer> spec) {

			return customerRepository.findAll(spec, Sort.by("id")).stream()
					.map(CustomerDto::from)
					.collect(toList());
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

		assertThatInterceptedStatements()
				.hasSelects(1)
				.hasJoins(2)
				.hasOneClause(" inner join orders ")
				.hasOneClause(" left outer join orders ");
	}


}
