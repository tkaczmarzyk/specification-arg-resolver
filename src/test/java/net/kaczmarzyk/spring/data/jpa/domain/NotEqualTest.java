/**
 * Copyright 2014-2023 the original author or authors.
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

import com.jparams.verifier.tostring.ToStringVerifier;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.Gender.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author Mateusz Fedkowicz
 */
public class NotEqualTest extends IntegrationTestBase {

	private static final String INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE = "Invalid size of 'httpParamValues' array, Expected 1 but was ";

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

		assertThatThrownBy(() -> customerRepo.findAll(genderRobot))
				.isInstanceOf(InvalidDataAccessApiUsageException.class)
				.hasCauseInstanceOf(IllegalArgumentException.class)
				.hasMessage("could not find value ROBOT for enum class Gender");
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
	public void filtersByCharValue() {
		NotEqual<Customer> gender = new NotEqual<>(queryCtx, "genderAsChar", new String[] { "M" }, defaultConverter);
		assertFilterMembers(gender, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByCharacterValue() {
		NotEqual<Customer> gender = new NotEqual<>(queryCtx, "genderAsCharacter", new String[] { "F" }, defaultConverter);
		assertFilterMembers(gender, homerSimpson);
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

	@Test
	public void rejectsNullArgumentArray() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new NotEqual<>(queryCtx, "path", null, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "null");
	}

	@Test
	public void rejectsMissingArguments() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new NotEqual<>(queryCtx, "path", new String[] {}, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[]");
	}

	@Test
	public void rejectsTooManyArguments() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new NotEqual<>(queryCtx, "path", new String[] {"2014-03-10", "2014-03-11"}, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[2014-03-10, 2014-03-11]");
	}

	@Test
	public void equalsAndHashCodeContract() {
		EqualsVerifier.forClass(NotEqual.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}

	@Test
	public void toStringVerifier() {
		ToStringVerifier.forClass(NotEqual.class)
				.withIgnoredFields("queryContext")
				.verify();
	}
}
