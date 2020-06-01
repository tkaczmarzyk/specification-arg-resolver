package net.kaczmarzyk.e2e.annotated;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
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
 * TC-1. interface with @Join spec
 * TC-2. interface with @Join spec extended by param spec
 * TC-3. interface without any spec extended by param @Join spec
 * TC-4. interface without any spec extended by interface with @Join spec
 * TC-5. interface with @Join spec extended by other interface with @Join spec
 */
public class AnnotatedSpecInterfaceJoinE2eTest extends E2eTestBase {

	// TC-1. interface with @Join spec
	@Join(path = "badges", alias = "b")
	@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class)
	private interface BadgeFilter extends Specification<Customer> {
	}

	// TC-3. interface without any spec extended by param @Join spec
	private static interface EmptyFilter extends Specification<Customer> {
	}

	// TC-4. interface without any spec extended by interface with @Join spec
	private static interface EmptyFilterExtendedByInterfaceWithJoinSpec extends BadgeFilter {}

	// TC-5. interface with @Join spec extended by other interface with @Join spec
	@Join(path = "orders", alias = "o")
	@Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
	private static interface ItemNameBadgeTypeFilter extends BadgeFilter {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByBadgeType")
		@ResponseBody
		public List<Customer> getCustomersWithCustomJoinFilter(BadgeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface with @Join spec extended by param spec
		@RequestMapping(value = "/anno-iface-join/customersByBadgeTypeAndItemName")
		@ResponseBody
		public List<Customer> getCustomersWithCustomAndFilterExtendedByJoinFilter(
				@Join(path = "orders", alias = "o")
				@Spec(path = "o.itemName", params = "itemName", spec = Equal.class) BadgeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extended by param @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByEmptyFilterExtendedByParamSpec")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendedByParamSpec(
				@Join(path = "badges", alias = "b")
				@Spec(path = "b.badgeType", params = "badgeType", spec = Equal.class) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface without any spec extended by interface with @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByEmptyFilterExtendedByInterfaceWithJoinSpec")
		@ResponseBody
		public List<Customer> getCustomersByEmptyFilterExtendedByInterfaceWithJoinSpec(EmptyFilterExtendedByInterfaceWithJoinSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-5. interface with @Join spec extended by other interface with @Join spec
		@RequestMapping(value = "/anno-iface-join/customersByItemNameBadgeTypeFilter")
		@ResponseBody
		public List<Customer> getCustomersByItemNameBadgeTypeFilter(ItemNameBadgeTypeFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface with @Join spec
	public void filtersAccordingToAnnotatedJoinSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByBadgeType")
				.param("badgeType", "Beef Eater")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-2. interface with @Join spec extended by param spec
	public void filtersAccordingToInterfaceWithJoinSpecExtendedBySpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByBadgeTypeAndItemName")
				.param("badgeType", "Tomacco Eater")
				.param("itemName", "Tomacco")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extended by param @Join spec
	public void filtersAccordingToEmptyFilterExtendedBySpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByBadgeType")
				.param("badgeType", "Beef Eater")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-4. interface without any spec extended by interface with @Join spec
	public void filtersAccordingToEmptyFilterExtendedByInterfaceWithJoinSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByEmptyFilterExtendedByInterfaceWithJoinSpec")
				.param("badgeType", "Beef Eater")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-5. interface with @Join spec extended by other interface with @Join spec
	public void filtersAccordingToItemNameBadgeTypeFilter() throws Exception {
		mockMvc.perform(get("/anno-iface-join/customersByItemNameBadgeTypeFilter")
				.param("badgeType", "Tomacco Eater")
				.param("itemName", "Tomacco")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}


}
