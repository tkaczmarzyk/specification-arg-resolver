package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.IGNORE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IgnoreTypeMismatchE2eTest extends E2eTestBase {

	@Controller
	public static class TestIgnoreOnTypeMismatchController {

		@Autowired
		CustomerRepository customerRepo;

		@RequestMapping(value = "/ignore/customers", params = { "id" })
		@ResponseBody
		public Object findById(
				@Spec(path = "id", params = "id", spec = Equal.class, onTypeMismatch = IGNORE) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/ignore/customers", params = { "id_in" })
		@ResponseBody
		public Object findByIdIn(
				@Spec(path = "id", params = "id_in", spec = In.class, onTypeMismatch = IGNORE) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/ignore/customers", params = { "query" })
		@ResponseBody
		public Object findByIdOrFirstName(@Or({
				@Spec(path = "id", params = "query", spec = Equal.class, onTypeMismatch = IGNORE),
				@Spec(path = "firstName", params = "query", spec = LikeIgnoreCase.class, onTypeMismatch = IGNORE) }) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}

		@RequestMapping(value = "/ignore/customers", params = { "from", "to" })
		@ResponseBody
		public Object findByRegistrationDateBetween(
				@Spec(path = "registrationDate", params = {"from", "to"}, spec = Between.class, onTypeMismatch = IGNORE) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}

	@Test
	public void ignoresSpecificationWithMismatchedParameterForEqualSpecificationType_returnsAllResultsIfItWasOneAndOnlyFilteringParameter() throws Exception {
		mockMvc.perform(get("/ignore/customers")
						.param("id", "not a Long")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[8]").doesNotExist());
	}

	@Test
	public void ignoresMismatchedParameterValueForInSpecificationType_returnsEmptyResultsIfNoneOfProvidedParamsWereValidForInSpecificationType() throws Exception {
		mockMvc.perform(get("/ignore/customers")
						.param("id_in", "not an id1", "not an id2")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	public void filtersOnlyByValidValuesForInSpecificationType() throws Exception {
		mockMvc.perform(get("/ignore/customers")
						.param("id_in", homerSimpson.getId().toString(), maggieSimpson.getId().toString(), "not valid id")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test
	public void filtersByEitherIdOrNameEvenThoughTheirTypesAreDifferent() throws Exception {
		mockMvc.perform(get("/ignore/customers")
						.param("query", "o")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[2]").doesNotExist());

		mockMvc.perform(get("/ignore/customers")
						.param("query", homerSimpson.getId().toString())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	public void ignoresSpecificationIfAtLeastOneParameterDoesNotMatchForOtherThanInSpecificationType_returnsAllResults() throws Exception {
		mockMvc.perform(get("/ignore/customers")
						.param("from", "not-valid-date")
						.param("to", "2014-03-21")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
				.andExpect(jsonPath("$[8]").doesNotExist());
	}
}