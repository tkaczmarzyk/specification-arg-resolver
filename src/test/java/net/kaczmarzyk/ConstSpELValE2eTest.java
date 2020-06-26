package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithSARConfiguredWithApplicationContext;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.Spec.StringValueType.SpEL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConstSpELValE2eTest extends IntegrationTestBaseWithSARConfiguredWithApplicationContext {
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "#{new String('Sim').concat(new String(T(java.util.Base64).getDecoder().decode('cHNvbg==')))}",
			constValType = SpEL
	)
	public interface LastNameSpecWithConstValueInSpEL extends Specification<Customer> {
	}
	
	@Spec(
			path = "birthDate",
			spec = GreaterThanOrEqual.class,
			constVal = "#{T(java.time.LocalDate).now()}",
			constValType = SpEL
	)
	public interface CustomersBornInTheFuture extends Specification<Customer> {
	}
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "#{'${SpEL-support.lastName.prefix}'.concat('ak')}",
			constValType = SpEL
	)
	public interface LastNameSpecWithConstValueInSpELWithPropertyPlaceholder extends Specification<Customer> {
	}
	
	@Spec(
			path = "lastName",
			spec = Equal.class,
			constVal = "${SpEL-support.lastName.value}",
			constValType = SpEL
	)
	public interface LastNameSpecWithConstValueWithPropertyPlaceholder extends Specification<Customer> {
	}
	
	@Controller
	public static class TestController {
		
		@Autowired
		CustomerRepository customerRepo;
		
		@RequestMapping("/constValInSpEL")
		@ResponseBody
		public Object listsCustomersUsingSpecWithConstValueInSpEL(LastNameSpecWithConstValueInSpEL spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/customersBornInTheFutureConstVal")
		@ResponseBody
		public Object listsCustomersWhichCameFromTheFuture(CustomersBornInTheFuture spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/constValInSpELWithPropertyPlaceholder")
		@ResponseBody
		public Object listsCustomersUsingSpecWithConstValueInSpELWithPropertyPlaceholder(LastNameSpecWithConstValueInSpELWithPropertyPlaceholder spec) {
			return customerRepo.findAll(spec);
		}
		
		@RequestMapping("/constValWithPropertyPlaceholder")
		@ResponseBody
		public Object listsCustomersUsingSpecWithConstValueWithPropertyPlaceholder(LastNameSpecWithConstValueWithPropertyPlaceholder spec) {
			return customerRepo.findAll(spec);
		}
		
	}
	
	@Before
	public void initializeTestData() {
		customer("Homer", "Simpson").birthDate(LocalDate.of(1970, 03, 21));
		customer("Marge", "Simpson").birthDate(LocalDate.of(1972, 7, 13)).build(em);
		customer("Bart", "Simpson").birthDate(LocalDate.of(1992, 2, 23)).build(em);
		customer("Lisa", "Simpson").birthDate(LocalDate.of(1994, 11, 7)).build(em);
		customer("Maggie", "Simpson").birthDate(LocalDate.of(1966, 4, 1)).build(em);
		customer("Maggie", "Simpson").birthDate(LocalDate.of(1966, 4, 1)).build(em);
		customer("Moe", "Szyslak").build(em);
		customer("Minnie", "Szyslak").build(em);
		customer("Ned", "Flanders").build(em);
		customer("Bart Jr.", "Simpsonx").birthDate(LocalDate.of(3000, 6, 22)).build(em);
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValInSpEL() throws Exception {
		mockMvc.perform(get("/constValInSpEL")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[5]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValInSpEL2() throws Exception {
		mockMvc.perform(get("/customersBornInTheFutureConstVal")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Bart Jr.')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValInSpELWithPropertyPlaceholder() throws Exception {
		mockMvc.perform(get("/constValInSpELWithPropertyPlaceholder")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
	@Test
	public void filtersBySingleSpecWithoutParamUsingConstValWithPropertyPlaceholder() throws Exception {
		mockMvc.perform(get("/constValWithPropertyPlaceholder")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}
	
}
