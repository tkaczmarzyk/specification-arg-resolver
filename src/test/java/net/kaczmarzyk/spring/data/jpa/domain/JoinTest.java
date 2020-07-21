package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.ItemTag;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

import static javax.persistence.criteria.JoinType.LEFT;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static org.assertj.core.api.Assertions.assertThat;

public class JoinTest extends IntegrationTestBase {

	Customer homerSimpson;
	Customer margeSimpson;
	Customer bartSimpson;

	@Before
	public void initData() {
		ItemTag books = itemTag("books").build(em);

		homerSimpson = customer("Homer", "Simpson")
				.orders(order("Duff Beer"), order("Donuts"))
				.build(em);

		margeSimpson = customer("Marge", "Simpson")
				.build(em);

		bartSimpson = customer("Bart", "Simpson")
				.orders(order("Comic Books").withTags(books))
				.build(em);

		em.flush();
		em.clear();
	}

	@Test
	public void joinsLazyCollection() {
		Join<Customer> joinOrders = new Join<>(queryCtx, "orders", "o", LEFT, true);
		Equal<Customer> orderedItemName = new Equal<>(queryCtx, "o.itemName", new String[]{ "Duff Beer" }, defaultConverter);

		Conjunction<Customer> conjunction = new Conjunction<>(joinOrders, orderedItemName);

		List<Customer> customers = customerRepo.findAll(conjunction, Sort.by("id"));

		assertThat(customers)
				.extracting(Customer::getFirstName)
				.containsExactly("Homer");
	}

	@Test
	public void performsMultilevelJoinWithAttributeOfTypeSet() {
		Join<Customer> joinOrders = new Join<>(queryCtx, "orders", "o", LEFT, true);
		Join<Customer> joinTags = new Join<>(queryCtx, "o.tags", "t", LEFT, true);
		Equal<Customer> tagEqual = new Equal<>(queryCtx, "t.name", new String[]{ "books" }, defaultConverter);

		Conjunction<Customer> conjunction = new Conjunction<>(joinOrders, joinTags, tagEqual);

		List<Customer> customers = customerRepo.findAll(conjunction, Sort.by("id"));

		assertThat(customers)
				.extracting(Customer::getFirstName)
				.containsExactly("Bart");
	}

	@Test
	public void performsMultilevelJoinWithAttributeOfTypeList() {
		Join<Customer> joinOrders = new Join<>(queryCtx, "orders", "o", LEFT, true);
		Join<Customer> joinTags = new Join<>(queryCtx, "o.tagsList", "t", LEFT, true);
		Equal<Customer> tagEqual = new Equal<>(queryCtx, "t.name", new String[]{ "books" }, defaultConverter);

		Conjunction<Customer> conjunction = new Conjunction<>(joinOrders, joinTags, tagEqual);

		List<Customer> customers = customerRepo.findAll(conjunction, Sort.by("id"));

		assertThat(customers)
				.extracting(Customer::getFirstName)
				.containsExactly("Bart");
	}

	@Test
	public void performsMultilevelJoinWithAttributeOfTypeCollection() {
		Join<Customer> joinOrders = new Join<>(queryCtx, "orders", "o", LEFT, true);
		Join<Customer> joinTags = new Join<>(queryCtx, "o.tagsCollection", "t", LEFT, true);
		Equal<Customer> tagEqual = new Equal<>(queryCtx, "t.name", new String[]{ "books" }, defaultConverter);

		Conjunction<Customer> conjunction = new Conjunction<>(joinOrders, joinTags, tagEqual);

		List<Customer> customers = customerRepo.findAll(conjunction, Sort.by("id"));

		assertThat(customers)
				.extracting(Customer::getFirstName)
				.containsExactly("Bart");
	}

	@Test
	public void performsMultilevelJoinWithSimpleEntityAttribute() {
		Join<Customer> joinOrders = new Join<>(queryCtx, "orders", "o", LEFT, true);
		Join<Customer> joinTags = new Join<>(queryCtx, "o.note", "n", LEFT, true);
		Equal<Customer> tagEqual = new Equal<>(queryCtx, "n.title", new String[]{ "NoteDonuts" }, defaultConverter);

		Conjunction<Customer> conjunction = new Conjunction<>(joinOrders, joinTags, tagEqual);

		List<Customer> customers = customerRepo.findAll(conjunction, Sort.by("id"));

		assertThat(customers)
				.extracting(Customer::getFirstName)
				.containsExactly("Homer");
	}

}
