/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.Gender.FEMALE;
import static net.kaczmarzyk.spring.data.jpa.Gender.MALE;
import static net.kaczmarzyk.spring.data.jpa.Gender.OTHER;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author Mateusz Fedkowicz
 */
public class NotEqualTest extends IntegrationTestBase {

	protected Customer homerSimpson;
	protected Customer margeSimpson;
	protected Customer joeQuimby;

	@BeforeEach
	public void initData() {
		homerSimpson = customer("Homer", "Simpson")
				.gender(Gender.MALE)
				.registrationDate(2015, 03, 01)
				.weight(121)
				.notGolden()
				.build(em);

		margeSimpson = customer("Marge", "Simpson")
				.gender(Gender.FEMALE)
				.registrationDate(2015, 03, 01)
				.weight(55)
				.notGolden()
				.build(em);

		joeQuimby = customer("Joe", "Quimby")
				.golden()
				.weight(78)
				.registrationDate(2015, 03, 02)
				.build(em);

	}

	@Test
	public void filtersByEnumValue() {
		NotEqual<Customer> notMale = notEqualSpec("gender", MALE);
		assertFilterMembers(notMale, margeSimpson);

		NotEqual<Customer> notFemale = notEqualSpec("gender", FEMALE);
		assertFilterMembers(notFemale, homerSimpson);

		NotEqual<Customer> notOther = notEqualSpec("gender", OTHER);
		assertFilterMembers(notOther, homerSimpson, margeSimpson);
	}

	@Test
	public void rejectsNotExistingEnumConstantName() {
		NotEqual<Customer> genderRobot = notEqualSpec("gender", "ROBOT");

		assertThrows(InvalidDataAccessApiUsageException.class, () -> customerRepo.findAll(genderRobot));
	}

	@Test
	public void filtersByLongValue() {
		NotEqual<Customer> notHomer = notEqualSpec("id", homerSimpson.getId());
		assertFilterMembers(notHomer, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByPrimitiveLongValue() {
		NotEqual<Customer> notHomerWeight = notEqualSpec("weightLong", 121);
		assertFilterMembers(notHomerWeight, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByIntegerValue() {
		NotEqual<Customer> notHomerWeight = notEqualSpec("weight", 121);
		assertFilterMembers(notHomerWeight, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByPrimitiveIntValue() {
		NotEqual<Customer> notHomerWeight = notEqualSpec("weightInt", 121);
		assertFilterMembers(notHomerWeight, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByPrimitiveBooleanValue() {
		NotEqual<Customer> notGolden = notEqualSpec("gold", true);
		assertFilterMembers(notGolden, homerSimpson, margeSimpson);
	}

	@Test
	public void filtersByBooleanValue() {
		NotEqual<Customer> notGolden = notEqualSpec("goldObj", true);
		assertFilterMembers(notGolden, homerSimpson, margeSimpson);
	}

	@Test
	public void filtersByDouble() {
		NotEqual<Customer> notHomerWeight = notEqualSpec("weightDouble", homerSimpson.getWeightDouble());
		assertFilterMembers(notHomerWeight, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByString() {
		NotEqual<Customer> notSimpson = notEqualSpec("lastName", "Simpson");
		assertFilterMembers(notSimpson, joeQuimby);
	}

	@Test
	public void filtersByDateWithDefaultDateFormat() {
		NotEqual<Customer> notRegisteredOn1stMarch = notEqualSpec("registrationDate", "2015-03-01");
		assertFilterMembers(notRegisteredOn1stMarch, joeQuimby);
	}

	@Test
	public void filterByDateWithCustomDateFormat() {
		Converter customConverter = Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null);
		NotEqual<Customer> notRegisteredOn1stMarch = notEqualSpec("registrationDate", "01-03-2015", customConverter);
		assertFilterMembers(notRegisteredOn1stMarch, joeQuimby);
	}

	private <T> NotEqual<T> notEqualSpec(String path, Object expectedValue) {
		return notEqualSpec(path, expectedValue, defaultConverter);
	}

	private <T> NotEqual<T> notEqualSpec(String path, Object expectedValue, Converter converter) {
		return new NotEqual<>(queryCtx, path, new String[]{expectedValue.toString()}, converter);
	}

}
