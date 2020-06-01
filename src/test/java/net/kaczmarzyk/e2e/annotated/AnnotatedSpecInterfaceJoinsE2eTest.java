package net.kaczmarzyk.e2e.annotated;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Joins;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test cases:
 * TC-1. interface with @Joins spec
 * TC-2. interface with @Joins spec extended by param spec
 * TC-3. interface without any spec extended by @Joins param spec
 * TC-4. interface without any spec extended by interface with @Joins spec
 * TC-5. interface with @Joins spec extended by other interface with @Joins spec
 */
public class AnnotatedSpecInterfaceJoinsE2eTest extends E2eTestBase {

	// TC-1. interface with @Joins spec
	@Joins({
			@Join(path = "orders", alias = "o", type = JoinType.LEFT),
			@Join(path = "badges", alias = "b", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "o.itemName", params = "order", spec = Like.class),
			@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
	})
	private static interface ItemNameOrBadgeTypeFilter extends Specification<Customer> {
	}

	// TC-3. interface without any spec extended by param spec
	private static interface EmptyFilter extends Specification<Customer> {
	}

	// TC-4. interface without any spec extended by interface with @Joins spec
	@Joins({
			@Join(path = "orders", alias = "o", type = JoinType.LEFT),
			@Join(path = "badges", alias = "b", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "o.itemName", params = "order", spec = Like.class),
			@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
	})
	private static interface EmptyFilterExtendedByInterfaceWithJoinsSpec extends ItemNameOrBadgeTypeFilter {

	}

	// TC-5. interface with @Joins spec extended by other interface with @Joins spec
	@Joins({
			@Join(path = "badges", alias = "b", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
	})
	private static interface BadgeTypeFilter extends Specification<Customer> {
	}

	@Joins({
			@Join(path = "orders", alias = "o", type = JoinType.LEFT)
	})
	@Or({
			@Spec(path = "o.itemName", params = "order", spec = Like.class)
	})
	private static interface CustomJoinsFilterExtendedByOtherJoinsFilter extends BadgeTypeFilter {
	}

	@RestController
	public static class CustomSpecTestsController {

		@Autowired
		CustomerRepository customerRepo;

		// TC-1. interface with @Joins spec
		@RequestMapping(value = "/anno-iface-joins/customersByItemNameOrBadgeTypeFilter")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(ItemNameOrBadgeTypeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-2. interface with @Join spec extended by param spec
		@RequestMapping(value = "/anno-iface-joins/customersByItemNameOrBadgeTypeFilterExtendedBySpecParam")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeTypeAndLastName(
				@Spec(params = "lastName", path = "lastName", spec = Equal.class) ItemNameOrBadgeTypeFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-3. interface without any spec extended by param spec
		@RequestMapping(value = "/anno-iface-joins/customersByEmptyFilterExtendedByParamSpec")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(
				@Joins({
						@Join(path = "orders", alias = "o", type = JoinType.LEFT),
						@Join(path = "badges", alias = "b", type = JoinType.LEFT)
				})
				@Or({
						@Spec(path = "o.itemName", params = "order", spec = Like.class),
						@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
				}) EmptyFilter spec) {
			return customerRepo.findAll(spec);
		}

		// TC-4. interface without any spec extended by interface with @Joins spec
		@RequestMapping(value = "/anno-iface-joins/customersByEmptyFilterExtendedByInterfaceWithJoinsSpec")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(EmptyFilterExtendedByInterfaceWithJoinsSpec spec) {
			return customerRepo.findAll(spec);
		}

		// TC-5. interface with @Joins spec extended by other interface with @Joins spec
		@RequestMapping(value = "/anno-iface-joins/customersByCustomJoinsFilterExtendedByOtherJoinsFilter")
		@ResponseBody
		public List<Customer> getCustomersByItemNameOrBadgeType(CustomJoinsFilterExtendedByOtherJoinsFilter spec) {
			return customerRepo.findAll(spec);
		}

	}

	@Test // TC-1. interface without any spec extended by param spec
	public void filtersAccordingToAnnotatedJoinsSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByItemNameOrBadgeTypeFilter")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test //TC-2. interface with @Joins spec extended by param spec
	public void filtersAccordingToInterfaceWithJoinSpecExtendedBySpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByItemNameOrBadgeTypeFilterExtendedBySpecParam")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.param("lastName", "Simpson")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test // TC-3. interface without any spec extended by param spec
	public void filtersAccordingToEmptyFilterExtendedByInterfaceWithJoinsSpec() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByEmptyFilterExtendedByParamSpec")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-4. interface without any spec extended by interface with @Joins spec
	public void filtersAccordingToEmptyFilterExtendedBySpecParam() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByEmptyFilterExtendedByInterfaceWithJoinsSpec")
				.param("badge", "Beef Eater")
				.param("order", "Bible")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[?(@.firstName=='Ned')]").exists())
			.andExpect(jsonPath("$[2]").doesNotExist());
	}

	@Test // TC-5. interface without any spec extended by interface with @Joins spec
	public void filtersAccordingToCustomJoinsFilterExtendedByOtherJoinsFilter() throws Exception {
		mockMvc.perform(get("/anno-iface-joins/customersByCustomJoinsFilterExtendedByOtherJoinsFilter")
				.param("badge", "Beef Eater")
				.param("order", "Pizza")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
			.andExpect(jsonPath("$[1]").doesNotExist());
	}

}
