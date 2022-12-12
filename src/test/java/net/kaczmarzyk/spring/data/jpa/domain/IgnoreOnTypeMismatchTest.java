package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

public class IgnoreOnTypeMismatchTest extends IntegrationTestBase {

	private Customer homerSimpson;
	private Customer margeSimpson;

	@Before
	public void initData() {
		homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
		margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
	}

	@Test
	public void ignoresSpecificationOnTypeMismatch_long() {
		IgnoreOnTypeMismatch<Customer> idQuery = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "id", new String[] { "not a long" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(idQuery);
		assertThat(result)
				.hasSize(2)
				.contains(homerSimpson)
				.contains(margeSimpson);
	}

	@Test
	public void ignoresSpecificationOnTypeMismatch_longPrimitive() {
		IgnoreOnTypeMismatch<Customer> weightQuery = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "weightLong", new String[] { "not a long" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(weightQuery);
		assertThat(result)
				.hasSize(2)
				.contains(homerSimpson)
				.contains(margeSimpson);
	}

	@Test
	public void ignoresSpecificationOnTypeMismatch_intPrimitive() {
		IgnoreOnTypeMismatch<Customer> weightQuery = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "weightInt", new String[] { "not an int" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(weightQuery);
		assertThat(result)
				.hasSize(2)
				.contains(homerSimpson)
				.contains(margeSimpson);
	}

	@Test
	public void ignoresSpecificationOnTypeMismatch_enum() {
		IgnoreOnTypeMismatch<Customer> genderQuery = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "gender", new String[] { "not an enum" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(genderQuery);
		assertThat(result)
				.hasSize(2)
				.contains(homerSimpson)
				.contains(margeSimpson);
	}

	@Test
	public void ignoresSpecificationOnTypeMismatch_date() {
		IgnoreOnTypeMismatch<Customer> registrationDateQuery = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "registrationDate", new String[] { "not a date" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(registrationDateQuery);
		assertThat(result)
				.hasSize(2)
				.contains(homerSimpson)
				.contains(margeSimpson);
	}

	@Test
	public void ignoresSpecificationOnTypeMismatch_boolean() {
		IgnoreOnTypeMismatch<Customer> goldStatusQuery = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "gold", new String[] { "not a boolean" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(goldStatusQuery);
		assertThat(result)
				.hasSize(2)
				.contains(homerSimpson)
				.contains(margeSimpson);
	}

	@Test
	public void usesWrappedSpecWhenTypeMatches() {
		IgnoreOnTypeMismatch<Customer> homerId = new IgnoreOnTypeMismatch<>(new Equal<Customer>(queryCtx, "id", new String[] { homerSimpson.getId().toString() }, defaultConverter));
		List<Customer> result = customerRepo.findAll(homerId);
		assertThat(result)
				.hasSize(1)
				.contains(homerSimpson);
	}

	@Test
	public void equalsAndHashCodeContract() {
		EqualsVerifier.forClass(IgnoreOnTypeMismatch.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
}