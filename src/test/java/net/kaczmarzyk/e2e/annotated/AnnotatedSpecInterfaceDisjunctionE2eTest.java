package net.kaczmarzyk.e2e.annotated;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test cases:
 * TC-1. interface with @Disjunction spec
 * TC-2. interface without any spec extended by param spec
 * TC-3. interface without any spec extended by interface with @Disjunction spec
 * TC-4. interface with @Disjunction spec extended by other interface with @Disjunction spec
 */
public class AnnotatedSpecInterfaceDisjunctionE2eTest extends E2eTestBase {

	// TC-1. interface with @Disjunction spec
	@Disjunction(value = @And({
			@Spec(params = "lastName", path = "lastName", spec = Equal.class),
			@Spec(params = "gender", path = "gender", spec = Equal.class)
	}), or = {
			@Spec(params = "firstName", path = "firstName", spec = Equal.class)
	})
	private static interface LastNameAndGenderOrFirstNameFilter extends Specification<Customer> {
	}

	// TC-2. interface without any spec extended by param spec
	private static interface EmptyFilter extends Specification<Customer> {
	}

	// TC-3. interface without any spec extended by interface with @Disjunction spec
	private static interface EmptyFilterExtendedByInterfaceWithDisjunctionSpec extends LastNameAndGenderOrFirstNameFilter {
	}

	// TC-4. interface with @Disjunction spec extended by other interface with @Disjunction spec
	@Disjunction(value = @And({
			@Spec(params = "firstName2", path = "firstName", spec = Like.class)
	}), or = @Spec(params = "firstName3", path = "firstName", spec = Like.class))
	private static interface DisjunctionFilterExtendedByOtherDisjunctionFilter extends LastNameAndGenderOrFirstNameFilter {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Disjunction spec
		@RequestMapping(value = "/anno-iface-disjunction/customersByLastNameAndGenderOrFirstNameFilter")
		@ResponseBody
		public List<Customer> getCustomersByLastNameAndGenderOrFirstNameFilter(LastNameAndGenderOrFirstNameFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface without any spec extended by param spec
		@RequestMapping(value = "/anno-iface-disjunction/customersByEmptyFilterExtendedByDisjunctionSpecParam")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendedByDisjunctionSpecParam(
				@Disjunction(value = @And({
						@Spec(params = "lastName", path = "lastName", spec = Equal.class),
						@Spec(params = "gender", path = "gender", spec = Equal.class)
				}), or = {
						@Spec(params = "firstName", path = "firstName", spec = Equal.class)
				}) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extended by interface with @Disjunction spec
		@RequestMapping(value = "/anno-iface-disjunction/customersByEmptyFilterExtendedByInterfaceWithDisjunctionSpec")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendedByDisjunctionSpecParam(
				EmptyFilterExtendedByInterfaceWithDisjunctionSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface with @Disjunction spec extended by other interface with @Disjunction spec
		@RequestMapping(value = "/anno-iface-disjunction/customersByDisjunctionFilterExtendedByOtherDisjunctionFilter")
		@ResponseBody
		public List<Customer> getCustomersByDisjunctionFilterExtendedByOtherDisjunctionFilter(
				DisjunctionFilterExtendedByOtherDisjunctionFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @Disjunction spec
	public void filtersAccordingToInterfaceWithOrSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-disjunction/customersByLastNameAndGenderOrFirstNameFilter")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test // TC-2. interface without any spec extended by param spec
	public void filtersAccordingToEmptyFilterExtendedByDisjunctionSpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-disjunction/customersByEmptyFilterExtendedByDisjunctionSpecParam")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extended by interface with @Disjunction spec
	public void filtersAccordingToEmptyFilterExtendedByInterfaceWithDisjunctionSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-disjunction/customersByEmptyFilterExtendedByInterfaceWithDisjunctionSpec")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[3]").doesNotExist());
	}

	@Test // TC-4. interface with @Disjunction spec extended by other interface with @Disjunction spec
	public void filtersAccordingToDisjunctionFilterExtendedByOtherDisjunctionFilter() throws Exception {
		mockMvc.perform(get("/anno-iface-disjunction/customersByDisjunctionFilterExtendedByOtherDisjunctionFilter")
				.param("firstName", "Ned")
				.param("lastName", "Simpson")
				.param("gender", "MALE")
				.param("firstName2", "er")
				.param("firstName3", "ar")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

}
